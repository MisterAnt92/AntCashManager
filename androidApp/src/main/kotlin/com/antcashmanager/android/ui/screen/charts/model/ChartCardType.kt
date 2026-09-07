package com.antcashmanager.android.ui.screen.charts.model

import com.antcashmanager.android.R

/**
 * Enum defining all personalizable chart card types in Charts Screen.
 * Used for ordering/reordering persistence similar to HomeTopCardType.
 *
 * Not personalizable: Period Filter, Summary Row (Income/Expense/Balance)
 */
enum class ChartCardType(
    val storageKey: String,
    val titleResId: Int,
) {
    // New Visualizations Section (Part 1)
    SPENDING_FORECAST_CARD("spending_forecast", R.string.chart_spending_forecast_title),
    QUICK_STATS_CARD("quick_stats", R.string.chart_quick_stats_title),
    DAILY_EXPENSE_CHART_CARD("daily_expense_chart", R.string.chart_daily_expenses_title),
    WEEKDAY_DISTRIBUTION_CARD("weekday_distribution", R.string.chart_weekday_distribution_title),

    // Chart Breakdown Section
    INCOME_CATEGORY_PIE_CHART("income_pie", R.string.charts_income_by_category),
    EXPENSE_CATEGORY_PIE_CHART("expense_pie", R.string.charts_expense_by_category),
    TOP_INCOME_CATEGORIES("top_income", R.string.charts_top_income_categories),
    TOP_EXPENSE_CATEGORIES("top_expense", R.string.charts_top_expense_categories),
    PAYMENT_TYPE_BREAKDOWN("payment_breakdown", R.string.charts_payment_breakdown_title),
    MONTHLY_BAR_CHART("monthly_bar", R.string.charts_monthly_overview),
    YEARLY_BAR_CHART("yearly_bar", R.string.charts_yearly_overview),
    ;

    companion object {
        /**
         * Default order matching current screen layout:
         * 1. New visualizations (5 cards)
         * 2. Income pie chart
         * 3. Expense pie chart
         * 4. Top categories (income)
         * 5. Top categories (expense)
         * 6. Payment breakdown
         * 7. Monthly chart
         * 8. Yearly chart
         */
        val defaultOrder =
            listOf(
                SPENDING_FORECAST_CARD,
                QUICK_STATS_CARD,
                DAILY_EXPENSE_CHART_CARD,
                WEEKDAY_DISTRIBUTION_CARD,
                INCOME_CATEGORY_PIE_CHART,
                EXPENSE_CATEGORY_PIE_CHART,
                TOP_INCOME_CATEGORIES,
                TOP_EXPENSE_CATEGORIES,
                PAYMENT_TYPE_BREAKDOWN,
                MONTHLY_BAR_CHART,
                YEARLY_BAR_CHART,
            )

        /**
         * Parse serialized order string (comma-separated storage keys).
         * Missing entries are appended at the end in default order.
         */
        fun parse(raw: String): List<ChartCardType> {
            if (raw.isBlank()) return defaultOrder

            val ordered =
                raw
                    .split(',')
                    .map(String::trim)
                    .mapNotNull { key -> entries.find { it.storageKey == key } }
                    .distinct()
                    .toMutableList()

            // Append any missing types in default order
            defaultOrder.forEach { type ->
                if (type !in ordered) {
                    ordered += type
                }
            }

            return ordered
        }

        /**
         * Serialize order to comma-separated storage key string.
         */
        fun serialize(order: List<ChartCardType>): String = order.joinToString(separator = ",") { it.storageKey }
    }
}
