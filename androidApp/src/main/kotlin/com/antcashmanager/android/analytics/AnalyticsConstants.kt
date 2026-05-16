package com.antcashmanager.android.analytics

/**
 * Shared constants for analytics tracking.
 */
object AnalyticsConstants {
    const val TAG = "AnalyticsManager"
    const val MAX_NAME_LENGTH = 40
    const val SCREEN_CLASS_COMPOSE_NAV_HOST = "ComposeNavHost"

    val ALLOWED_USAGE_EVENTS = setOf(
        "transactions_filter_applied",
        "transactions_filter_cleared",
        "transaction_add_opened",
        "receipt_scan_opened",
        "transaction_form_opened",
        "transaction_form_cancelled",
        "transaction_submit_success",
        "backup_create_requested",
        "backup_file_saved",
        "backup_file_save_error",
        "restore_open_requested",
        "restore_file_selected",
        "delete_all_data_confirmed",
        "reset_preferences_confirmed",
    )
}

