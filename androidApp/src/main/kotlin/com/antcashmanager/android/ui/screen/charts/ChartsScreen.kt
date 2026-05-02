package com.antcashmanager.android.ui.screen.charts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.domain.usecase.share.BuildShareTextUseCase
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.components.HelpButton
import com.antcashmanager.android.ui.components.ScreenHeader
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.charts.view.BarChartLegend
import com.antcashmanager.android.ui.screen.charts.view.HelpDialog
import com.antcashmanager.android.ui.screen.charts.view.PieLegend
import com.antcashmanager.android.ui.screen.charts.view.ZoomableBarChart
import com.antcashmanager.android.ui.screen.charts.view.ZoomablePieChart
import com.antcashmanager.android.ui.screen.charts.view.ZoomableYearlyBarChart
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.DateRange
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun ChartsScreen(
    transactionRepository: TransactionRepository,
    settingsRepository: com.antcashmanager.domain.repository.SettingsRepository,
) {
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

    val screenDateFilterPreset by settingsRepository.getChartsDateFilterPreset()
        .collectAsState(initial = 1)

    val chartsZoomEnabled by settingsRepository.getChartsZoomEnabled()
        .collectAsState(initial = true)

    androidx.compose.runtime.LaunchedEffect(screenDateFilterPreset) {
        viewModel.setPresetRange(RangePreset.entries[screenDateFilterPreset])
    }

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    ChartsContent(
        chartData = chartData,
        dateRange = dateRange,
        initialPresetIndex = screenDateFilterPreset,
        zoomEnabled = chartsZoomEnabled,
        onDateRangeChanged = { from, to -> viewModel.setDateRange(from, to) },
        onPresetSelected = { preset ->
            coroutineScope.launch {
                settingsRepository.setChartsDateFilterPreset(preset.ordinal)
            }
            viewModel.setPresetRange(preset)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChartsContent(
    chartData: ChartData,
    dateRange: DateRange,
    initialPresetIndex: Int = 1,
    zoomEnabled: Boolean = true,
    onDateRangeChanged: (Long, Long) -> Unit = { _, _ -> },
    onPresetSelected: (RangePreset) -> Unit = {},
) {
    val context = LocalContext.current
    val buildShareTextUseCase = remember { BuildShareTextUseCase(context) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fmt = LocalCurrencyFormat.current
    val shareLabel = stringResource(R.string.share)
    var selectedPreset by remember { mutableIntStateOf(initialPresetIndex) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Sync selectedPreset when initialPresetIndex changes
    androidx.compose.runtime.LaunchedEffect(initialPresetIndex) {
        selectedPreset = initialPresetIndex
    }

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    top = 0.dp,
                    end = padding.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = padding.calculateBottomPadding(),
                )
                .padding(vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp), // Extra space for visibility
        ) {
            ScreenHeader(
                title = stringResource(R.string.charts_title),
                actions = { HelpButton(onHelpClick = { showHelpDialog = true }) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Period filter card
            // ... rest of Column content ...
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AppText(
                        text = stringResource(R.string.charts_period),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
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
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
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
                        IconButton(
                            onClick = { showToPicker = true },
                            modifier = Modifier.size(32.dp)
                        ) {
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
            // Summary cards with improved design and alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val balance = chartData.totalIncome - chartData.totalExpense

                SummaryCard(
                    modifier = Modifier.weight(1f),
                    state = SummaryCardState(
                        label = stringResource(R.string.charts_income),
                        amount = chartData.totalIncome,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        fmt = fmt
                    )
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    state = SummaryCardState(
                        label = stringResource(R.string.charts_expenses),
                        amount = chartData.totalExpense,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        fmt = fmt
                    )
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    state = SummaryCardState(
                        label = stringResource(R.string.share_balance),
                        amount = balance,
                        containerColor = if (balance >= 0)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (balance >= 0)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        fmt = fmt,
                        isBalance = true
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Income pie chart section
            if (chartData.incomeByCategory.isNotEmpty()) {
                val incomeCategoryShareSubject = stringResource(R.string.share_categories_subject)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppText(
                                text = stringResource(R.string.charts_income_by_category),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            IconButton(
                                onClick = {
                                    val shareText = buildShareTextUseCase.buildCategoryShareText(
                                        data = chartData.incomeByCategory,
                                        fmt = fmt,
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, incomeCategoryShareSubject)
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, shareLabel))
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = shareLabel,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        ZoomablePieChart(
                            data = chartData.incomeByCategory,
                            zoomEnabled = zoomEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PieLegend(data = chartData.incomeByCategory)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Expense pie chart section
            if (chartData.expenseByCategory.isNotEmpty()) {
                val categoryShareSubject = stringResource(R.string.share_categories_subject)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppText(
                                text = stringResource(R.string.charts_expense_by_category),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            IconButton(
                                onClick = {
                                    // Convert negative values to positive for display
                                    val positiveExpenseData =
                                        chartData.expenseByCategory.mapValues { abs(it.value) }
                                    val shareText = buildShareTextUseCase.buildCategoryShareText(
                                        data = positiveExpenseData,
                                        fmt = fmt,
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, categoryShareSubject)
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, shareLabel))
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = shareLabel,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Convert negative values to positive for pie chart display
                        ZoomablePieChart(
                            data = chartData.expenseByCategory.mapValues { abs(it.value) },
                            zoomEnabled = zoomEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Pass converted data to legend as well
                        PieLegend(data = chartData.expenseByCategory.mapValues { abs(it.value) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Bar chart section - Monthly Overview
            if (chartData.monthlyData.isNotEmpty()) {
                val monthlyShareSubject = stringResource(R.string.share_monthly_subject)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppText(
                                text = stringResource(R.string.charts_monthly_overview),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            IconButton(
                                onClick = {
                                    val shareText = buildShareTextUseCase.buildMonthlyShareText(
                                        data = chartData.monthlyData,
                                        fmt = fmt,
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, monthlyShareSubject)
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, shareLabel))
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = shareLabel,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Legend at top
                        BarChartLegend()

                        Spacer(modifier = Modifier.height(8.dp))

                        // Scrollable bar chart for many months
                        val chartWidth = (chartData.monthlyData.size * 80).coerceAtLeast(300)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            ZoomableBarChart(
                                data = chartData.monthlyData,
                                zoomEnabled = zoomEnabled,
                                modifier = Modifier
                                    .width(chartWidth.dp)
                                    .height(180.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Bar chart section - Yearly Overview
            if (chartData.yearlyData.isNotEmpty()) {
                val yearlyShareSubject = stringResource(R.string.share_yearly_subject)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppText(
                                text = stringResource(R.string.charts_yearly_overview),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            IconButton(
                                onClick = {
                                    val shareText = buildShareTextUseCase.buildYearlyShareText(
                                        data = chartData.yearlyData,
                                        fmt = fmt,
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, yearlyShareSubject)
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, shareLabel))
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = shareLabel,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Legend at top
                        BarChartLegend()

                        Spacer(modifier = Modifier.height(8.dp))

                        // Scrollable bar chart for years
                        val yearlyChartWidth = (chartData.yearlyData.size * 100).coerceAtLeast(300)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            ZoomableYearlyBarChart(
                                data = chartData.yearlyData,
                                zoomEnabled = zoomEnabled,
                                modifier = Modifier
                                    .width(yearlyChartWidth.dp)
                                    .height(200.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Empty state
            if (chartData.expenseByCategory.isEmpty() && chartData.monthlyData.isEmpty()) {
                Spacer(modifier = Modifier.height(48.dp))
                AntEmptyState(
                    mascotRes = R.drawable.ic_ant_mascot,
                    title = stringResource(R.string.charts_no_data),
                    subtitle = stringResource(R.string.charts_empty_ant),
                )
            }
        }
    } // Closing Scaffold
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
private fun SummaryCard(
    state: SummaryCardState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = state.containerColor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.label,
                style = MaterialTheme.typography.labelSmall,
                color = state.contentColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatAmount(abs(state.amount), state.fmt),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = state.contentColor,
                maxLines = 1
            )
            if (state.isBalance) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(6.dp)
                        .background(
                            if (state.amount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                            CircleShape
                        )
                )
            } else {
                // Spacer to maintain same height across all cards
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

private data class SummaryCardState(
    val label: String,
    val amount: Double,
    val containerColor: Color,
    val contentColor: Color,
    val fmt: CurrencyFormat,
    val isBalance: Boolean = false
)

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, name = "ChartsScreen - Default")
@Composable
private fun ChartsContentPreviewDefault() {
    AntCashManagerTheme(dynamicColor = false) {
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

@Preview(showBackground = true, name = "ChartsScreen - Empty")
@Composable
private fun ChartsContentPreviewEmpty() {
    AntCashManagerTheme(dynamicColor = false) {
        ChartsContent(
            chartData = ChartData(),
            dateRange = DateRange(System.currentTimeMillis(), System.currentTimeMillis()),
        )
    }
}

@Preview(showBackground = true, name = "ChartsScreen - Dark")
@Composable
private fun ChartsContentPreviewDark() {
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


// ══════════════════════════════════════════════════════════════════════════════
// ADDITIONAL PREVIEW - WITH FULL DATA
// ══════════════════════════════════════════════════════════════════════════════

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
                totalIncome = 3300.0,
                totalExpense = 750.0,
                monthlyData = listOf(
                    MonthlyAmount("Jan 26", 2000.0, 800.0),
                    MonthlyAmount("Feb 26", 2500.0, 650.0),
                    MonthlyAmount("Mar 26", 3300.0, 750.0)
                ),
                yearlyData = listOf(
                    YearlyAmount(2024, "2024", 15000.0, 8500.0),
                    YearlyAmount(2025, "2025", 18000.0, 9200.0),
                    YearlyAmount(2026, "2026", 12000.0, 6500.0)
                ),
            ),
            dateRange = DateRange(
                System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
                System.currentTimeMillis()
            ),
        )
    }
}
