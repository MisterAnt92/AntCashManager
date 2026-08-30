package com.antcashmanager.android.ui.screen.settings.displaySettings

import com.antcashmanager.domain.model.TransactionDisplayType

/**
 * UDF Pattern: Events for Display screen.
 *
 * All display preference changes emit events that the ViewModel processes
 * to persist and update display state.
 */
sealed class DisplayEvent {
    // Number Formatting
    data class SetCurrencySymbol(val symbol: String) : DisplayEvent()
    data class SetDecimalDigits(val digits: Int) : DisplayEvent()
    data class SetDecimalSeparator(val separator: String) : DisplayEvent()
    data class SetThousandsSeparator(val separator: String) : DisplayEvent()
    
    // Display Options
    data class SetMealVoucherValue(val value: Double) : DisplayEvent()
    data class SetShowChartsSection(val show: Boolean) : DisplayEvent()
    data class SetChartsZoomEnabled(val enabled: Boolean) : DisplayEvent()
    data class SetDateFormat(val pattern: String) : DisplayEvent()
    data class SetShowTransactionNotes(val show: Boolean) : DisplayEvent()
    data class SetMaskAmounts(val mask: Boolean) : DisplayEvent()
    data class SetShowPaymentTypeBreakdown(val show: Boolean) : DisplayEvent()
    data class SetShowQuickInsightsCard(val show: Boolean) : DisplayEvent()
    data class SetDefaultPaymentType(val paymentType: String) : DisplayEvent()
    data class SetTransactionDisplayType(val displayType: TransactionDisplayType) : DisplayEvent()
    data class SetShowInitialAnimation(val show: Boolean) : DisplayEvent()
    data class SetWidgetBackgroundColor(val color: Long) : DisplayEvent()
    data class SetWidgetOpacity(val opacity: Int) : DisplayEvent()
    
    data object RetryLastOperation : DisplayEvent()
}
