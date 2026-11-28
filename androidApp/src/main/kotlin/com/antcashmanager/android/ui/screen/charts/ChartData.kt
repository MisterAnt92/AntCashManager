package com.antcashmanager.android.ui.screen.charts

import com.antcashmanager.android.ui.screen.charts.ChartsConstant.TREND_THRESHOLD_PERCENT
import com.antcashmanager.domain.model.PaymentType
import kotlin.math.abs

enum class TrendDirection(val symbol: String) {
    UP("↑"),
    DOWN("↓"),
    NEUTRAL("→"),
}

private fun trendOf(delta: Double?) = when {
    delta == null                    -> TrendDirection.NEUTRAL
    delta > TREND_THRESHOLD_PERCENT  -> TrendDirection.UP
    delta < -TREND_THRESHOLD_PERCENT -> TrendDirection.DOWN
    else                             -> TrendDirection.NEUTRAL
}

/**
 * Stato aggregato per i grafici.
 */
data class ChartData(
    val incomeByCategory: Map<String, Double> = emptyMap(),
    val expenseByCategory: Map<String, Double> = emptyMap(),
    val incomeCountByCategory: Map<String, Int> = emptyMap(),
    val expenseCountByCategory: Map<String, Int> = emptyMap(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyData: List<MonthlyAmount> = emptyList(),
    val yearlyData: List<YearlyAmount> = emptyList(),
    val paymentTypeBreakdown: Map<PaymentType, Double> = emptyMap(),
    val dailyTimeline: List<DailyAmount> = emptyList(),
    val expenseByWeekday: Map<Int, Double> = emptyMap(),
    val previousPeriodIncome: Double = 0.0,
    val previousPeriodExpense: Double = 0.0,
) {
    val incomeDeltaPercent: Double?
        get() = if (previousPeriodIncome == 0.0) null
                else (totalIncome - previousPeriodIncome) / abs(previousPeriodIncome) * 100.0

    val expenseDeltaPercent: Double?
        get() = if (previousPeriodExpense == 0.0) null
                else (totalExpense - previousPeriodExpense) / abs(previousPeriodExpense) * 100.0

    val incomeTrend: TrendDirection get() = trendOf(incomeDeltaPercent)
    val expenseTrend: TrendDirection get() = trendOf(expenseDeltaPercent)
}

data class MonthlyAmount(
    val label: String,
    val income: Double,
    val expense: Double,
)

data class YearlyAmount(
    val year: Int,
    val label: String,
    val income: Double,
    val expense: Double,
)

data class DailyAmount(
    val dateLabel: String,
    val expense: Double,
)
