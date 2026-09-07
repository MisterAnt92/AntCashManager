package com.antcashmanager.android.ui.screen.charts

/**
 * UDF Pattern: Events for Charts screen.
 *
 * All user interactions (date range changes, category selection) emit events
 * that the ViewModel processes to update chart state and selection.
 */
sealed class ChartEvent {
    data class SetDateRange(
        val from: Long,
        val to: Long,
    ) : ChartEvent()

    data class SetPresetRange(
        val preset: RangePreset,
    ) : ChartEvent()

    data class SelectChartCategory(
        val categoryName: String,
        val amount: Double,
        val colorHex: Long,
        val isExpense: Boolean = true,
    ) : ChartEvent()

    data object ClearChartSelection : ChartEvent()

    data object RetryLastOperation : ChartEvent()

    data class SetChartCardsOrder(
        val order: String,
    ) : ChartEvent()
}
