package com.antcashmanager.android.ui.screen.charts

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
