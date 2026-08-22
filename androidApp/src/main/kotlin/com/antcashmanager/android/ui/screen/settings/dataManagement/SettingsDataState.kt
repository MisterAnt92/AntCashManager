package com.antcashmanager.android.ui.screen.settings.dataManagement

import com.antcashmanager.domain.model.BackupDestination

/**
 * Stato UI per la schermata Gestione Dati.
 *
 * **Backup Automatico (Fase 1 & 2)**:
 * - `autoBackupEnabled`: Toggle abilitazione backup automatico
 * - `autoBackupDestination`: LOCAL o GOOGLE_DRIVE
 * - `autoBackupFolderUri`: URI della cartella locale (Phase 1)
 * - `googleDriveUserEmail`: Email utente loggato (Phase 2)
 * - `isGoogleDriveSignedIn`: Flag se l'utente è autenticato con Google
 * - `showGoogleSignInDialog`: Mostra dialog di login
 * - `googleDriveSignInLoading`: Loading state durante sign-in flow
 */
data class SettingsDataState(
    val deleteResult: DeleteResult = DeleteResult.Idle,
    val backupResult: BackupResult = BackupResult.Idle,
    val restoreResult: RestoreOperationResult = RestoreOperationResult.Idle,
    val showDeleteConfirmDialog: Boolean = false,
    val showDeleteSuccessDialog: Boolean = false,
    val showResetPreferencesDialog: Boolean = false,
    val showBackupSuccessDialog: Boolean = false,
    val showBackupErrorDialog: Boolean = false,
    val showRestoreSuccessDialog: Boolean = false,
    val showRestoreErrorDialog: Boolean = false,
    val backupErrorMessage: String = "",
    val restoreErrorMessage: String = "",
    val restoreSuccessInfo: RestoreSuccessInfo? = null,
    val pendingBackupData: String? = null,
    val pendingBackupFileName: String? = null,
    val dataEncryptionEnabled: Boolean = false,
    val lastBackupTimestamp: Long? = null,
    val lastRestoreTimestamp: Long? = null,
    val suggestionsEnabled: Boolean = true,
    val showDeleteSuggestionsDialog: Boolean = false,
    // ── Automatic Backup (Phase 1 & 2) ──
    val autoBackupEnabled: Boolean = false,
    val autoBackupDestination: BackupDestination = BackupDestination.LOCAL,
    val autoBackupFolderUri: String? = null,
    // ── Google Drive Backup (Phase 2) ──
    val googleDriveUserEmail: String? = null,
    val isGoogleDriveSignedIn: Boolean = false,
    val showGoogleSignInDialog: Boolean = false,
    val googleDriveSignInLoading: Boolean = false,
)

data class RestoreSuccessInfo(
    val transactions: Int,
    val categories: Int,
)

sealed interface DeleteResult {
    data object Idle : DeleteResult
    data object Success : DeleteResult
    data class Error(val message: String) : DeleteResult
}

sealed interface BackupResult {
    data object Idle : BackupResult
    data object Loading : BackupResult
    data object Success : BackupResult
    data class Error(val message: String) : BackupResult
}

sealed interface RestoreOperationResult {
    data object Idle : RestoreOperationResult
    data object Loading : RestoreOperationResult
    data class Success(val transactions: Int, val categories: Int) : RestoreOperationResult
    data class Error(val message: String) : RestoreOperationResult
}
