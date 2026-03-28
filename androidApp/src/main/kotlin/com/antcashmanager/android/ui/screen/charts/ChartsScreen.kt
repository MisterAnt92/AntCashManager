package com.antcashmanager.android.ui.screen.home.charts

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.components.HelpButton
import com.antcashmanager.android.ui.components.HelpDialogContent
import com.antcashmanager.android.ui.components.SimpleHelpFeature
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.LocalReduceMotion
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.DateRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val pieColors = listOf(
    Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
    Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4FC3F7),
    Color(0xFFF06292), Color(0xFFDCE775), Color(0xFF4DB6AC),
    Color(0xFF7986CB), Color(0xFFA1887F), Color(0xFF90A4AE),
)

@Composable
fun ChartsScreen(transactionRepository: TransactionRepository) {
    Logger.d("ChartsScreen") { "Displaying ChartsScreen" }
    val viewModel: ChartsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ChartsViewModel(transactionRepository) as T
        },
    )
    val chartData by viewModel.chartData.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    ChartsContent(
        chartData = chartData,
        dateRange = dateRange,
        onDateRangeChanged = { from, to -> viewModel.setDateRange(from, to) },
        onPresetSelected = { viewModel.setPresetRange(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChartsContent(
    chartData: ChartData,
    dateRange: DateRange,
    onDateRangeChanged: (Long, Long) -> Unit = { _, _ -> },
    onPresetSelected: (RangePreset) -> Unit = {},
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fmt = LocalCurrencyFormat.current
    var selectedPreset by remember { mutableIntStateOf(1) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.charts_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            HelpButton(onHelpClick = { showHelpDialog = true })
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Period filter card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.charts_period),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RangePreset.entries.forEachIndexed { index, preset ->
                        FilterChip(
                            selected = selectedPreset == index,
                            onClick = {
                                selectedPreset = index
                                onPresetSelected(preset)
                            },
                            label = {
                                Text(
                                    text = stringResource(preset.labelResId),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.charts_from,
                            dateFormat.format(Date(dateRange.from))
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { showFromPicker = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.charts_pick_start_date),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.charts_to,
                            dateFormat.format(Date(dateRange.to))
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showToPicker = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.charts_pick_end_date),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        stringResource(R.string.charts_income),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = formatAmount(chartData.totalIncome, fmt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        stringResource(R.string.charts_expenses),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = formatAmount(chartData.totalExpense, fmt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        // Pie chart
        if (chartData.expenseByCategory.isNotEmpty()) {
            Text(
                stringResource(R.string.charts_expense_by_category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            PieChart(
                data = chartData.expenseByCategory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PieLegend(data = chartData.expenseByCategory)
        }
        Spacer(modifier = Modifier.height(20.dp))
        // Bar chart
        if (chartData.monthlyData.isNotEmpty()) {
            Text(
                stringResource(R.string.charts_monthly_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            BarChart(
                data = chartData.monthlyData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        if (chartData.expenseByCategory.isEmpty() && chartData.monthlyData.isEmpty()) {
            Spacer(modifier = Modifier.height(48.dp))
            AntEmptyState(
                mascotRes = R.drawable.ic_ant_mascot,
                title = stringResource(R.string.charts_no_data),
                subtitle = stringResource(R.string.charts_empty_ant),
            )
        }
    }
    // Date pickers
    if (showFromPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateRange.from)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDateRangeChanged(it, dateRange.to) }
                    selectedPreset = -1
                    showFromPicker = false
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFromPicker = false
                }) { Text(stringResource(R.string.dialog_cancel)) }
            },
        ) { DatePicker(state = state) }
    }
    if (showToPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateRange.to)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDateRangeChanged(dateRange.from, it) }
                    selectedPreset = -1
                    showToPicker = false
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showToPicker = false
                }) { Text(stringResource(R.string.dialog_cancel)) }
            },
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun PieChart(data: Map<String, Double>, modifier: Modifier = Modifier) {
    val total = data.values.sum()
    if (total == 0.0) return
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 800
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "pie"
    )
    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height) * 0.75f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f
        data.entries.forEachIndexed { index, (_, value) ->
            val sweep = (value / total * 360f * animatedProgress).toFloat()
            drawArc(
                color = pieColors[index % pieColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun PieLegend(data: Map<String, Double>) {
    val total = data.values.sum()
    val fmt = LocalCurrencyFormat.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        data.entries.forEachIndexed { index, (category, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(12.dp)) { drawCircle(color = pieColors[index % pieColors.size]) }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${formatAmount(value, fmt)} (%.0f%%)".format(value / total * 100),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BarChart(data: List<MonthlyAmount>, modifier: Modifier = Modifier) {
    val maxValue = data.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1.0)
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 800
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "bar"
    )
    Canvas(modifier = modifier) {
        val barAreaHeight = size.height - 30.dp.toPx()
        val barGroupWidth = size.width / data.size.coerceAtLeast(1)
        val barWidth = (barGroupWidth * 0.3f).coerceAtMost(40.dp.toPx())
        val gap = 4.dp.toPx()
        data.forEachIndexed { index, item ->
            val groupX = index * barGroupWidth + barGroupWidth / 2f
            val incomeH = (item.income / maxValue * barAreaHeight * animatedProgress).toFloat()
            drawRoundRect(
                color = incomeColor,
                topLeft = Offset(groupX - barWidth - gap / 2f, barAreaHeight - incomeH),
                size = Size(barWidth, incomeH),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            val expenseH = (item.expense / maxValue * barAreaHeight * animatedProgress).toFloat()
            drawRoundRect(
                color = expenseColor,
                topLeft = Offset(groupX + gap / 2f, barAreaHeight - expenseH),
                size = Size(barWidth, expenseH),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawContext.canvas.nativeCanvas.drawText(
                item.label, groupX, size.height - 4.dp.toPx(),
                Paint().apply {
                    color = labelColor.hashCode(); textSize = 10.sp.toPx(); textAlign =
                    Paint.Align.CENTER
                },
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(10.dp)) { drawCircle(color = incomeColor) }
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.charts_income), style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.width(16.dp))
        Canvas(modifier = Modifier.size(10.dp)) { drawCircle(color = expenseColor) }
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.charts_expenses), style = MaterialTheme.typography.labelSmall)
    }
}

// Previews
@Preview(showBackground = true, name = "ChartsScreen - With Data")
@Composable
private fun ChartsContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ChartsContent(
            chartData = ChartData(
                incomeByCategory = mapOf("Work" to 2500.0, "Freelance" to 800.0),
                expenseByCategory = mapOf(
                    "Food" to 350.0,
                    "Transport" to 120.0,
                    "Entertainment" to 80.0,
                    "Utilities" to 200.0
                ),
                totalIncome = 3300.0, totalExpense = 750.0,
                monthlyData = listOf(
                    MonthlyAmount("Jan 26", 2000.0, 800.0),
                    MonthlyAmount("Feb 26", 2500.0, 650.0),
                    MonthlyAmount("Mar 26", 3300.0, 750.0)
                ),
            ),
            dateRange = DateRange(
                System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
                System.currentTimeMillis()
            ),
        )
    }
}

@Preview(showBackground = true, name = "ChartsScreen - Empty")
@Composable
private fun ChartsContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ChartsContent(
            chartData = ChartData(),
            dateRange = DateRange(
                System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
                System.currentTimeMillis()
            )
        )
    }
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        SimpleHelpFeature(
            title = "Visualizzazione Grafici",
            description = "Vedi i tuoi dati finanziari in formato grafico con pie chart per le categorie",
            icon = Icons.Default.BarChart,
        ),
        SimpleHelpFeature(
            title = "Filtri Temporali",
            description = "Seleziona periodi predefiniti o personalizzati per analizzare i tuoi dati",
            icon = Icons.Default.CalendarMonth,
        ),
        SimpleHelpFeature(
            title = "Analisi Dettagliata",
            description = "Visualizza il riepilogo mensile e l'analisi per categoria",
            icon = Icons.Default.TrendingUp,
        ),
    )

    HelpDialogContent(
        isVisible = true,
        title = "Guida Grafici",
        description = "Visualizza grafici e analisi dei tuoi dati finanziari!",
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}

@Preview(showBackground = true, name = "ChartsScreen - Default")
@Composable
private fun ChartsContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ChartsContent(
            chartData = ChartData(
                expenseByCategory = mapOf("Food" to 350.0, "Transport" to 120.0),
                totalIncome = 2000.0,
                totalExpense = 470.0,
                monthlyData = listOf(MonthlyAmount("Feb 26", 2000.0, 470.0))
            ),
            dateRange = DateRange(
                System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
                System.currentTimeMillis()
            ),
        )
    }
}
