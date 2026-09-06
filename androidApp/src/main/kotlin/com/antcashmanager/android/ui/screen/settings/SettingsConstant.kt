package com.antcashmanager.android.ui.screen.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.TransactionDisplayType

/**
 * Shared constants for the Settings feature.
 */
object SettingsConstant {
    const val SHARING_TIMEOUT = 5_000L

    val DEFAULT_THEME: AppTheme = AppTheme.SYSTEM
    val DEFAULT_LANGUAGE: AppLanguage = AppLanguage.SYSTEM

    const val DEFAULT_SHOW_CHARTS = true
    const val DEFAULT_HIGH_CONTRAST = false
    const val DEFAULT_LARGE_TEXT = false
    const val DEFAULT_REDUCE_MOTION = false

    const val DEFAULT_CURRENCY_SYMBOL = "\u20ac"
    const val DEFAULT_DECIMAL_DIGITS = 2
    const val DEFAULT_DECIMAL_SEPARATOR = ","
    const val DEFAULT_THOUSANDS_SEPARATOR = "."
    const val DEFAULT_SHOW_TRANSACTION_NOTES = true

    val DEFAULT_TRANSACTION_DISPLAY_TYPE: TransactionDisplayType = TransactionDisplayType.CATEGORY

    const val DEBUG_ASSET_NAME = "debug_initial_data.json"

    const val JSON_KEY_TRANSACTIONS = "transactions"
    const val JSON_KEY_ID = "id"
    const val JSON_KEY_TITLE = "title"
    const val JSON_KEY_AMOUNT = "amount"
    const val JSON_KEY_CATEGORY = "category"
    const val JSON_KEY_TYPE = "type"
    const val JSON_KEY_TIMESTAMP = "timestamp"
    const val JSON_KEY_NOTES = "notes"
    const val JSON_KEY_PAYEE = "payee"
    const val JSON_KEY_LOCATION = "location"
    const val JSON_KEY_IS_RECURRING = "isRecurring"
    const val JSON_KEY_TAGS = "tags"
    const val JSON_KEY_RECURRENCE_RULE = "recurrenceRule"
    const val JSON_KEY_PAYMENT_TYPE = "paymentType"

    const val DEFAULT_TRANSACTION_TITLE = "Senza titolo"
    const val DEFAULT_TRANSACTION_CATEGORY = "Uncategorized"
    const val DEFAULT_TRANSACTION_TYPE = "EXPENSE"
    const val DEFAULT_PAYMENT_TYPE = "ELECTRONIC"
}
