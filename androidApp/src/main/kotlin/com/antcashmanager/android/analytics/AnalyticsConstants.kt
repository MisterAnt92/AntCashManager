package com.antcashmanager.android.analytics

import com.antcashmanager.android.analytics.AnalyticsConstants.ALLOWED_USAGE_EVENTS


/**
 * Shared constants for analytics tracking.
 *
 * Event names live in [Events], parameter keys in [Params]; [ALLOWED_USAGE_EVENTS] is the
 * privacy whitelist and is built from the same constants so no name is ever duplicated.
 */
object AnalyticsConstants {
    const val TAG = "AnalyticsManager"
    const val MAX_NAME_LENGTH = 40
    const val SCREEN_CLASS_COMPOSE_NAV_HOST = "ComposeNavHost"

    /** Event names. Every `logEvent(...)` call must reference one of these. */
    object Events {
        // Transactions
        const val TRANSACTIONS_FILTER_APPLIED = "transactions_filter_applied"
        const val TRANSACTIONS_FILTER_CLEARED = "transactions_filter_cleared"
        const val TRANSACTIONS_FILTER_OPENED = "transactions_filter_opened"
        const val TRANSACTIONS_HELP_OPENED = "transactions_help_opened"
        const val TRANSACTIONS_LIST_ITEM_CLICKED = "transactions_list_item_clicked"
        const val TRANSACTION_ADD_OPENED = "transaction_add_opened"
        const val TRANSACTION_FORM_OPENED = "transaction_form_opened"
        const val TRANSACTION_FORM_CANCELLED = "transaction_form_cancelled"
        const val TRANSACTION_FORM_VALIDATION_FAILED = "transaction_form_validation_failed"
        const val TRANSACTION_SUBMIT_SUCCESS = "transaction_submit_success"
        const val TRANSACTION_DELETED = "transaction_deleted"
        const val TRANSACTION_SHARED = "transaction_shared"
        const val TRANSACTION_RECURRING_TOGGLED = "transaction_recurring_toggled"
        const val TRANSACTION_DUPLICATE_SUGGESTION_ACCEPTED = "transaction_duplicate_suggestion_accepted"
        const val TRANSACTION_TYPE_SELECTED = "transaction_type_selected"
        const val PAYMENT_TYPE_SELECTED = "payment_type_selected"
        const val RECURRING_INTERVAL_SELECTED = "recurring_interval_selected"
        const val FORM_FIELD_FIRST_INTERACTION = "form_field_first_interaction"
        const val DATE_PICKER_SELECTED = "date_picker_selected"
        const val CATEGORY_SELECTION_DIALOG_DISMISSED = "category_selection_dialog_dismissed"

        // Receipt scan
        const val RECEIPT_SCAN_OPENED = "receipt_scan_opened"
        const val RECEIPT_SCAN_CAPTURED = "receipt_scan_captured"
        const val RECEIPT_SCAN_SAVED = "receipt_scan_saved"
        const val RECEIPT_SCAN_AMOUNT_EDITED = "receipt_scan_amount_edited"
        const val RECEIPT_SCAN_RETRY = "receipt_scan_retry"
        const val RECEIPT_SCAN_FAILED = "receipt_scan_failed"
        const val RECEIPT_SCAN_MANUAL_ENTRY = "receipt_scan_manual_entry"

        // Backup / restore / data
        const val BACKUP_CREATE_REQUESTED = "backup_create_requested"
        const val BACKUP_FILE_SAVED = "backup_file_saved"
        const val BACKUP_FILE_SAVE_ERROR = "backup_file_save_error"
        const val BACKUP_FILE_READ_ERROR = "backup_file_read_error"
        const val BACKUP_FILE_READ_CHARSET_ERROR = "backup_file_read_charset_error"
        const val RESTORE_OPEN_REQUESTED = "restore_open_requested"
        const val RESTORE_FILE_SELECTED = "restore_file_selected"
        const val DELETE_ALL_DATA_CONFIRMED = "delete_all_data_confirmed"
        const val RESET_PREFERENCES_CONFIRMED = "reset_preferences_confirmed"
        const val DELETE_SUGGESTIONS_CONFIRMED = "delete_suggestions_confirmed"
        const val DATA_ENCRYPTION_TOGGLED = "data_encryption_toggled"
        const val SUGGESTIONS_TOGGLED = "suggestions_toggled"
        const val AUTO_BACKUP_TOGGLED = "auto_backup_toggled"
        const val AUTO_BACKUP_FOLDER_SELECTED = "auto_backup_folder_selected"
        const val NOTIFICATION_PERMISSION_GRANTED = "notification_permission_granted"
        const val APP_AUTO_BACKUP_EXECUTED = "app_auto_backup_executed"

