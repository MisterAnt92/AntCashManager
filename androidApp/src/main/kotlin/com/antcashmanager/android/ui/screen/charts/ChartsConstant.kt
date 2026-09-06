package com.antcashmanager.android.ui.screen.charts

/**
 * Shared constants for the Charts feature.
 */
object ChartsConstant {
    const val TABLET_COLUMNS_SPACING_DP = 16
    const val TOP_CATEGORIES_MAX_ENTRIES = 5

    const val PIE_CHART_HEIGHT_COMPACT_DP = 200
    const val PIE_CHART_HEIGHT_TABLET_DP = 280
    const val BAR_CHART_HEIGHT_COMPACT_DP = 180
    const val BAR_CHART_HEIGHT_MEDIUM_DP = 220
    const val YEARLY_BAR_CHART_HEIGHT_COMPACT_DP = 200
    const val BAR_CHART_HEIGHT_TABLET_DP = 260
    const val YEARLY_BAR_CHART_HEIGHT_TABLET_DP = 280

    // New chart constants
    const val LINE_CHART_HEIGHT_COMPACT_DP = 180
    const val LINE_CHART_HEIGHT_TABLET_DP = 240
    const val WEEKDAY_CHART_HEIGHT_COMPACT_DP = 160
    const val WEEKDAY_CHART_HEIGHT_TABLET_DP = 220
    const val FORECAST_MONTHS_LOOKBACK = 3
    const val SAVINGS_GAUGE_STROKE_DP = 14

    // Adaptive layout constants
    const val BAR_CHART_FOLDABLE_MIN_WIDTH_DP = 240
    const val PERIOD_FILTER_CHIP_SPACING_DP = 4

    // Default order of charts cards for persistence
    // Format: comma-separated storage keys that match ChartCardType.storageKey
    // Note: PERIOD FILTER is NOT included here - it's always fixed at the top
    const val DEFAULT_CHARTS_CARDS_ORDER =
        "spending_forecast,quick_stats,daily_expense_chart,weekday_distribution," +
            "income_pie,expense_pie,top_income,top_expense,payment_breakdown," +
            "monthly_bar,yearly_bar"
}
