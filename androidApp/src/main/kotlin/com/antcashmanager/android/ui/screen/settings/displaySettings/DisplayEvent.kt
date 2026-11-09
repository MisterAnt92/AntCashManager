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
    data class SetCurrencySymbolEvent(
        val symbol: String,
    ) : DisplayEvent()

    data class SetDecimalDigitsEvent(
        val digits: Int,
    ) : DisplayEvent()

    data class SetDecimalSeparatorEvent(
        val separator: String,
    ) : DisplayEvent()

    data class SetThousandsSeparatorEvent(
        val separator: String,
    ) : DisplayEvent()

    // Display Options
    data class SetMealVoucherValueEvent(
        val value: Double,
    ) : DisplayEvent()

    data class SetShowChartsSectionEvent(
        val show: Boolean,
    ) : DisplayEvent()

    data class SetChartsZoomEnabledEvent(
        val enabled: Boolean,
    ) : DisplayEvent()

    data class SetDateFormatEvent(
        val pattern: String,
    ) : DisplayEvent()

    data class SetShowTransactionNotesEvent(
        val show: Boolean,
    ) : DisplayEvent()

    data class SetMaskAmountsEvent(
        val mask: Boolean,
    ) : DisplayEvent()

    data class SetShowPaymentTypeBreakdownEvent(
        val show: Boolean,
    ) : DisplayEvent()

    data class SetShowQuickInsightsCardEvent(
        val show: Boolean,
    ) : DisplayEvent()

    data class SetDefaultPaymentTypeEvent(
        val paymentType: String,
    ) : DisplayEvent()

    data class SetTransactionDisplayTypeEvent(
        val displayType: TransactionDisplayType,
    ) : DisplayEvent()

    data class SetTransactionsTransactionDisplayTypeEvent(
        val displayType: TransactionDisplayType,
    ) : DisplayEvent()

    data class SetWidgetBackgroundColorEvent(
        val color: Long,
    ) : DisplayEvent()

    data class SetWidgetOpacityEvent(
        val opacity: Int,
    ) : DisplayEvent()

    data object RetryLastOperationEvent : DisplayEvent()
}
