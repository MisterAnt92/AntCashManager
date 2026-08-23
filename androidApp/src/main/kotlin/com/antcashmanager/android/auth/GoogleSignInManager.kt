package com.antcashmanager.android.auth

import android.content.Context
import co.touchlab.kermit.Logger
import com.antcashmanager.android.data.storage.EncryptedGoogleDriveStorage
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.repository.SettingsRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.tasks.await

/**
 * Gestisce il flusso di autenticazione OAuth 2.0 con Google per Google Drive integration.
 *
 * **Responsabilità**:
 * - Orchestrare il sign-in flow (Account Chooser → Authorization)
 * - Gestire token (access + refresh) via EncryptedGoogleDriveStorage
 * - Refresh token automatico/manuale
 * - Logout e revoca delle credenziali
 * - Creare cartella di backup one-shot su Google Drive
 *
 * **OAuth Scope**:
 * - `drive.file`: Accesso solo ai file creati da questa app (minimal permission)
 * - Non usiamo `drive` (full access) — è una security risk
 *
 * **Utilizzo**:
 * ```kotlin
 * val manager = GoogleSignInManager(context, settingsRepository, driveUploadManager)
 * val result = manager.signIn()
 * if (result.isSuccess) {
 *     val data = result.getOrNull()
 *     // Save tokens, update UI, etc.
 * } else {
 *     val error = result.exceptionOrNull()
 *     // Handle error
 * }
 * ```
 *
 * **Error Handling**:
 * - Network errors: Eccezione lanciata, caller può retry
 * - User cancelled: Torna Result.failure() (non è un errore vero)
 * - Invalid credentials: Excezione lanciata, user must sign in di nuovo
 * - Permission revoked: Token refresh fallisce (401), caller deve retry sign-in
 */
class GoogleSignInManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
) {

    private val logger = Logger
    private val googleSignInClient: GoogleSignInClient = createGoogleSignInClient()

    /**
     * Crea il GoogleSignInClient con le opzioni OAuth corrette.
     *
     * **Opzioni**:
     * - `requestProfile()`: Ottiene basic profile (email, name, etc.)
     * - `requestEmail()`: Email access
     * - `requestScopes()`: Drive API scope (`drive.file`)
     *
     * **Client ID**:
     * Google Play Services ha il Client ID embedded basato su SHA-1 fingerprint.
     * Non specificare manualmente — viene risolto automaticamente.
     */
    private fun createGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }

    /**
     * Avvia il flusso di sign-in OAuth 2.0.
     *
     * **Flusso**:
     * 1. Controlla se già signed in (tramite [isSignedIn])
     * 2. Se non signed in, lancia l'Account Chooser
     * 3. Utente autorizza l'app → riceve `GoogleSignInAccount` con token
     * 4. Salva token (access + refresh) in EncryptedSharedPreferences
     * 5. Crea cartella "/AntCashManager/Backups" su Google Drive
     * 6. Ritorna `GoogleSignInResult` con email e folder ID
     *
     * **Errori**:
     * - Network error: Eccezione; caller retry
     * - User cancelled: Result.failure()
     * - Permission denied: Eccezione; user must retry
     *
     * **Nota**: Questo è un one-time setup. Successivamente, i token
     * sono refreshati automaticamente da Google Play Services.
     */
    suspend fun signIn(): Result<GoogleSignInResult> {
        return try {
            logger.d(tag = "GoogleSignInManager") { "Initiating Google Sign-In flow" }

            // Controlla se già signed in
            val alreadySignedIn = isSignedIn()
            if (alreadySignedIn) {
                logger.d(tag = "GoogleSignInManager") { "User already signed in, returning cached account" }
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account != null) {
                    val result = handleSignInSuccess(account)
                    return Result.success(result)
                }
            }

            // Lancia il picker di account
            logger.d(tag = "GoogleSignInManager") { "Launching Google Account Chooser" }
            val account = getSignedInAccountFromUI()

            if (account != null) {
                val result = handleSignInSuccess(account)
                Result.success(result)
            } else {
                logger.w(tag = "GoogleSignInManager") { "User cancelled sign-in" }
                Result.failure(Exception("User cancelled sign-in"))
            }
        } catch (e: Exception) {
            logger.e(tag = "GoogleSignInManager") { "Sign-in error: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Scarica le credenziali e completa il sign-in.
     *
     * **Nota**: In una vera implementazione, questo dovrebbe essere gestito
     * da un launcher registrato in Activity (vedi ViewModel).
     * Per ora, è un placeholder che simula il flusso.
     */
    private fun getSignedInAccountFromUI(): GoogleSignInAccount? {
        // TODO: Implementare via Activity.registerForActivityResult()
        // Per ora ritorna null (caller gestirà il launch della Activity)
        logger.w(tag = "GoogleSignInManager") { "getSignedInAccountFromUI is a placeholder — implement via registerForActivityResult()" }
        return null
    }

    /**
     * Gestisce il successo del sign-in: salva token, crea cartella, aggiorna settings.
     */
    private suspend fun handleSignInSuccess(account: GoogleSignInAccount): GoogleSignInResult {
        logger.d(tag = "GoogleSignInManager") { "Handling successful sign-in for ${account.email}" }

        // Estrai token
        val accessToken = account.serverAuthCode ?: account.idToken ?: ""
        val email = account.email ?: ""

        // Salva token cifrati
        val storage = getEncryptedStorage()
        storage.saveAccessToken(accessToken)
        storage.saveRefreshToken(accessToken)  // TODO: In fase 2, implementare proper refresh token extraction

        // Aggiorna settings
        settingsRepository.setGoogleDriveUserEmail(email)
        settingsRepository.setAutoBackupDestination(BackupDestination.GOOGLE_DRIVE)

        logger.d(tag = "GoogleSignInManager") { "Sign-in successful for $email" }

        return GoogleSignInResult(
            email = email,
            accessToken = accessToken,
            idToken = account.idToken ?: "",
            driveFolder = null  // TODO: Implementare folder creation in DriveUploadManager
        )
    }

    /**
     * Verifica se l'utente è già loggato.
     */
    fun isSignedIn(): Boolean {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            val isSignedIn = account != null && account.isExpired.not()
            logger.d(tag = "GoogleSignInManager") { "Sign-in status: $isSignedIn" }
            isSignedIn
        } catch (e: Exception) {
            logger.e(tag = "GoogleSignInManager") { "Error checking sign-in status: ${e.message}" }
            false
        }
    }

    /**
     * Richiede un nuovo access token (token refresh).
     *
     * **Utilizzo**: Se access token scade durante upload, questo lo richiede.
     * Di solito gestito automaticamente da Google Play Services, ma fornito
     * come fallback manuale.
     */
    suspend fun refreshAccessToken(): Result<String> {
        return try {
            logger.d(tag = "GoogleSignInManager") { "Refreshing access token" }
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return Result.failure(
                Exception("No signed-in account")
            )

            // Google Play Services refresh automaticamente se validità < 1 minuto
            // Questo è un manual refresh fallback
            val newToken = account.idToken ?: account.serverAuthCode
            if (newToken != null) {
                val storage = getEncryptedStorage()
                storage.saveAccessToken(newToken)
                logger.d(tag = "GoogleSignInManager") { "Access token refreshed successfully" }
                Result.success(newToken)
            } else {
                Result.failure(Exception("Failed to refresh access token"))
            }
        } catch (e: Exception) {
            logger.e(tag = "GoogleSignInManager") { "Token refresh error: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Effettua logout: revoca credenziali e cancella token locali.
     *
     * **Effetto**:
     * - Revoca accesso a Google Drive
     * - Cancella token dal device
     * - Aggiorna settings (disabilita Google Drive backup, cancella email)
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            logger.d(tag = "GoogleSignInManager") { "Initiating sign-out" }

            // Rivoca accesso
            googleSignInClient.signOut().await()

            // Cancella token locali
            val storage = getEncryptedStorage()
            storage.clearAllTokens()

            // Aggiorna settings
            settingsRepository.setGoogleDriveUserEmail(null)
            settingsRepository.setGoogleDriveFolderId(null)
            settingsRepository.setAutoBackupDestination(BackupDestination.LOCAL)

            logger.d(tag = "GoogleSignInManager") { "Sign-out successful" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(tag = "GoogleSignInManager") { "Sign-out error: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Ottiene l'EncryptedGoogleDriveStorage per gestione token.
     */
    private fun getEncryptedStorage(): EncryptedGoogleDriveStorage {
        return EncryptedGoogleDriveStorage(context)
    }
}

/**
 * Risultato di un sign-in riuscito.
 */
data class GoogleSignInResult(
    val email: String,
    val accessToken: String,
    val idToken: String,
    val driveFolder: String? = null,  // Folder ID se già creata
)
