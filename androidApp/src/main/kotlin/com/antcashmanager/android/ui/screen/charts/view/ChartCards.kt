package com.antcashmanager.android.ui.screen.charts.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.charts.ChartData
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount

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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_spending_forecast_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            VerticalSpacer(SpacingSize.XS)
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_quick_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            VerticalSpacer(SpacingSize.SM)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Days Tracked
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            this.contentDescription = "Days tracked: $totalDays"
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppText(
                        text = stringResource(id = R.string.chart_days_tracked),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    VerticalSpacer(SpacingSize.XXS)
                    AppText(
                        text = totalDays.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Max Daily Expense
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            this.contentDescription = "Max daily: ${formatAmount(maxDailyExpense, format)}"
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppText(
                        text = stringResource(id = R.string.chart_max_daily),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    VerticalSpacer(SpacingSize.XXS)
                    AppText(
                        text = formatAmount(maxDailyExpense, format),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Average Daily Expense
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            this.contentDescription = "Average daily: ${formatAmount(avgDailyExpense, format)}"
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppText(
                        text = stringResource(id = R.string.chart_avg_daily),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    VerticalSpacer(SpacingSize.XXS)
                    AppText(
                        text = formatAmount(avgDailyExpense, format),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_daily_expenses_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            VerticalSpacer(SpacingSize.SM)
            ExpenseLineChart(
                dailyExpenses = displayData.map { it.expense },
                modifier = Modifier.fillMaxWidth()
            )
            VerticalSpacer(SpacingSize.SM)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AppText(text = stringResource(id = R.string.chart_last_14_days), style = MaterialTheme.typography.bodySmall)
                AppText(text = "${stringResource(id = R.string.chart_average)}: ${formatAmount(avgExpense, format)}", style = MaterialTheme.typography.bodySmall)
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
    val format = LocalCurrencyFormat.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(id = R.string.chart_weekday_distribution_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            VerticalSpacer(SpacingSize.SM)
            WeekdayBarChart(
                expenseByWeekday = chartData.expenseByWeekday,
                modifier = Modifier.fillMaxWidth()
            )
            VerticalSpacer(SpacingSize.SM)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..7).forEach { day ->
                    val expense = chartData.expenseByWeekday[day] ?: 0.0
                    val dayLabel = weekdayLabels.getOrNull(day - 1) ?: "?"
                    val amountFormatted = if (expense > 0.0) formatAmount(expense, format) else "—"

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) {
                                this.contentDescription = "$dayLabel: $amountFormatted"
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppText(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        VerticalSpacer(SpacingSize.XXXS)
                        AppText(
                            text = amountFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (expense > 0.0) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