        // Categories
        const val CATEGORY_CREATED = "category_created"
        const val CATEGORY_DELETED = "category_deleted"
        const val CATEGORY_HIDDEN = "category_hidden"
        const val CATEGORY_SHOWN = "category_shown"
        const val CATEGORIES_REORDERED = "categories_reordered"
        const val CATEGORIES_REORDER_OPENED = "categories_reorder_opened"
        const val CATEGORIES_HELP_OPENED = "categories_help_opened"
        const val CATEGORIES_LIST_ITEM_CLICKED = "categories_list_item_clicked"
        const val CATEGORY_TYPE_DISTRIBUTION = "category_type_distribution"
        const val CATEGORY_CRUD_OPERATION = "category_crud_operation"

        // Charts
        const val CHART_DATE_FILTER_CHANGED = "chart_date_filter_changed"
        const val CHART_CUSTOM_DATE_RANGE_SET = "chart_custom_date_range_set"
        const val CHART_SHARED = "chart_shared"
        const val CHART_HELP_OPENED = "chart_help_opened"
        const val CHART_ITEM_CLICKED = "chart_item_clicked"
        const val CHART_LOADING_COMPLETED = "chart_loading_completed"
        const val CHART_RENDERING_TIME = "chart_rendering_time"
        const val CHART_INTERACTION_DEPTH = "chart_interaction_depth"

        // Home
        const val HOME_TOP_CARDS_REORDERED = "home_top_cards_reordered"
        const val HOME_DATE_FILTER_CHANGED = "home_date_filter_changed"
        const val HOME_SEARCH_OPENED = "home_search_opened"
        const val HOME_SEARCH_SUBMITTED = "home_search_submitted"
        const val HOME_SEARCH_CLEARED = "home_search_cleared"
        const val HOME_HELP_OPENED = "home_help_opened"
        const val HOME_TRANSACTION_CLICKED = "home_transaction_clicked"
        const val HOME_TRANSACTION_DETAIL_OPENED = "home_transaction_detail_opened"
        const val HOME_QUICK_INSIGHTS_TOGGLED = "home_quick_insights_toggled"
        const val SEARCH_QUERY_INITIATED = "search_query_initiated"
        const val FILTER_COMBINATION_APPLIED = "filter_combination_applied"
        const val EMPTY_STATE_ACTION_TAKEN = "empty_state_action_taken"

        // Settings / display
        const val DATE_FORMAT_CHANGED = "date_format_changed"
        const val TRANSACTION_DISPLAY_TYPE_CHANGED = "transaction_display_type_changed"
        const val THEME_CHANGED = "theme_changed"
        const val LANGUAGE_CHANGED = "language_changed"
        const val CURRENCY_FORMAT_CHANGED = "currency_format_changed"
        const val CURRENCY_FORMAT_DIALOG_DISMISSED = "currency_format_dialog_dismissed"
        const val FEEDBACK_EMAIL_SENT = "feedback_email_sent"
        const val DECIMAL_DIGITS_CHANGED = "decimal_digits_changed"
        const val DECIMAL_SEPARATOR_CHANGED = "decimal_separator_changed"
        const val THOUSANDS_SEPARATOR_CHANGED = "thousands_separator_changed"
        const val MEAL_VOUCHER_VALUE_CHANGED = "meal_voucher_value_changed"
        const val MEAL_VOUCHER_DETAILS_UPDATED = "meal_voucher_details_updated"
        const val MASK_AMOUNTS_TOGGLED = "mask_amounts_toggled"
        const val SHOW_CHARTS_SECTION_TOGGLED = "show_charts_section_toggled"
        const val CHARTS_ZOOM_TOGGLED = "charts_zoom_toggled"
        const val SHOW_PAYMENT_BREAKDOWN_TOGGLED = "show_payment_breakdown_toggled"
        const val SHOW_TRANSACTION_NOTES_TOGGLED = "show_transaction_notes_toggled"
        const val DEFAULT_PAYMENT_TYPE_CHANGED = "default_payment_type_changed"
        const val WIDGET_BACKGROUND_COLOR_CHANGED = "widget_background_color_changed"
        const val WIDGET_OPACITY_CHANGED = "widget_opacity_changed"
        const val WIDGET_RECENT_TRANSACTIONS_OPENED = "widget_recent_transactions_opened"
        const val WIDGET_CATEGORY_BREAKDOWN_OPENED = "widget_category_breakdown_opened"
        const val WIDGET_TAP_ACTION_TRIGGERED = "widget_tap_action_triggered"
        const val WIDGET_ENGAGEMENT_SESSION = "widget_engagement_session"
        const val SETTINGS_HELP_OPENED = "settings_help_opened"
        const val SETTINGS_PRIVACY_POLICY_OPENED = "settings_privacy_policy_opened"
        const val SETTINGS_THIRD_PARTY_LIBRARIES_OPENED = "settings_third_party_libraries_opened"
        const val SETTINGS_HIGH_CONTRAST_TOGGLED = "settings_high_contrast_toggled"
        const val SETTINGS_LARGE_TEXT_TOGGLED = "settings_large_text_toggled"
        const val SETTINGS_REDUCE_MOTION_TOGGLED = "settings_reduce_motion_toggled"
        const val SETTINGS_DIALOG_DISMISSED = "settings_dialog_dismissed"
        const val SETTINGS_SUBMENU_OPENED = "settings_submenu_opened"
        const val SETTINGS_CUSTOMIZATION_SCORE = "settings_customization_score"
        const val EASTER_EGG_ANIMATION_OPENED = "easter_egg_animation_opened"

