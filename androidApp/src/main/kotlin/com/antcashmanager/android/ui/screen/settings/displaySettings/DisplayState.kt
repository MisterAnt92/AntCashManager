package com.antcashmanager.android.ui.screen.settings.displaySettings

import com.antcashmanager.domain.model.TransactionDisplayType

/**
 * Consolidated state for Display Settings screen.
 *
 * Replaces 16+ separate StateFlow with a single state object:
 * - Number formatting: currency, decimals, separators
 * - Display options: charts, zoom, dates, notes, masking, breakdown, insights
 * - Payment settings: default type, widget colors
 * - Transaction visualization
 */
data class DisplayState(
    // ── Number Formatting ──
    val currencySymbol: String = DisplayConstant.DEFAULT_CURRENCY_SYMBOL,
    val decimalDigits: Int = DisplayConstant.DEFAULT_DECIMAL_DIGITS,
    val decimalSeparator: String = DisplayConstant.DEFAULT_DECIMAL_SEPARATOR,
    val thousandsSeparator: String = DisplayConstant.DEFAULT_THOUSANDS_SEPARATOR,
    // ── Display Options ──
    val mealVoucherValue: Double = DisplayConstant.DEFAULT_MEAL_VOUCHER_VALUE,
    val showChartsSection: Boolean = DisplayConstant.DEFAULT_SHOW_CHARTS_SECTION,
    val chartsZoomEnabled: Boolean = DisplayConstant.DEFAULT_SHOW_CHARTS_ZOOM,
    val dateFormat: String = DisplayConstant.DEFAULT_DATE_FORMAT,
    val showTransactionNotes: Boolean = DisplayConstant.DEFAULT_SHOW_TRANSACTION_NOTES,
    val maskAmounts: Boolean = DisplayConstant.DEFAULT_MASK_AMOUNTS,
    val showPaymentTypeBreakdown: Boolean = DisplayConstant.DEFAULT_SHOW_PAYMENT_BREAKDOWN,
    val showQuickInsightsCard: Boolean = DisplayConstant.DEFAULT_SHOW_QUICK_INSIGHTS_CARD,
    // ── Payment Settings ──
    val defaultPaymentType: String = DisplayConstant.DEFAULT_PAYMENT_TYPE,
    // ── Transaction Display ──
    val transactionDisplayType: TransactionDisplayType = DisplayConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE,
    val transactionsTransactionDisplayType: TransactionDisplayType = DisplayConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE,
    // ── Widget Settings ──
    val widgetBackgroundColor: Long = DisplayConstant.DEFAULT_WIDGET_BACKGROUND_COLOR,
    val widgetOpacity: Int = DisplayConstant.DEFAULT_WIDGET_OPACITY,
)
