package com.antcashmanager.android.ui.screen.settings.dataManagement

import android.os.Build
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.analytics.tracker.ErrorTracker
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.auth.GoogleSignInManager
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.android.work.AutoBackupScheduler
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.model.None
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel per la sotto-sezione Gestione Dati.
 *
 * **Backup Automatico (Phase 1 & 2)**:
 * - Gestisce l'abilitazione/disabilitazione del backup automatico
 * - Gestisce la selezione della cartella locale (SAF picker)
 * - Gestisce il sign-in OAuth con Google per Google Drive backup
 * - Coordina il cambio di destinazione (LOCAL ↔ GOOGLE_DRIVE)
 */
class SettingsDataViewModel(
    settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase,
    private val backupService: BackupService,
    private val autoBackupScheduler: AutoBackupScheduler,
    private val googleSignInManager: GoogleSignInManager,
    private val performanceTracker: PerformanceTracker,
    private val errorTracker: ErrorTracker,
) : BaseViewModel<None>() {
    private val settingsRepositoryRef = settingsRepository

    private val _state = MutableStateFlow(SettingsDataState())
    val state: StateFlow<SettingsDataState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepositoryRef.getDataEncryptionEnabled().collect { enabled ->
                _state.update { it.copy(dataEncryptionEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getLastBackupTimestamp().collect { timestamp ->
                _state.update { it.copy(lastBackupTimestamp = timestamp) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getLastRestoreTimestamp().collect { timestamp ->
                _state.update { it.copy(lastRestoreTimestamp = timestamp) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getSuggestionsEnabled().collect { enabled ->
                _state.update { it.copy(suggestionsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getAutoBackupEnabled().collect { enabled ->
                _state.update { it.copy(autoBackupEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getAutoBackupFolderUri().collect { uri ->
                _state.update { it.copy(autoBackupFolderUri = uri) }
            }
        }
        // ── Phase 2: Google Drive Backup ──
        viewModelScope.launch {
            settingsRepositoryRef.getAutoBackupDestination().collect { destination ->
                _state.update { it.copy(autoBackupDestination = destination) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getGoogleDriveUserEmail().collect { email ->
                _state.update { it.copy(googleDriveUserEmail = email, isGoogleDriveSignedIn = email != null) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getGoogleDriveFolderId().collect { folderId ->
                _state.update { it.copy(googleDriveFolderId = folderId) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getGoogleDriveFolderName().collect { folderName ->
                _state.update { it.copy(googleDriveFolderName = folderName) }
            }
        }
    }

    fun setDataEncryptionEnabled(enabled: Boolean) {
        logDebug("Setting data encryption enabled: $enabled")
        viewModelScope.launch {
            try {
                // Apply encryption flag
                settingsRepositoryRef.setDataEncryptionEnabled(enabled)

                // Re-encrypt/decrypt all existing transactions
                // CRITICAL: Must run AFTER flag is applied, so cipher respects new state
                performReEncryption()
            } catch (e: Exception) {
                logError("Failed to toggle encryption: ${e.message}")
                // Rollback the flag on error
                try {
                    settingsRepositoryRef.setDataEncryptionEnabled(!enabled)
                } catch (rollbackError: Exception) {
                    logError("CRITICAL: Failed to rollback encryption flag: ${rollbackError.message}")
                    errorTracker.trackError("encryption_toggle_rollback_failed", rollbackError)
                }
                errorTracker.trackError("encryption_toggle_failed", e)
            }
        }
    }

    private suspend fun performReEncryption() {
        try {
            val allTransactions = transactionRepository.getAllTransactions().first()
            if (allTransactions.isEmpty()) {
                logDebug("No transactions to re-encrypt")
                return
            }

            logDebug("Re-encrypting ${allTransactions.size} transactions...")
            var reencrypted = 0
            for (transaction in allTransactions) {
                try {
                    // Re-write each transaction: cipher will apply new encryption state
                    transactionRepository.updateTransaction(transaction)
                    reencrypted++
                } catch (e: Exception) {
                    logError("Failed to re-encrypt transaction ${transaction.id}: ${e.message}")
                    throw e  // Fail fast: abort re-encryption on first error
                }
            }

            logDebug("Successfully re-encrypted $reencrypted transactions")
        } catch (e: CancellationException) {
            throw e  // Propagate cancellation
        } catch (e: Exception) {
            logError("Re-encryption failed: ${e.message}")
            throw e  // Propagate to caller for rollback handling
        }
    }

    fun setSuggestionsEnabled(enabled: Boolean) {
        logDebug("Setting suggestions enabled: $enabled")
        viewModelScope.launch {
            settingsRepositoryRef.setSuggestionsEnabled(enabled)
        }
    }

    fun showDeleteSuggestionsDialog() {
        _state.update { it.copy(showDeleteSuggestionsDialog = true) }
    }

    fun dismissDeleteSuggestionsDialog() {
        _state.update { it.copy(showDeleteSuggestionsDialog = false) }
    }

    fun deleteAllSuggestions() {
        logDebug("Deleting all suggestions")
        viewModelScope.launch {
            settingsRepositoryRef.setSuggestionsClearedAt(System.currentTimeMillis())
            _state.update { it.copy(showDeleteSuggestionsDialog = false) }
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        logDebug("Setting auto backup enabled: $enabled")
        viewModelScope.launch {
            if (enabled) {
                // Enable: chiama il scheduler per avviare il backup settimanale
                autoBackupScheduler.schedule()
            } else {
                // Disable: cancella il lavoro schedulato
                autoBackupScheduler.cancel()
            }
            settingsRepositoryRef.setAutoBackupEnabled(enabled)
        }
    }

    fun onAutoBackupFolderSelected(uriString: String) {
        logDebug("Auto backup folder selected: $uriString")
        viewModelScope.launch {
            // Persisti URI, abilita backup, e schedula il lavoro
            settingsRepositoryRef.setAutoBackupFolderUri(uriString)
            settingsRepositoryRef.setAutoBackupEnabled(true)
            autoBackupScheduler.schedule()
        }
    }

    fun onAutoBackupFolderSelectionCancelled() {
        logDebug("Auto backup folder selection cancelled")
        // No-op: il toggle rimane OFF, niente è stato persistito
    }

    fun showDeleteConfirmDialog() {
        _state.update { it.copy(showDeleteConfirmDialog = true) }
    }

    fun dismissDeleteConfirmDialog() {
        _state.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun dismissDeleteSuccessDialog() {
        _state.update {
            it.copy(
                showDeleteSuccessDialog = false,
                deleteResult = DeleteResult.Idle,
            )
        }
    }

    fun showResetPreferencesDialog() {
        _state.update { it.copy(showResetPreferencesDialog = true) }
    }

    fun dismissResetPreferencesDialog() {
        _state.update { it.copy(showResetPreferencesDialog = false) }
    }

    fun dismissBackupSuccessDialog() {
        _state.update {
            it.copy(
                showBackupSuccessDialog = false,
                backupResult = BackupResult.Idle,
            )
        }
    }

    fun dismissBackupErrorDialog() {
        _state.update {
            it.copy(
                showBackupErrorDialog = false,
                backupResult = BackupResult.Idle,
                backupErrorMessage = "",
            )
        }
    }

    fun dismissRestoreSuccessDialog() {
        _state.update {
            it.copy(
                showRestoreSuccessDialog = false,
                restoreResult = RestoreOperationResult.Idle,
                restoreSuccessInfo = null,
            )
        }
    }

    fun dismissRestoreErrorDialog() {
        _state.update {
            it.copy(
                showRestoreErrorDialog = false,
                restoreResult = RestoreOperationResult.Idle,
                restoreErrorMessage = "",
            )
        }
    }

    fun onBackupFileSaved() {
        _state.update {
            it.copy(
                pendingBackupData = null,
                pendingBackupFileName = null,
                showBackupSuccessDialog = true,
            )
        }
        viewModelScope.launch {
            settingsRepositoryRef.setLastBackupTimestamp(System.currentTimeMillis())
        }
    }

    fun clearPendingBackupRequest() {
        _state.update {
            it.copy(
                pendingBackupData = null,
                pendingBackupFileName = null,
            )
        }
    }

    fun onBackupFileSaveError(message: String) {
        _state.update {
            it.copy(
                pendingBackupData = null,
                pendingBackupFileName = null,
                backupResult = BackupResult.Error(message),
                backupErrorMessage = message,
                showBackupErrorDialog = true,
            )
        }
    }

    fun onRestoreFileReadError(message: String) {
        _state.update {
            it.copy(
                restoreResult = RestoreOperationResult.Error(message),
                restoreErrorMessage = message,
                showRestoreErrorDialog = true,
            )
        }
    }

    fun deleteAllData() {
        logDebug("Deleting all data")
        viewModelScope.launch {
            try {
                deleteAllTransactionsUseCase()
                    .onSuccess {
                        categoryRepository.deleteAllCategories()
                        _state.update {
                            it.copy(
                                deleteResult = DeleteResult.Success,
                                showDeleteConfirmDialog = false,
                                showDeleteSuccessDialog = true,
                            )
                        }
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Error deleting data: ${error.message}", error)
                        _state.update {
                            it.copy(
                                deleteResult = DeleteResult.Error(
                                    error.message ?: SettingsDataConstant.UNKNOWN_ERROR,
                                ),
                                showDeleteConfirmDialog = false,
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logError("Error deleting data: ${error.message}", error)
                _state.update {
                    it.copy(
                        deleteResult = DeleteResult.Error(
                            error.message ?: SettingsDataConstant.UNKNOWN_ERROR,
                        ),
                        showDeleteConfirmDialog = false,
                    )
                }
            }
        }
    }

    /**
     * Garantisce che [block] resti "percepibile" almeno per [SettingsDataConstant.MIN_LOADING_DURATION_MS]:
     * se l'operazione reale è più veloce, attende la differenza prima di restituire il risultato,
     * cosicché la dialog di caricamento non lampeggi troppo velocemente per essere notata.
     */
    private suspend fun <T> withMinimumLoadingDuration(block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val remaining =
            SettingsDataConstant.MIN_LOADING_DURATION_MS - (System.currentTimeMillis() - startTime)
        if (remaining > 0) delay(remaining)
        return result
    }

    fun createBackup() {
        logDebug("Creating backup")
        _state.update { it.copy(backupResult = BackupResult.Loading) }
        viewModelScope.launch {
            val backupStartTime = System.currentTimeMillis()
            withMinimumLoadingDuration { backupService.createBackup() }
                .onSuccess { jsonString ->
                    val duration = System.currentTimeMillis() - backupStartTime
                    val fileSizeMb = jsonString.length.toDouble() / (1024 * 1024)
                    performanceTracker.trackBackupRestoreDuration(duration, fileSizeMb, operation = "backup")

                    val payloadToPersist = if (_state.value.dataEncryptionEnabled) {
                        runCatching { BackupPayloadCipher.encrypt(jsonString) }
                            .getOrElse { error ->
                                val message = error.message ?: SettingsDataConstant.UNKNOWN_ERROR
                                _state.update {
                                    it.copy(
                                        backupResult = BackupResult.Error(message),
                                        backupErrorMessage = message,
                                        showBackupErrorDialog = true,
                                    )
                                }
                                return@onSuccess
                            }
                    } else {
                        jsonString
                    }

                    val timestamp = SimpleDateFormat(
                        SettingsDataConstant.BACKUP_TIMESTAMP_PATTERN,
                        Locale.getDefault(),
                    )
                        .format(Date())
                    _state.update {
                        it.copy(
                            backupResult = BackupResult.Success,
                            pendingBackupData = payloadToPersist,
                            pendingBackupFileName =
                                "${SettingsDataConstant.BACKUP_FILE_PREFIX}$timestamp${SettingsDataConstant.BACKUP_FILE_SUFFIX}",
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val duration = System.currentTimeMillis() - backupStartTime
                    val errorCode = when {
                        error.message?.contains("permission") == true -> "permission_denied"
                        error.message?.contains("storage") == true -> "storage_error"
                        error.message?.contains("encrypt") == true -> "encryption_error"
                        else -> "backup_failed"
                    }
                    errorTracker.trackSyncError("backup_local", errorCode)
                    val message = error.message ?: SettingsDataConstant.UNKNOWN_ERROR
                    _state.update {
                        it.copy(
                            backupResult = BackupResult.Error(message),
                            backupErrorMessage = message,
                            showBackupErrorDialog = true,
                        )
                    }
                }
        }
    }

    fun restoreBackup(jsonString: String) {
        logDebug("Restoring backup")
        _state.update { it.copy(restoreResult = RestoreOperationResult.Loading) }
        val restoreStartTime = System.currentTimeMillis()

        val payloadToRestore = if (BackupPayloadCipher.isEncryptedPayload(jsonString)) {
            runCatching { BackupPayloadCipher.decrypt(jsonString) }
                .onFailure { error ->
                    // ✅ Log API level info for diagnostics
                    Logger.e(tag = "SettingsDataViewModel") {
                        "Decryption failed on API ${Build.VERSION.SDK_INT}: ${error.message}"
                    }
                    // Track decryption error
                    val errorCode = when (error) {
                        is javax.crypto.BadPaddingException -> "wrong_password"
                        is java.security.InvalidKeyException -> "invalid_key"
                        is IllegalArgumentException -> "invalid_payload"
                        else -> "decryption_failed"
                    }
                    errorTracker.trackSyncError("restore_local", errorCode)
                }
                .getOrElse { error ->
                    // ✅ Enhanced error message mapping
                    val message = when (error) {
                        is IllegalArgumentException -> {
                            when {
                                error.message?.contains("another device") == true -> {
                                    "Questo backup è stato creato su un altro dispositivo. " +
                                        "I backup con crittografia device-bound non possono essere ripristinati su telefoni diversi. " +
                                        "Usa backup con password protetta (v1.8+) per trasferimenti tra dispositivi."
                                }
                                error.message?.contains("password-based") == true -> {
                                    "Questo backup richiede una password. Aggiorna all'app v1.8+ per ripristinare backup con password."
                                }
                                else -> "Payload di backup invalido o danneggiato"
                            }
                        }
                        is javax.crypto.BadPaddingException -> {
                            "Chiave di crittografia non corrisponde - password errata?"
                        }
                        is java.security.InvalidKeyException -> {
                            "Chiave di crittografia non valida"
                        }
                        else -> {
                            error.message ?: SettingsDataConstant.UNKNOWN_ERROR
                        }
                    }
                    _state.update {
                        it.copy(
                            restoreResult = RestoreOperationResult.Error(message),
                            restoreErrorMessage = message,
                            showRestoreErrorDialog = true,
                        )
                    }
                    return
                }
        } else {
            jsonString
        }

        viewModelScope.launch {
            withMinimumLoadingDuration { backupService.restoreBackup(payloadToRestore) }
                .onSuccess { result ->
                    val duration = System.currentTimeMillis() - restoreStartTime
                    val fileSizeMb = payloadToRestore.length.toDouble() / (1024 * 1024)
                    performanceTracker.trackBackupRestoreDuration(duration, fileSizeMb, operation = "restore")

                    _state.update {
                        it.copy(
                            restoreResult = RestoreOperationResult.Success(
                                transactions = result.transactionsRestored,
                                categories = result.categoriesRestored,
                            ),
                            restoreSuccessInfo = RestoreSuccessInfo(
                                transactions = result.transactionsRestored,
                                categories = result.categoriesRestored,
                            ),
                            showRestoreSuccessDialog = true,
                        )
                    }
                    settingsRepositoryRef.setLastRestoreTimestamp(System.currentTimeMillis())
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val errorCode = when {
                        error.message?.contains("permission") == true -> "permission_denied"
                        error.message?.contains("invalid") == true -> "invalid_data"
                        error.message?.contains("schema") == true -> "schema_mismatch"
                        error.message?.contains("database") == true -> "database_error"
                        else -> "restore_failed"
                    }
                    errorTracker.trackSyncError("restore_local", errorCode)
                    val message = error.message ?: SettingsDataConstant.UNKNOWN_ERROR
                    _state.update {
                        it.copy(
                            restoreResult = RestoreOperationResult.Error(message),
                            restoreErrorMessage = message,
                            showRestoreErrorDialog = true,
                        )
                    }
                }
        }
    }

    fun resetAllPreferences() {
        logDebug("Resetting all preferences")
        viewModelScope.launch {
            settingsRepositoryRef.resetAllPreferences()
            _state.update { it.copy(showResetPreferencesDialog = false) }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Google Drive Backup (Phase 2)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Cambia la destinazione del backup automatico (LOCAL ↔ GOOGLE_DRIVE).
     *
     * **Logica**:
     * - Se cambio a GOOGLE_DRIVE e l'utente non è loggato, mostra il dialog di sign-in
     * - Se cambio a LOCAL, disabilita il dialog di sign-in
     * - Persiste la destinazione in SettingsRepository
     */
    fun setAutoBackupDestination(destination: BackupDestination) {
        logDebug("Setting auto backup destination: $destination")
        viewModelScope.launch {
            when (destination) {
                BackupDestination.LOCAL -> {
                    // Seleziona LOCAL: persisti e chiudi dialog se aperto
                    settingsRepositoryRef.setAutoBackupDestination(BackupDestination.LOCAL)
                    _state.update { it.copy(showGoogleSignInDialog = false) }
                }
                BackupDestination.GOOGLE_DRIVE -> {
                    // Seleziona GOOGLE_DRIVE: verifica se già loggato
                    val isSignedIn = googleSignInManager.isSignedIn()
                    if (!isSignedIn) {
                        // Non loggato: mostra dialog di sign-in
                        logDebug("User not signed in with Google — showing sign-in dialog")
                        _state.update { it.copy(showGoogleSignInDialog = true) }
                    } else {
                        // Già loggato: persisti subito
                        logDebug("User already signed in — setting destination to GOOGLE_DRIVE")
                        settingsRepositoryRef.setAutoBackupDestination(BackupDestination.GOOGLE_DRIVE)
                    }
                }
            }
        }
    }

    /**
     * Avvia il flusso di sign-in OAuth con Google.
     *
     * **Flusso**:
     * 1. Mostra loading state
     * 2. Chiama GoogleSignInManager.signIn()
     * 3. Su successo: persisti token, aggiorna email, imposta destinazione a GOOGLE_DRIVE
     * 4. Su fallimento: mostra errore
     *
     * **Nota**: Il vero picker di account è gestito a livello di Activity tramite
     * `registerForActivityResult(ActivityResultContracts.StartActivityForResult())`.
     * Questo metodo coordina il flusso post-picker.
     */
    fun initiateGoogleSignIn() {
        logDebug("Initiating Google Sign-In flow")
        _state.update { it.copy(googleDriveSignInLoading = true) }
        viewModelScope.launch {
            try {
                val signInResult = googleSignInManager.signIn()
                if (signInResult.isSuccess) {
                    val result = signInResult.getOrNull()
                    if (result != null) {
                        handleGoogleSignInSuccess(result)
                    } else {
                        handleGoogleSignInError(Exception("Sign-in returned null result"))
                    }
                } else {
                    val throwable = signInResult.exceptionOrNull()
                    val error = if (throwable is Exception) throwable else Exception(throwable?.message ?: "Unknown sign-in error", throwable)
                    handleGoogleSignInError(error)
                }
            } catch (e: Exception) {
                handleGoogleSignInError(e)
            }
        }
    }

    /**
     * Gestisce il successo del sign-in.
     *
     * **Azioni**:
     * - Salva email dell'utente
     * - Imposta flag isGoogleDriveSignedIn a true
     * - Imposta destinazione a GOOGLE_DRIVE
     * - Chiude il dialog di sign-in
     * - Disabilita il loading state
     */
    private suspend fun handleGoogleSignInSuccess(result: com.antcashmanager.android.auth.GoogleSignInResult) {
        logDebug("Google Sign-In successful for ${result.email}")
        try {
            // I token sono già salvati da GoogleSignInManager.handleSignInSuccess()
            // Aggiorna solo l'UI
            _state.update {
                it.copy(
                    googleDriveUserEmail = result.email,
                    isGoogleDriveSignedIn = true,
                    autoBackupDestination = BackupDestination.GOOGLE_DRIVE,
                    showGoogleSignInDialog = false,
                    googleDriveSignInLoading = false,
                )
            }
            // Persisti destinazione
            settingsRepositoryRef.setAutoBackupDestination(BackupDestination.GOOGLE_DRIVE)
        } catch (e: Exception) {
            logError("Error handling sign-in success: ${e.message}", e)
            handleGoogleSignInError(e)
        }
    }

    /**
     * Gestisce il fallimento del sign-in.
     *
     * **Azioni**:
     * - Log dell'errore
     * - Mantiene il toggle su LOCAL
     * - Disabilita il loading state
     * - Chiude il dialog (l'utente può ritentare cliccando di nuovo)
     */
    private suspend fun handleGoogleSignInError(error: Exception) {
        logError("Google Sign-In error: ${error.message}", error)
        _state.update {
            it.copy(
                autoBackupDestination = BackupDestination.LOCAL,
                showGoogleSignInDialog = false,
                googleDriveSignInLoading = false,
            )
        }
    }

    /**
     * Effettua il logout da Google.
     *
     * **Azioni**:
     * - Revoca credenziali via GoogleSignInManager
     * - Cancella token dal device
     * - Ripristina destinazione a LOCAL
     * - Aggiorna stato UI
     */
    fun signOutFromGoogle() {
        logDebug("Signing out from Google")
        viewModelScope.launch {
            try {
                val result = googleSignInManager.signOut()
                if (result.isSuccess) {
                    logDebug("Signed out successfully")
                    _state.update {
                        it.copy(
                            googleDriveUserEmail = null,
                            isGoogleDriveSignedIn = false,
                            autoBackupDestination = BackupDestination.LOCAL,
                        )
                    }
                } else {
                    logError("Sign-out failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                logError("Error signing out: ${e.message}", e)
            }
        }
    }

    /**
     * Chiude il dialog di sign-in senza effettuare alcuna azione.
     *
     * **Azioni**:
     * - Chiude il dialog
     * - Mantiene la destinazione su LOCAL
     * - Disabilita il loading state
     */
    fun dismissGoogleSignInDialog() {
        logDebug("Dismissing Google Sign-In dialog")
        _state.update {
            it.copy(
                showGoogleSignInDialog = false,
                googleDriveSignInLoading = false,
                autoBackupDestination = BackupDestination.LOCAL,
            )
        }
    }
}

