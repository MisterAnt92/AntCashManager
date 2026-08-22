package com.antcashmanager.android.work

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.drive.DriveUploadManager
import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.android.ui.screen.settings.dataManagement.SettingsDataConstant
import com.antcashmanager.android.auth.GoogleSignInManager
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.repository.SettingsRepository
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Worker per eseguire il backup automatico settimanale.
 *
 * Logica:
 * 1. Legge enabled flag, destination (LOCAL/GOOGLE_DRIVE) da [SettingsRepository]
 * 2. Se disabilitato, ritorna success (no-op)
 * 3. Chiama [BackupService.createBackup()] per generare JSON
 * 4. Applica cifratura AES-GCM se enabled
 * 5. Se destinazione LOCAL: scrive nel DocumentFile (SAF tree URI)
 *    Se destinazione GOOGLE_DRIVE: uploda via [DriveUploadManager]
 * 6. Aggiorna lastBackupTimestamp su successo
 * 7. Mostra notifica su fallimento permanente
 *
 * Retry: IO errors (LOCAL) o network errors (Google Drive) → retry
 * Fallimento: permesso revocato, token scaduto, cartella non trovata → failure con notifica
 */
class AutoBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val settingsRepository: SettingsRepository by inject()
    private val backupService: BackupService by inject()
    private val googleSignInManager: GoogleSignInManager by inject()
    private val driveUploadManager: DriveUploadManager by inject()

    override suspend fun doWork(): Result {
        return try {
            Logger.d(tag = "AutoBackupWorker") { "Starting automatic backup..." }

            // ── 1. Leggi stato from SettingsRepository ──
            val enabled = settingsRepository.getAutoBackupEnabled().first()
            val destination = settingsRepository.getAutoBackupDestination().first()

            if (!enabled) {
                Logger.d(tag = "AutoBackupWorker") { "Automatic backup disabled, skipping" }
                return Result.success()
            }

            Logger.d(tag = "AutoBackupWorker") { "Backup destination: $destination" }

            // ── 2. Genera JSON di backup ──
            val backupResult = backupService.createBackup()
            val backupJson = backupResult.getOrElse { error ->
                Logger.e(tag = "AutoBackupWorker") { "Backup generation failed: $error" }
                AutoBackupNotifier.notifyFailure(applicationContext)
                return Result.failure()
            }

            // ── 3. Applica cifratura se necessaria ──
            val dataEncryptionEnabled = settingsRepository.getDataEncryptionEnabled().first()
            val contentToWrite = if (dataEncryptionEnabled) {
                try {
                    BackupPayloadCipher.encrypt(backupJson)
                } catch (e: Exception) {
                    Logger.e(tag = "AutoBackupWorker") { "Encryption failed: ${e.message}" }
                    AutoBackupNotifier.notifyFailure(applicationContext)
                    return Result.failure()
                }
            } else {
                backupJson
            }

            // Genera nome file e timestamp
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val fileName = "${SettingsDataConstant.BACKUP_FILE_PREFIX}${dateFormat.format(Date(timestamp))}${SettingsDataConstant.BACKUP_FILE_SUFFIX}"

            // ── 4. Scrivi a destinazione (LOCAL o GOOGLE_DRIVE) ──
            val writeResult = try {
                when (destination) {
                    BackupDestination.LOCAL -> writeToLocalFolder(fileName, contentToWrite, dataEncryptionEnabled)
                    BackupDestination.GOOGLE_DRIVE -> writeToGoogleDrive(fileName, contentToWrite)
                }
            } catch (e: IOException) {
                Logger.e(tag = "AutoBackupWorker") { "IO/Network error during backup write — will retry" }
                return Result.retry()
            }

            if (!writeResult) {
                return Result.failure()
            }

            // ── 5. Aggiorna lastBackupTimestamp ──
            try {
                settingsRepository.setLastBackupTimestamp(timestamp)
                Logger.d(tag = "AutoBackupWorker") { "Backup successful, timestamp updated" }
                Result.success()
            } catch (e: Exception) {
                Logger.e(tag = "AutoBackupWorker") { "Error updating timestamp: ${e.message}" }
                AutoBackupNotifier.notifyFailure(applicationContext)
                Result.failure()
            }
        } catch (e: Exception) {
            Logger.e(tag = "AutoBackupWorker") { "Unexpected error in AutoBackupWorker: ${e.message}" }
            AutoBackupNotifier.notifyFailure(applicationContext)
            Result.failure()
        }
    }

    /**
     * Scrivi il backup nel filesystem locale (via SAF DocumentFile).
     *
     * @return true se successo, false se fallimento
     */
    private suspend fun writeToLocalFolder(
        fileName: String,
        content: String,
        isEncrypted: Boolean,
    ): Boolean {
        return try {
            val folderUri = settingsRepository.getAutoBackupFolderUri().first()

            if (folderUri.isNullOrBlank()) {
                Logger.w(tag = "AutoBackupWorker") { "No local folder URI configured, cannot backup" }
                AutoBackupNotifier.notifyFailure(applicationContext)
                return false
            }

            val parentUri = Uri.parse(folderUri)
            val parentDir = DocumentFile.fromTreeUri(applicationContext, parentUri)

            if (parentDir == null || !parentDir.canWrite()) {
                Logger.e(tag = "AutoBackupWorker") { "Cannot write to backup folder (permission revoked?)" }
                AutoBackupNotifier.notifyFailure(applicationContext)
                return false
            }

            // Crea il file nel DocumentFile
            val backupFile = parentDir.createFile(
                if (isEncrypted) "application/octet-stream" else "application/json",
                fileName.substringBeforeLast(".")
            )

            if (backupFile == null) {
                Logger.e(tag = "AutoBackupWorker") { "Failed to create backup file" }
                AutoBackupNotifier.notifyFailure(applicationContext)
                return false
            }

            // Scrivi il contenuto
            try {
                applicationContext.contentResolver.openOutputStream(backupFile.uri)?.use { output ->
                    output.write(content.toByteArray(Charsets.UTF_8))
                    output.flush()
                }
                Logger.d(tag = "AutoBackupWorker") { "Backup file written locally: ${backupFile.name}" }
                true
            } catch (e: IOException) {
                Logger.e(tag = "AutoBackupWorker") { "IOException writing backup: ${e.message}" }
                // IO error → riprovare
                throw e
            } catch (e: Exception) {
                Logger.e(tag = "AutoBackupWorker") { "Error writing backup: ${e.message}" }
                AutoBackupNotifier.notifyFailure(applicationContext)
                false
            }
        } catch (e: IOException) {
            Logger.e(tag = "AutoBackupWorker") { "IO error writing to local folder — will retry" }
            throw e  // Re-throw per trigger Result.retry() nel caller
        }
    }

    /**
     * Uploda il backup a Google Drive.
     *
     * @return true se successo, false se fallimento
     */
    private suspend fun writeToGoogleDrive(
        fileName: String,
        content: String,
    ): Boolean {
        return try {
            Logger.d(tag = "AutoBackupWorker") { "Uploading backup to Google Drive: $fileName" }

            // Step 1: Assicurati che la cartella esista
            val folderResult = driveUploadManager.ensureFolderExists()
            if (!folderResult.isSuccess) {
                Logger.e(tag = "AutoBackupWorker") { "Failed to ensure Drive folder: ${folderResult.exceptionOrNull()?.message}" }
                AutoBackupNotifier.notifyFailure(applicationContext)
                return false
            }

            // Step 2: Upload il file
            val uploadResult = driveUploadManager.uploadBackupFile(
                fileName = fileName,
                fileContent = content.toByteArray(Charsets.UTF_8),
                mimeType = "application/json"  // Sempre JSON (cifratura opzionale è gestita a livello di contenuto)
            )

            if (uploadResult.isSuccess) {
                Logger.d(tag = "AutoBackupWorker") { "Backup uploaded to Google Drive: ${uploadResult.getOrNull()}" }
                true
            } else {
                val error = uploadResult.exceptionOrNull()
                Logger.e(tag = "AutoBackupWorker") { "Upload to Google Drive failed: ${error?.message}" }

                // Se è un errore 401 (token scaduto), sarà gestito da DriveUploadManager con retry automatico
                // Se è un errore permanente, mostra notifica
                when {
                    error?.message?.contains("401") == true -> {
                        Logger.w(tag = "AutoBackupWorker") { "Token expired — will retry after refresh" }
                        throw error  // Re-throw per trigger Result.retry()
                    }
                    error?.message?.contains("403") == true -> {
                        Logger.e(tag = "AutoBackupWorker") { "Permission denied — user must sign in again" }
                        AutoBackupNotifier.notifyFailure(applicationContext)
                        false
                    }
                    else -> {
                        AutoBackupNotifier.notifyFailure(applicationContext)
                        false
                    }
                }
            }
        } catch (e: IOException) {
            Logger.e(tag = "AutoBackupWorker") { "Network error uploading to Google Drive — will retry" }
            throw e  // Re-throw per trigger Result.retry()
        } catch (e: Exception) {
            Logger.e(tag = "AutoBackupWorker") { "Error uploading to Google Drive: ${e.message}" }
            AutoBackupNotifier.notifyFailure(applicationContext)
            false
        }
    }

}
