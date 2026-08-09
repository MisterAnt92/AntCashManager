package com.antcashmanager.android.ui.screen.charts

import com.antcashmanager.domain.model.PaymentType

/**
 * Stato aggregato per i grafici.
 */
data class ChartData(
    val incomeByCategory: Map<String, Double> = emptyMap(),
    val expenseByCategory: Map<String, Double> = emptyMap(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyData: List<MonthlyAmount> = emptyList(),
    val yearlyData: List<YearlyAmount> = emptyList(),
    val paymentTypeBreakdown: Map<PaymentType, Double> = emptyMap(),
    val dailyTimeline: List<DailyAmount> = emptyList(),
    val expenseByWeekday: Map<Int, Double> = emptyMap(),
)

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