        // Navigation
        const val SIDEBAR_NAVIGATION_CLICKED = "sidebar_navigation_clicked"
        const val SIDEBAR_TOGGLED = "sidebar_toggled"
        const val SCREEN_LOAD_TIME = "screen_load_time"

        // Tutorial
        const val TUTORIAL_REPLAY_REQUESTED = "tutorial_replay_requested"
        const val TUTORIAL_STARTED = "tutorial_started"
        const val TUTORIAL_STEP_COMPLETED = "tutorial_step_completed"
        const val TUTORIAL_STEP_SKIPPED = "tutorial_step_skipped"
        const val TUTORIAL_COMPLETED = "tutorial_completed"
        const val TUTORIAL_STEP_HELP_OPENED = "tutorial_step_help_opened"

        // Performance / session / lifecycle
        const val TRANSACTION_FORM_SUBMIT_LATENCY = "transaction_form_submit_latency"
        const val RECEIPT_OCR_PROCESSING_TIME = "receipt_ocr_processing_time"
        const val BACKUP_RESTORE_DURATION = "backup_restore_duration"
        const val APP_LAUNCHED = "app_launched"
        const val SESSION_STARTED = "session_started"
        const val SESSION_ENDED = "session_ended"
        const val APP_VERSION_UPDATED = "app_version_updated"
        const val DAILY_ACTIVE_USER = "daily_active_user"
        const val VIEWMODEL_CREATED = "viewmodel_created"
        const val VIEWMODEL_CLEARED = "viewmodel_cleared"

        // Segmentation / cohorts
        const val USER_COHORT_IDENTIFIED = "user_cohort_identified"
        const val SPENDING_PATTERN_DETECTED = "spending_pattern_detected"
        const val INCOME_SOURCE_TRACKING = "income_source_tracking"
        const val BUDGET_EXCEEDED_ALERT = "budget_exceeded_alert"
        const val CATEGORY_PREFERENCE_SHIFT = "category_preference_shift"
        const val PAYMENT_METHOD_PREFERENCE = "payment_method_preference"
        const val TRANSACTION_SEARCH_EFFECTIVENESS = "transaction_search_effectiveness"

        // Errors
        const val DATABASE_ERROR = "database_error"
        const val SYNC_ERROR = "sync_error"
        const val PAYMENT_METHOD_ERROR = "payment_method_error"
        const val TRANSACTION_VALIDATION_ERROR = "transaction_validation_error"
        const val RECEIPT_OCR_ERROR = "receipt_ocr_error"
    }

    /** Bundle parameter keys. */
    object Params {
        const val INDEX = "index"
        const val TYPE = "type"
        const val DATE = "date"
        const val PRESET = "preset"
        const val MODE = "mode"
        const val ENABLED = "enabled"
        const val SETTING = "setting"
        const val SUBMENU = "submenu"
        const val DIALOG_TYPE = "dialog_type"
        const val CATEGORY = "category"
        const val CATEGORY_NAME = "category_name"
        const val TRANSACTION_TYPE = "transaction_type"
        const val PAYMENT_TYPE = "payment_type"
        const val CHART_TYPE = "chart_type"
        const val DATA_POINTS = "data_points"
        const val FILTER_COUNT = "filter_count"
        const val TYPES = "types"
        const val QUERY_LENGTH = "query_length"
        const val FILTERS_ACTIVE = "filters_active"
        const val HAS_SEARCH_QUERY = "has_search_query"
        const val HAS_CATEGORY_FILTER = "has_category_filter"
        const val SUGGESTION_TYPE = "suggestion_type"
        const val SUGGESTION_LENGTH = "suggestion_length"
        const val OPERATION = "operation"
        const val ACTION = "action"
        const val STATUS = "status"
        const val SCREEN = "screen"
        const val STEP = "step"
        const val VALUE = "value"
        const val THEME = "theme"
        const val INTERVAL = "interval"
        const val ERROR_TYPE = "error_type"
        const val ERROR_CODE = "error_code"
        const val DURATION_MS = "duration_ms"
        const val ZOOM_ENABLED = "zoom_enabled"

