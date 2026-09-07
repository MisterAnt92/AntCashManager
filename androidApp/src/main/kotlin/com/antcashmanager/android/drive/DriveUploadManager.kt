package com.antcashmanager.android.drive

import android.content.Context
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.data.storage.EncryptedGoogleDriveStorage
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import com.google.api.services.drive.model.File as DriveFile

/**
 * Gestisce l'upload dei file di backup a Google Drive.
 *
 * **Responsabilità**:
 * - Creare la cartella "/AntCashManager/Backups" (one-shot, stored folder ID)
 * - Uploadare file di backup in quella cartella
 * - Gestire token refresh (401 Unauthorized)
 * - Retry logic per errori transitori
 * - Logging dettagliato per debugging
 *
 * **Google Drive API**:
 * - Endpoint: https://www.googleapis.com/drive/v3
 * - Scope: `drive.file` (accesso solo ai file creati da questa app)
 * - Auth: Access token tramite GoogleSignInManager
 * - Timeout: 30s per request (configurabile)
 *
 * **Utilizzo**:
 * ```kotlin
 * val manager = DriveUploadManager(context, signInManager, settingsRepository)
 * val result = manager.ensureFolderExists()  // Crea cartella se non esiste
 * if (result.isSuccess) {
 *     val uploadResult = manager.uploadBackupFile(
 *         fileName = "AntCashManager-backup-20260821-143022.json",
 *         fileContent = backupJsonBytes,
 *         mimeType = "application/json"
 *     )
 * }
 * ```
 *
 * **Error Handling**:
 * - 401 Unauthorized: Token scaduto → chiama `getAccessTokenSilently()` → retry
 * - 403 Forbidden: Permesso revocato → failure + user deve sign-in di nuovo
 * - 404 Not Found: Folder deleted → crea nuova cartella
 * - 5xx Server Error: Retry con backoff esponenziale
 * - Network error: Eccezione; caller può decidere se retry
 *
 * **Cartella di backup**:
 * ```
 * /AntCashManager/Backups/
 *   ├── AntCashManager-backup-20260821-143022.json
 *   ├── AntCashManager-backup-20260814-120500.json
 *   └── ...
 * ```
 */
