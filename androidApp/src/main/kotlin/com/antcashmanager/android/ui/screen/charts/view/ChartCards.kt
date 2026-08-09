package com.antcashmanager.android.ui.screen.charts.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.charts.ChartData
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount

/**
 * Placeholder card structure for new chart visualizations.
 * These will be fully implemented with Canvas components in Step 4.
 */
@Composable
internal fun SavingsRateCard(
    chartData: ChartData,
    modifier: Modifier = Modifier,
) {
    if (chartData.totalIncome <= 0.0 || chartData.totalExpense <= 0.0) {
        return
    }

    val savingsRate = if (chartData.totalIncome > 0) {
        ((chartData.totalIncome - chartData.totalExpense) / chartData.totalIncome * 100).toInt()
            .coerceIn(0, 100)
    } else {
        0
    }

    val format = LocalCurrencyFormat.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_savings_rate_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AppText(text = "${stringResource(id = R.string.chart_savings_percentage)}: $savingsRate%")
                AppText(text = formatAmount(chartData.totalIncome, format))
            }
        }
    }
}

@Composable
internal fun SpendingForecastCard(
    chartData: ChartData,
    modifier: Modifier = Modifier,
) {
    if (chartData.monthlyData.isEmpty()) return

    val last3Months = chartData.monthlyData.takeLast(3)
    val averageMonthlyExpense = if (last3Months.isNotEmpty()) {
        last3Months.map { it.expense }.average()
    } else {
        0.0
    }

    val format = LocalCurrencyFormat.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_spending_forecast_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AppText(text = stringResource(id = R.string.chart_avg_monthly))
                AppText(text = formatAmount(averageMonthlyExpense, format))
            }
        }
    }
}

@Composable
internal fun QuickStatsCard(
    chartData: ChartData,
    modifier: Modifier = Modifier,
) {
    if (chartData.dailyTimeline.isEmpty()) return

    val totalDays = chartData.dailyTimeline.size
    val maxDailyExpense = chartData.dailyTimeline.maxOfOrNull { it.expense } ?: 0.0
    val avgDailyExpense = if (totalDays > 0) {
        chartData.dailyTimeline.map { it.expense }.average()
    } else {
        0.0
    }

    val format = LocalCurrencyFormat.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_quick_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppText(text = stringResource(id = R.string.chart_days_tracked), style = MaterialTheme.typography.labelSmall)
                    AppText(text = totalDays.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppText(text = stringResource(id = R.string.chart_max_daily), style = MaterialTheme.typography.labelSmall)
                    AppText(text = formatAmount(maxDailyExpense, format), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppText(text = stringResource(id = R.string.chart_avg_daily), style = MaterialTheme.typography.labelSmall)
                    AppText(text = formatAmount(avgDailyExpense, format), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun DailyExpenseLineChartCard(
    chartData: ChartData,
    modifier: Modifier = Modifier,
) {
    if (chartData.dailyTimeline.isEmpty()) return

    val displayData = chartData.dailyTimeline.takeLast(14)
    val avgExpense = if (displayData.isNotEmpty()) displayData.map { it.expense }.average() else 0.0
    val format = LocalCurrencyFormat.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_daily_expenses_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AppText(text = stringResource(id = R.string.chart_last_14_days))
                AppText(text = "${stringResource(id = R.string.chart_average)}: ${formatAmount(avgExpense, format)}")
            }
        }
    }
}

@Composable
internal fun WeekdayExpenseCard(
    chartData: ChartData,
    modifier: Modifier = Modifier,
) {
    if (chartData.expenseByWeekday.isEmpty()) return

    val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxExpense = chartData.expenseByWeekday.values.maxOrNull() ?: 1.0
    val format = LocalCurrencyFormat.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_weekday_distribution_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            (1..7).forEach { day ->
                val expense = chartData.expenseByWeekday[day] ?: 0.0
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AppText(text = weekdayLabels.getOrNull(day - 1) ?: "?", style = MaterialTheme.typography.bodySmall)
                    AppText(text = formatAmount(expense, format), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
