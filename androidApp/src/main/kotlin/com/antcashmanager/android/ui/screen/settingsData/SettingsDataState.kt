package com.antcashmanager.android.ui.screen.settingsData

/**
 * Stato UI per la schermata Gestione Dati.
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
