package com.antcashmanager.android.ui.screen.home.charts

/**
 * Stato aggregato per i grafici.
 */
data class ChartData(
    val incomeByCategory: Map<String, Double> = emptyMap(),
    val expenseByCategory: Map<String, Double> = emptyMap(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyData: List<MonthlyAmount> = emptyList(),
)

data class MonthlyAmount(
    val label: String,
    val income: Double,
    val expense: Double,
)

