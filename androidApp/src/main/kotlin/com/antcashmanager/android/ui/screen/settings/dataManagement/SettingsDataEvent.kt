package com.antcashmanager.android.ui.screen.settings.dataManagement

import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.model.BackupFrequency

/**
 * UDF Pattern: Events for Settings Data Management screen.
 *
 * All user interactions (backup, restore, encryption toggle, etc.) emit events
 * that the ViewModel processes to update data management state.
 */
sealed class SettingsDataEvent {
    // ── Data Encryption ──
    data class SetDataEncryptionEnabledEvent(
        val enabled: Boolean,
    ) : SettingsDataEvent()

    // ── Suggestions ──
    data class SetSuggestionsEnabledEvent(
        val enabled: Boolean,
    ) : SettingsDataEvent()

    data object ShowDeleteSuggestionsDialogEvent : SettingsDataEvent()

    data object DismissDeleteSuggestionsDialogEvent : SettingsDataEvent()

    data object DeleteAllSuggestionsEvent : SettingsDataEvent()

    // ── Automatic Backup (Phase 1 & 2) ──
    data class SetAutoBackupEnabledEvent(
        val enabled: Boolean,
    ) : SettingsDataEvent()

    data class OnAutoBackupFolderSelectedEvent(
        val uriString: String,
    ) : SettingsDataEvent()

    data object OnAutoBackupFolderSelectionCancelledEvent : SettingsDataEvent()

    data class SetAutoBackupDestinationEvent(
        val destination: BackupDestination,
    ) : SettingsDataEvent()

    data class SetBackupFrequencyEvent(
        val frequency: BackupFrequency,
    ) : SettingsDataEvent()

    // ── Delete All Data ──
    data object ShowDeleteConfirmDialogEvent : SettingsDataEvent()

    data object DismissDeleteConfirmDialogEvent : SettingsDataEvent()

    data object DismissDeleteSuccessDialogEvent : SettingsDataEvent()

    data object DeleteAllDataEvent : SettingsDataEvent()

    // ── Reset Preferences ──
    data object ShowResetPreferencesDialogEvent : SettingsDataEvent()

    data object DismissResetPreferencesDialogEvent : SettingsDataEvent()

    data object ResetAllPreferencesEvent : SettingsDataEvent()

    // ── Backup Operations ──
    data object CreateBackupEvent : SettingsDataEvent()

    data object OnBackupFileSavedEvent : SettingsDataEvent()

    data object ClearPendingBackupRequestEvent : SettingsDataEvent()

    data class OnBackupFileSaveErrorEvent(
        val message: String,
    ) : SettingsDataEvent()

    data object DismissBackupSuccessDialogEvent : SettingsDataEvent()

    data object DismissBackupErrorDialogEvent : SettingsDataEvent()

    // ── Restore Operations ──
    data class RestoreBackupEvent(
        val jsonString: String,
    ) : SettingsDataEvent()

    data class OnRestoreFileReadErrorEvent(
        val message: String,
    ) : SettingsDataEvent()

    data object DismissRestoreSuccessDialogEvent : SettingsDataEvent()

    data object DismissRestoreErrorDialogEvent : SettingsDataEvent()

    // ── Google Drive Sign-In (Phase 2) ──
    data object InitiateGoogleSignInEvent : SettingsDataEvent()

    data object SignOutFromGoogleEvent : SettingsDataEvent()

    data object DismissGoogleSignInDialogEvent : SettingsDataEvent()
}
