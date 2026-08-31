package com.antcashmanager.android.ui.screen.charts

import com.antcashmanager.android.ui.screen.charts.view.ChartDetailsData
import com.antcashmanager.domain.usecase.transaction.DateRange

/**
 * UDF Pattern: Consolidated state for Charts screen.
 *
 * Combines all chart screen state into a single data class for predictable state management
 * and easier testing.
 */
data class ChartsState(
    val dateRange: DateRange,
    val selectedPresetIndex: Int,
    val selectedChartDetails: ChartDetailsData? = null,
    val chartData: ChartData = ChartData(),
)
