package com.antcashmanager.android.ui.screen.settings.dataManagement

import com.antcashmanager.domain.model.BackupDestination

/**
 * UDF Pattern: Events for Settings Data Management screen.
 *
 * All user interactions (backup, restore, encryption toggle, etc.) emit events
 * that the ViewModel processes to update data management state.
 */
sealed class SettingsDataEvent {
    // ── Data Encryption ──
    data class SetDataEncryptionEnabled(val enabled: Boolean) : SettingsDataEvent()

    // ── Suggestions ──
    data class SetSuggestionsEnabled(val enabled: Boolean) : SettingsDataEvent()
    data object ShowDeleteSuggestionsDialog : SettingsDataEvent()
    data object DismissDeleteSuggestionsDialog : SettingsDataEvent()
    data object DeleteAllSuggestions : SettingsDataEvent()

    // ── Automatic Backup (Phase 1 & 2) ──
    data class SetAutoBackupEnabled(val enabled: Boolean) : SettingsDataEvent()
    data class OnAutoBackupFolderSelected(val uriString: String) : SettingsDataEvent()
    data object OnAutoBackupFolderSelectionCancelled : SettingsDataEvent()
    data class SetAutoBackupDestination(val destination: BackupDestination) : SettingsDataEvent()

    // ── Delete All Data ──
    data object ShowDeleteConfirmDialog : SettingsDataEvent()
    data object DismissDeleteConfirmDialog : SettingsDataEvent()
    data object DismissDeleteSuccessDialog : SettingsDataEvent()
    data object DeleteAllData : SettingsDataEvent()

    // ── Reset Preferences ──
    data object ShowResetPreferencesDialog : SettingsDataEvent()
    data object DismissResetPreferencesDialog : SettingsDataEvent()
    data object ResetAllPreferences : SettingsDataEvent()

    // ── Backup Operations ──
    data object CreateBackup : SettingsDataEvent()
    data object OnBackupFileSaved : SettingsDataEvent()
    data object ClearPendingBackupRequest : SettingsDataEvent()
    data class OnBackupFileSaveError(val message: String) : SettingsDataEvent()
    data object DismissBackupSuccessDialog : SettingsDataEvent()
    data object DismissBackupErrorDialog : SettingsDataEvent()

    // ── Restore Operations ──
    data class RestoreBackup(val jsonString: String) : SettingsDataEvent()
    data class OnRestoreFileReadError(val message: String) : SettingsDataEvent()
    data object DismissRestoreSuccessDialog : SettingsDataEvent()
    data object DismissRestoreErrorDialog : SettingsDataEvent()

    // ── Google Drive Sign-In (Phase 2) ──
    data object InitiateGoogleSignIn : SettingsDataEvent()
    data object SignOutFromGoogle : SettingsDataEvent()
    data object DismissGoogleSignInDialog : SettingsDataEvent()
}
