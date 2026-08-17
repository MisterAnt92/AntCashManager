package com.antcashmanager.android.test.data

/**
 * Centralized test constants to reduce hardcoded strings
 *
 * Benefits:
 * - Changes propagate to all test files automatically
 * - Reduces maintenance burden if UI strings change
 * - Makes test code more readable and maintainable
 * - Eliminates typo-related test failures
 *
 * Usage:
 * ```kotlin
 * composeTestRule.waitForNode(TestStrings.ADD_TRANSACTION_BUTTON)
 * composeTestRule.waitUntil(TestTimeouts.DEFAULT_WAIT) { ... }
 * ```
 */

/**
 * Common UI text strings used across test suite
 */
object TestStrings {
    // Navigation & Buttons
    const val ADD_TRANSACTION_BUTTON = "Add Transaction"
    const val EDIT_TRANSACTION_BUTTON = "Edit"
    const val DELETE_TRANSACTION_BUTTON = "Delete"
    const val SAVE_BUTTON = "Save"
    const val CANCEL_BUTTON = "Cancel"
    const val OK_BUTTON = "OK"
    const val CLOSE_BUTTON = "Close"

    // States & Messages
    const val LOADING = "Loading..."
    const val SUCCESS = "Success"
    const val ERROR = "Error"
    const val EMPTY_STATE = "No transactions"
    const val EMPTY_CATEGORIES = "No categories"
    const val NO_DATA = "No data available"

    // Common Labels
    const val TITLE = "Title"
    const val AMOUNT = "Amount"
    const val CATEGORY = "Category"
    const val DATE = "Date"
    const val NOTES = "Notes"
    const val PAYEE = "Payee"
    const val LOCATION = "Location"

    // Transaction Types
    const val EXPENSE = "Expense"
    const val INCOME = "Income"

    // Settings
    const val SETTINGS = "Settings"
    const val ACCOUNT = "Account"
    const val PREFERENCES = "Preferences"
    const val BACKUP = "Backup"
    const val RESTORE = "Restore"

    // Meal Vouchers
    const val MEAL_VOUCHERS = "Meal Vouchers"
    const val MEAL_VOUCHER_COUNT = "Number of meal vouchers"
    const val MEAL_VOUCHER_DIFFERENCE = "Difference paid"
    const val MEAL_VOUCHER_TOTAL = "Total amount"
}

/**
 * Timeout constants for test synchronization
 *
 * These values control how long tests wait for various operations to complete.
 * Adjust based on your device/emulator performance.
 */
object TestTimeouts {
    /** 1 second - for quick operations (immediate UI updates, fast animations) */
    const val SHORT_WAIT = 1000L

    /** 2 seconds - default timeout for most operations */
    const val DEFAULT_WAIT = 2000L

    /** 5 seconds - for slow operations (data loading, complex calculations) */
    const val LONG_WAIT = 5000L

    /** 800ms - for animation completion */
    const val ANIMATION_WAIT = 800L

    /** 500ms - for basic transitions */
    const val TRANSITION_WAIT = 500L

    /** 3 seconds - for database operations */
    const val DATABASE_WAIT = 3000L

    /** 10 seconds - for network operations (fallback only) */
    const val NETWORK_WAIT = 10000L
}

/**
 * Sample test data for creating test scenarios
 *
 * Used to create consistent test data across multiple test cases
 */
object TestData {
    // Transaction samples
    const val SAMPLE_TRANSACTION_TITLE = "Test Transaction"
    const val SAMPLE_AMOUNT = "100.00"
    const val SAMPLE_CATEGORY = "Food"
    const val SAMPLE_CATEGORY_ID = 1L

    // Alternative samples
    const val SAMPLE_TRANSACTION_TITLE_2 = "Another Transaction"
    const val SAMPLE_AMOUNT_2 = "50.00"
    const val SAMPLE_CATEGORY_2 = "Transport"

    // Edge cases
    const val MINIMUM_AMOUNT = "0.01"
    const val LARGE_AMOUNT = "99999.99"

    // Meal vouchers
    const val SAMPLE_MEAL_VOUCHER_COUNT = "5"
    const val SAMPLE_MEAL_VOUCHER_VALUE = "5.29"
    const val SAMPLE_MEAL_VOUCHER_DIFFERENCE = "3.55"

    // Dates
    const val TODAY = "Today"
    const val YESTERDAY = "Yesterday"
    const val LAST_WEEK = "Last week"
    const val LAST_MONTH = "Last month"
}

/**
 * Error messages commonly shown in tests
 */
object TestErrorMessages {
    const val GENERIC_ERROR = "An error occurred"
    const val LOADING_FAILED = "Failed to load data"
    const val SAVE_FAILED = "Failed to save transaction"
    const val DELETE_FAILED = "Failed to delete transaction"
    const val VALIDATION_ERROR = "Please fix errors and try again"
    const val OFFLINE_ERROR = "No internet connection"
}

/**
 * Regular expressions used in tests
 */
object TestPatterns {
    const val CURRENCY_REGEX = "\\$?\\d+\\.\\d{2}"  // Matches "100.00" or "$100.00"
    const val DATE_REGEX = "\\d{1,2}/\\d{1,2}/\\d{4}"  // Matches "1/1/2024"
    const val TIME_REGEX = "\\d{1,2}:\\d{2}"  // Matches "12:30"
    const val EMAIL_REGEX = "[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})"
}
