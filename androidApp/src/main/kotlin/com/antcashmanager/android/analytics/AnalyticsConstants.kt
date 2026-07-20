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
        "transaction_deleted",
        "transaction_shared",
        "backup_create_requested",
        "backup_file_saved",
        "backup_file_save_error",
        "restore_open_requested",
        "restore_file_selected",
        "delete_all_data_confirmed",
        "reset_preferences_confirmed",
        "delete_suggestions_confirmed",
        "category_created",
        "category_deleted",
        "chart_date_filter_changed",
        "chart_custom_date_range_set",
        "chart_shared",
        "chart_help_opened",
        "home_top_cards_reordered",
        "home_date_filter_changed",
        "home_search_opened",
        "home_help_opened",
        "receipt_scan_captured",
        "receipt_scan_saved",
        "data_encryption_toggled",
        "suggestions_toggled",
        "home_quick_insights_toggled",
        "date_format_changed",
        "transaction_display_type_changed",
        "theme_changed",
        "language_changed",
        "currency_format_changed",
        "feedback_email_sent",
        "tutorial_replay_requested",
    )
}

