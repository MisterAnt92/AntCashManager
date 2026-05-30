package com.antcashmanager.android.ui.screen.settingsDisplay

import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.TransactionDisplayType

/**
 * Shared constants for the Display settings feature.
 */
object DisplayConstant {
    const val TAG = "DisplayViewModel"
    const val SHARING_TIMEOUT = 5_000L

    val DEFAULT_CURRENCY_SYMBOL: String = CurrencyFormat.DEFAULT.currencySymbol
    val DEFAULT_DECIMAL_DIGITS: Int = CurrencyFormat.DEFAULT.decimalDigits
    val DEFAULT_DECIMAL_SEPARATOR: String = CurrencyFormat.DEFAULT.decimalSeparator
    val DEFAULT_THOUSANDS_SEPARATOR: String = CurrencyFormat.DEFAULT.thousandsSeparator
    const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy"
    const val DEFAULT_SHOW_TRANSACTION_NOTES = true
    const val DEFAULT_SHOW_CHARTS_SECTION = true
    const val DEFAULT_SHOW_CHARTS_ZOOM = true
    const val DEFAULT_SHOW_PAYMENT_BREAKDOWN = false
    val DEFAULT_TRANSACTION_DISPLAY_TYPE: TransactionDisplayType = TransactionDisplayType.TREND

    val SUPPORTED_CURRENCY_SYMBOLS: Set<String> =
        CurrencyFormat.SUPPORTED_CURRENCIES.map { it.first }.toSet()
    val SUPPORTED_DECIMAL_SEPARATORS: Set<String> =
        CurrencyFormat.DECIMAL_SEPARATORS.map { it.first }.toSet()
    val SUPPORTED_THOUSANDS_SEPARATORS: Set<String> =
        CurrencyFormat.THOUSANDS_SEPARATORS.map { it.first }.toSet()

    const val CONTENT_HORIZONTAL_PADDING_DP = 16
    const val CONTENT_TOP_PADDING_DP = 12
    const val CONTENT_BOTTOM_PADDING_DP = 24
    const val TABLET_COLUMNS_SPACING_DP = 16
}
