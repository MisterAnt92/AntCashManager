package com.antcashmanager.android.ui.screen.settingsData

/**
 * Shared constants for the Settings Data feature.
 */
object SettingsDataConstant {
    const val TAG = "SettingsDataViewModel"
    const val UNKNOWN_ERROR = "Unknown error"
    const val BACKUP_TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss"
    const val BACKUP_FILE_PREFIX = "antcashmanager_backup_"
    const val BACKUP_FILE_SUFFIX = ".json"
    const val CONTENT_HORIZONTAL_PADDING_DP = 16
    const val CONTENT_TOP_PADDING_DP = 12
    const val CONTENT_BOTTOM_PADDING_DP = 24
    const val TABLET_COLUMNS_SPACING_DP = 16
    const val CARD_SPACING_DP = 8

    /**
     * Durata minima di visualizzazione della dialog di caricamento per backup/restore: se
     * l'operazione reale è più veloce, si attende la differenza prima di chiudere la dialog,
     * per evitare un flash troppo rapido che sembrerebbe un mancato feedback.
     */
    const val MIN_LOADING_DURATION_MS = 1500L
}