        // Common values
        const val VALUE_YES = "yes"
        const val VALUE_NO = "no"
        const val VALUE_ALL = "all"
        const val VALUE_UNKNOWN = "unknown"
        const val VALUE_CREATE = "create"
        const val VALUE_UPDATE = "update"
        const val TYPES_SEPARATOR = "|"
    }

    /** Privacy whitelist: events not listed here are silently dropped by [AnalyticsManager]. */
    val ALLOWED_USAGE_EVENTS: Set<String> =
        setOf(
            Events.TRANSACTIONS_FILTER_APPLIED,
            Events.TRANSACTIONS_FILTER_CLEARED,
            Events.TRANSACTION_ADD_OPENED,
            Events.RECEIPT_SCAN_OPENED,
            Events.TRANSACTION_FORM_OPENED,
            Events.TRANSACTION_FORM_CANCELLED,
            Events.TRANSACTION_SUBMIT_SUCCESS,
            Events.TRANSACTION_DELETED,
            Events.TRANSACTION_SHARED,
            Events.BACKUP_CREATE_REQUESTED,
            Events.BACKUP_FILE_SAVED,
            Events.BACKUP_FILE_SAVE_ERROR,
            Events.RESTORE_OPEN_REQUESTED,
            Events.RESTORE_FILE_SELECTED,
            Events.DELETE_ALL_DATA_CONFIRMED,
            Events.RESET_PREFERENCES_CONFIRMED,
            Events.DELETE_SUGGESTIONS_CONFIRMED,
            Events.CATEGORY_CREATED,
            Events.CATEGORY_DELETED,
            Events.CATEGORIES_REORDERED,
            Events.CATEGORIES_REORDER_OPENED,
            Events.CATEGORY_HIDDEN,
            Events.CATEGORY_SHOWN,
            Events.CHART_DATE_FILTER_CHANGED,
            Events.CHART_CUSTOM_DATE_RANGE_SET,
            Events.CHART_SHARED,
            Events.CHART_HELP_OPENED,
            Events.HOME_TOP_CARDS_REORDERED,
            Events.HOME_DATE_FILTER_CHANGED,
            Events.HOME_SEARCH_OPENED,
            Events.HOME_HELP_OPENED,
            Events.RECEIPT_SCAN_CAPTURED,
            Events.RECEIPT_SCAN_SAVED,
            Events.RECEIPT_SCAN_AMOUNT_EDITED,
            Events.DATA_ENCRYPTION_TOGGLED,
            Events.SUGGESTIONS_TOGGLED,
            Events.HOME_QUICK_INSIGHTS_TOGGLED,
            Events.DATE_FORMAT_CHANGED,
            Events.TRANSACTION_DISPLAY_TYPE_CHANGED,
            Events.THEME_CHANGED,
            Events.LANGUAGE_CHANGED,
            Events.CURRENCY_FORMAT_CHANGED,
            Events.FEEDBACK_EMAIL_SENT,
            Events.TUTORIAL_REPLAY_REQUESTED,
            Events.DECIMAL_DIGITS_CHANGED,
            Events.DECIMAL_SEPARATOR_CHANGED,
            Events.THOUSANDS_SEPARATOR_CHANGED,
            Events.MEAL_VOUCHER_VALUE_CHANGED,
            Events.MASK_AMOUNTS_TOGGLED,
            Events.SHOW_CHARTS_SECTION_TOGGLED,
            Events.CHARTS_ZOOM_TOGGLED,
            Events.SHOW_PAYMENT_BREAKDOWN_TOGGLED,
            Events.SHOW_TRANSACTION_NOTES_TOGGLED,
            Events.WIDGET_BACKGROUND_COLOR_CHANGED,
            Events.WIDGET_OPACITY_CHANGED,
            Events.WIDGET_RECENT_TRANSACTIONS_OPENED,
            Events.WIDGET_CATEGORY_BREAKDOWN_OPENED,
            Events.SETTINGS_HELP_OPENED,
            Events.SETTINGS_PRIVACY_POLICY_OPENED,
            Events.SETTINGS_THIRD_PARTY_LIBRARIES_OPENED,
            Events.SETTINGS_HIGH_CONTRAST_TOGGLED,
            Events.SETTINGS_LARGE_TEXT_TOGGLED,
            Events.SETTINGS_REDUCE_MOTION_TOGGLED,
            Events.TRANSACTIONS_FILTER_OPENED,
            Events.TRANSACTIONS_HELP_OPENED,
            Events.CATEGORIES_HELP_OPENED,
            Events.RECEIPT_SCAN_RETRY,
            Events.TRANSACTION_RECURRING_TOGGLED,
            Events.HOME_TRANSACTION_DETAIL_OPENED,
            Events.HOME_SEARCH_SUBMITTED,
            Events.HOME_SEARCH_CLEARED,
            Events.TRANSACTION_FORM_VALIDATION_FAILED,
            Events.RECEIPT_SCAN_FAILED,
            Events.RECEIPT_SCAN_MANUAL_ENTRY,
            Events.EMPTY_STATE_ACTION_TAKEN,
            Events.CHART_LOADING_COMPLETED,
            Events.SETTINGS_DIALOG_DISMISSED,
            Events.DATE_PICKER_SELECTED,
            Events.CATEGORY_SELECTION_DIALOG_DISMISSED,
            Events.TRANSACTION_TYPE_SELECTED,
            Events.PAYMENT_TYPE_SELECTED,
            Events.CURRENCY_FORMAT_DIALOG_DISMISSED,
            Events.SIDEBAR_NAVIGATION_CLICKED,
            Events.SETTINGS_SUBMENU_OPENED,
            Events.SIDEBAR_TOGGLED,
            Events.TRANSACTIONS_LIST_ITEM_CLICKED,
            Events.CATEGORIES_LIST_ITEM_CLICKED,
            Events.CHART_ITEM_CLICKED,
            Events.EASTER_EGG_ANIMATION_OPENED,
            Events.TUTORIAL_STARTED,
            Events.TUTORIAL_STEP_COMPLETED,
            Events.TUTORIAL_STEP_SKIPPED,
            Events.TUTORIAL_COMPLETED,
            Events.TUTORIAL_STEP_HELP_OPENED,
            Events.CATEGORY_TYPE_DISTRIBUTION,
            Events.MEAL_VOUCHER_DETAILS_UPDATED,
            Events.RECURRING_INTERVAL_SELECTED,
            Events.SEARCH_QUERY_INITIATED,
            Events.FILTER_COMBINATION_APPLIED,
            Events.CATEGORY_CRUD_OPERATION,
            Events.FORM_FIELD_FIRST_INTERACTION,
            Events.APP_AUTO_BACKUP_EXECUTED,
            Events.WIDGET_TAP_ACTION_TRIGGERED,
            Events.TRANSACTION_DUPLICATE_SUGGESTION_ACCEPTED,
            Events.SCREEN_LOAD_TIME,
            Events.TRANSACTION_FORM_SUBMIT_LATENCY,
            Events.CHART_RENDERING_TIME,
            Events.RECEIPT_OCR_PROCESSING_TIME,
            Events.BACKUP_RESTORE_DURATION,
            Events.APP_LAUNCHED,
            Events.SESSION_STARTED,
            Events.SESSION_ENDED,
            Events.APP_VERSION_UPDATED,
            Events.DAILY_ACTIVE_USER,
            Events.DATABASE_ERROR,
            Events.SYNC_ERROR,
            Events.PAYMENT_METHOD_ERROR,
            Events.USER_COHORT_IDENTIFIED,
            Events.SPENDING_PATTERN_DETECTED,
            Events.INCOME_SOURCE_TRACKING,
            Events.BUDGET_EXCEEDED_ALERT,
            Events.CATEGORY_PREFERENCE_SHIFT,
            Events.PAYMENT_METHOD_PREFERENCE,
            Events.TRANSACTION_SEARCH_EFFECTIVENESS,
            Events.CHART_INTERACTION_DEPTH,
            Events.WIDGET_ENGAGEMENT_SESSION,
            Events.SETTINGS_CUSTOMIZATION_SCORE,
            Events.VIEWMODEL_CREATED,
            Events.VIEWMODEL_CLEARED,
            Events.TRANSACTION_VALIDATION_ERROR,
            Events.RECEIPT_OCR_ERROR,
        )
}