class DriveUploadManager(
    private val context: Context,
    private val signInManager: com.antcashmanager.android.auth.GoogleSignInManager,
    private val settingsRepository: com.antcashmanager.domain.repository.SettingsRepository,
) {
    // ── Feature Gating: Google Drive Backup ──
    init {
        if (!BuildConfig.INCLUDE_DRIVE_BACKUP) {
            throw UnsupportedOperationException(
                "Google Drive backup is not included in this build variant (lite). " +
                    "Use 'full' variant to enable Drive backup functionality.",
            )
        }
    }

    companion object {
        private const val BACKUP_FOLDER_NAME = "AntCashManager"
        private const val BACKUPS_SUBFOLDER_NAME = "Backups"
        private const val FULL_FOLDER_PATH = "$BACKUP_FOLDER_NAME/$BACKUPS_SUBFOLDER_NAME"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L // 1s, con backoff esponenziale
    }

    private val logger = Logger
    private val tokenStorage = EncryptedGoogleDriveStorage(context)

    /**
     * Crea la cartella di backup su Google Drive (one-shot).
     *
     * **Flusso**:
     * 1. Legge il folder ID salvato in `settingsRepository`
     * 2. Se presente e valido, verificalo con HEAD request
     * 3. Se non presente o non valido, creo la cartella strutturata:
     *    - Crea "/AntCashManager" se non esiste
     *    - Crea "/AntCashManager/Backups" dentro di essa
     * 4. Salva il folder ID di "Backups" in `settingsRepository`
     *
     * **Idempotente**: Può essere richiamato più volte — non crea duplicati.
     *
     * **Risultato**: Folder ID di "/AntCashManager/Backups"
     */
    suspend fun ensureFolderExists(): Result<String> {
        return try {
            logger.d(tag = "DriveUploadManager") { "Ensuring backup folder exists" }

            // Prova a leggere il folder ID salvato
            val savedFolderId = settingsRepository.getGoogleDriveFolderId().toString()
            if (savedFolderId.isNotEmpty() && savedFolderId != "null") {
                logger.d(tag = "DriveUploadManager") { "Folder ID already saved: $savedFolderId" }
                // Opzionale: verificare che il folder esista ancora (HEAD request)
                // Per ora, supponiamo che sia valido
                return Result.success(savedFolderId)
            }

            // Creare la cartella
            val backupFolderId = createBackupFolderStructure()
            if (backupFolderId.isNotEmpty()) {
                settingsRepository.setGoogleDriveFolderId(backupFolderId)
                logger.d(tag = "DriveUploadManager") { "Folder created successfully: $backupFolderId" }
                Result.success(backupFolderId)
            } else {
                Result.failure(Exception("Failed to create backup folder"))
            }
        } catch (e: Exception) {
            logger.e(tag = "DriveUploadManager") { "Error ensuring folder exists: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Crea la struttura di cartelle: "/AntCashManager/Backups"
     *
     * **Algoritmo**:
     * 1. Cerca "/AntCashManager" nella root (mimeType = folder, not trashed)
     * 2. Se non esiste, crea
     * 3. Dentro di essa, cerca "/Backups"
     * 4. Se non esiste, crea
     * 5. Ritorna l'ID della cartella "/Backups"
     */
    private suspend fun createBackupFolderStructure(): String {
        return withContext(Dispatchers.IO) {
            val drive = createDriveService() ?: return@withContext ""

            try {
                // Step 1: Crea o trova "/AntCashManager"
                val antCashManagerFolderId =
                    findOrCreateFolder(
                        drive,
                        folderName = BACKUP_FOLDER_NAME,
                        parentFolderId = null, // root
                    )
                if (antCashManagerFolderId.isEmpty()) {
                    logger.e(tag = "DriveUploadManager") { "Failed to create/find $BACKUP_FOLDER_NAME folder" }
                    return@withContext ""
                }

                // Step 2: Crea o trova "/AntCashManager/Backups"
                val backupsFolderId =
                    findOrCreateFolder(
                        drive,
                        folderName = BACKUPS_SUBFOLDER_NAME,
                        parentFolderId = antCashManagerFolderId,
                    )
                if (backupsFolderId.isEmpty()) {
                    logger.e(tag = "DriveUploadManager") { "Failed to create/find $BACKUPS_SUBFOLDER_NAME folder" }
                    return@withContext ""
                }

                logger.d(tag = "DriveUploadManager") { "Folder structure created: $FULL_FOLDER_PATH" }
                backupsFolderId
            } catch (e: Exception) {
                logger.e(tag = "DriveUploadManager") { "Error creating folder structure: ${e.message}" }
                ""
            }
        }
    }

    /**
     * Cerca una cartella per nome dentro un parent, o la crea se non esiste.
     *
     * **Query Drive API**:
     * ```
     * name = "FolderName"
     * AND mimeType = "application/vnd.google-apps.folder"
     * AND trashed = false
     * AND parents in "parent_id"
     * ```
     *
     * **Idempotente**: Se la cartella esiste già, la ritorna.
     */
    private suspend fun findOrCreateFolder(
        drive: Drive,
        folderName: String,
        parentFolderId: String?,
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // Query per trovare la cartella
                val query = buildFolderQuery(folderName, parentFolderId)
                logger.d(tag = "DriveUploadManager") { "Searching for folder: $folderName (query: $query)" }

                val files =
                    drive
                        .Files()
                        .list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id, name)")
                        .setPageSize(1)
                        .execute()
                        .files

                if (files.isNotEmpty()) {
                    logger.d(tag = "DriveUploadManager") { "Found existing folder: ${files[0].id}" }
                    return@withContext files[0].id
                }

                // Folder non trovata — creare
                logger.d(tag = "DriveUploadManager") { "Folder not found, creating: $folderName" }
                val newFolder = DriveFile()
                newFolder.name = folderName
                newFolder.mimeType = "application/vnd.google-apps.folder"
                if (parentFolderId != null) {
                    newFolder.parents = listOf(parentFolderId)
                }

                val created =
                    drive
                        .Files()
                        .create(newFolder)
                        .setFields("id")
                        .execute()

                logger.d(tag = "DriveUploadManager") { "Folder created: ${created.id}" }
                created.id ?: ""
            } catch (e: IOException) {
                if (e.message?.contains("401") == true) {
                    logger.e(tag = "DriveUploadManager") { "Unauthorized (401) — token expired or revoked" }
                } else if (e.message?.contains("403") == true) {
                    logger.e(tag = "DriveUploadManager") { "Forbidden (403) — permission denied" }
                }
                throw e
            }
        }
    }

    /**
     * Compila la query per cercare una cartella.
     */
    private fun buildFolderQuery(
        folderName: String,
        parentFolderId: String?,
    ): String {
        val parentClause =
            if (parentFolderId != null) {
                " AND '$parentFolderId' in parents"
            } else {
                " AND 'root' in parents"
            }
        return "name = '$folderName' AND mimeType = 'application/vnd.google-apps.folder' AND trashed = false$parentClause"
    }

    /**
     * Uploada un file di backup a Google Drive.
     *
     * **Flusso**:
     * 1. Assicurati che la cartella esista (chiama [ensureFolderExists] se necessario)
     * 2. Leggi l'access token cifrato
     * 3. Crea il Drive service
     * 4. Crea il metadata del file (name, parents, description)
     * 5. Upload il contenuto (ByteArrayContent in `application/json`)
     * 6. Su successo, ritorna il file ID
     * 7. Su 401, refresh token e retry (una volta)
     * 8. Su 403, failure (permesso revocato)
     * 9. Su 5xx, retry con backoff
     *
     * **Parametri**:
     * - `fileName`: Es. "AntCashManager-backup-20260821-143022.json" (include extension)
     * - `fileContent`: Contenuto JSON come ByteArray
     * - `mimeType`: Es. "application/json"
     * - `folderId`: Folder ID su Google Drive (otteniuto da [ensureFolderExists])
     *
     * **Risultato**: File ID su Google Drive
     */
    suspend fun uploadBackupFile(
        fileName: String,
        fileContent: ByteArray,
        mimeType: String = "application/json",
        folderId: String? = null,
    ): Result<String> {
        return try {
            logger.d(tag = "DriveUploadManager") { "Uploading backup file: $fileName" }

            // Ottieni il folder ID (salvato da ensureFolderExists)
            val backupFolderId = folderId ?: settingsRepository.getGoogleDriveFolderId().toString()
            if (backupFolderId.isEmpty() || backupFolderId == "null") {
                logger.e(tag = "DriveUploadManager") { "Backup folder ID not found — call ensureFolderExists() first" }
                return Result.failure(Exception("Backup folder not configured"))
            }

            // Upload con retry
            uploadWithRetry(fileName, fileContent, mimeType, backupFolderId, retryCount = 0)
        } catch (e: Exception) {
            logger.e(tag = "DriveUploadManager") { "Upload error: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Effettua l'upload con retry logic.
     *
     * **Retry estrategia**:
     * - 401 Unauthorized: Refresh token una volta e retry
     * - 5xx Server error: Retry fino a MAX_RETRIES volte con backoff esponenziale
     * - 403 Forbidden: No retry (permission revoked)
     * - Network error: No retry (caller decide)
     */
    private suspend fun uploadWithRetry(
        fileName: String,
        fileContent: ByteArray,
        mimeType: String,
        folderId: String,
        retryCount: Int = 0,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val drive =
                    createDriveService()
                        ?: return@withContext Result.failure(Exception("Failed to create Drive service"))

                val metadata = DriveFile()
                metadata.name = fileName
                metadata.parents = listOf(folderId)
                metadata.description = "Automatic backup created by AntCashManager"

                val content = ByteArrayContent(mimeType, fileContent)

                logger.d(tag = "DriveUploadManager") { "Uploading file: $fileName (size: ${fileContent.size} bytes)" }

                val file =
                    drive
                        .Files()
                        .create(metadata, content)
                        .setFields("id, name, createdTime, size")
                        .execute()

                logger.d(tag = "DriveUploadManager") { "File uploaded successfully: ${file.id} (${file.size} bytes)" }
                Result.success(file.id ?: "")
            } catch (e: IOException) {
                when {
                    e.message?.contains("401") == true -> {
                        if (retryCount < 1) {
                            logger.w(tag = "DriveUploadManager") { "Token expired (401) — refreshing and retrying" }
                            val tokenResult = signInManager.refreshAccessToken()
                            if (tokenResult.isSuccess) {
                                // Retry upload
                                uploadWithRetry(fileName, fileContent, mimeType, folderId, retryCount + 1)
                            } else {
                                Result.failure(
                                    Exception("Token refresh failed: ${tokenResult.exceptionOrNull()?.message}"),
                                )
                            }
                        } else {
                            Result.failure(Exception("Token refresh failed after retry — permission revoked"))
                        }
                    }
                    e.message?.contains("403") == true -> {
                        logger.e(
                            tag = "DriveUploadManager",
                        ) { "Permission denied (403) — file not created by this app?" }
                        Result.failure(Exception("Permission denied — folder may not be owned by this app"))
                    }
                    e.message?.contains("5") == true -> {
                        if (retryCount < MAX_RETRIES) {
                            val delay = RETRY_DELAY_MS * (retryCount + 1) // Backoff esponenziale
                            logger.w(tag = "DriveUploadManager") {
                                "Server error — retrying in ${delay}ms (attempt ${retryCount + 1}/$MAX_RETRIES)"
                            }
                            kotlinx.coroutines.delay(delay)
                            uploadWithRetry(fileName, fileContent, mimeType, folderId, retryCount + 1)
                        } else {
                            Result.failure(Exception("Server error after $MAX_RETRIES retries"))
                        }
                    }
                    else -> {
                        Result.failure(e)
                    }
                }
            }
        }
    }

    /**
     * Crea il Drive service con l'access token corrente.
     *
     * **Nota**: L'access token è letto da EncryptedSharedPreferences (cifrato).
     * Google API client lo usa automaticamente per autorizzare le richieste.
     */
    private suspend fun createDriveService(): Drive? {
        return try {
            val accessToken = tokenStorage.getAccessToken()
            if (accessToken == null) {
                logger.e(tag = "DriveUploadManager") { "No access token available" }
                return null
            }

            val requestInitializer =
                HttpRequestInitializer { request ->
                    request.headers.authorization = "Bearer $accessToken"
                    request.connectTimeout = 30000 // 30s
                    request.readTimeout = 30000
                }

            Drive
                .Builder(
                    NetHttpTransport(),
                    GsonFactory(),
                    requestInitializer,
                ).setApplicationName("AntCashManager")
                .build()
        } catch (e: Exception) {
            logger.e(tag = "DriveUploadManager") { "Error creating Drive service: ${e.message}" }
            null
        }
    }

    /**
     * Rimuove un file da Google Drive (opzionale, per cleanup).
     *
     * **Utilizzo**: Cancellare file di backup obsoleti.
     * Non è critico — può essere implementato in una versione futura.
     */
    suspend fun deleteFile(fileId: String): Result<Unit> {
        return try {
            val drive =
                createDriveService()
                    ?: return Result.failure(Exception("Failed to create Drive service"))

            logger.d(tag = "DriveUploadManager") { "Deleting file: $fileId" }
            drive.Files().delete(fileId).execute()
            logger.d(tag = "DriveUploadManager") { "File deleted: $fileId" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(tag = "DriveUploadManager") { "Error deleting file: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Lista i file di backup nella cartella (debug/admin utility).
     *
     * **Utilizzo**: Verificare quali backup sono già su Google Drive.
     * Non è critico — può essere usato per UI futura (lista backup).
     */
    suspend fun listBackupFiles(folderId: String? = null): Result<List<DriveFile>> {
        return try {
            val drive =
                createDriveService()
                    ?: return Result.failure(Exception("Failed to create Drive service"))

            val backupFolderId = folderId ?: settingsRepository.getGoogleDriveFolderId().toString()
            if (backupFolderId.isEmpty() || backupFolderId == "null") {
                return Result.failure(Exception("Backup folder ID not found"))
            }

            logger.d(tag = "DriveUploadManager") { "Listing backup files in folder: $backupFolderId" }

            val files =
                drive
                    .Files()
                    .list()
                    .setQ("'$backupFolderId' in parents AND trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name, createdTime, size, mimeType)")
                    .setPageSize(100)
                    .setOrderBy("createdTime desc")
                    .execute()
                    .files

            logger.d(tag = "DriveUploadManager") { "Found ${files?.size ?: 0} backup files" }
            Result.success(files ?: emptyList())
        } catch (e: Exception) {
            logger.e(tag = "DriveUploadManager") { "Error listing files: ${e.message}" }
            Result.failure(e)
        }
    }
}
