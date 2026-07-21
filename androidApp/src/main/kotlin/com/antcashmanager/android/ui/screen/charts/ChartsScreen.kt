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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.domain.usecase.share.BuildShareTextUseCase
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.components.HelpButton
import com.antcashmanager.android.ui.components.ScreenHeader
import com.antcashmanager.android.ui.components.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.charts.view.AmountMaskMode
import com.antcashmanager.android.ui.screen.charts.view.BarChartLegend
import com.antcashmanager.android.ui.screen.charts.view.HelpDialog
import com.antcashmanager.android.ui.screen.charts.view.PieLegend
import com.antcashmanager.android.ui.screen.charts.view.ZoomableBarChart
import com.antcashmanager.android.ui.screen.charts.view.ZoomablePieChart
import com.antcashmanager.android.ui.screen.charts.view.ZoomableYearlyBarChart
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ThemeConstants
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.PROTECTED_INCOME_CATEGORY
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.android.util.maskDigits
import com.antcashmanager.android.util.translateCategory
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.transaction.DateRange
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun ChartsScreen() {
    Logger.d(tag = "ChartsScreen") { "Displaying ChartsScreen" }
    val viewModel: ChartsViewModel = koinViewModel()
    val settingsRepository: SettingsRepository = koinInject()
    val chartData by viewModel.chartData.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val selectedPresetIndex by viewModel.selectedPresetIndex.collectAsState()

    val chartsZoomEnabled by settingsRepository.getChartsZoomEnabled()
        .collectAsState(initial = false)

    ChartsContent(
        chartData = chartData,
        dateRange = dateRange,
        initialPresetIndex = selectedPresetIndex,
        zoomEnabled = chartsZoomEnabled,
        onDateRangeChanged = { from, to -> viewModel.setDateRange(from, to) },
        onPresetSelected = viewModel::setPresetRange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChartsContent(
    chartData: ChartData,
    dateRange: DateRange,
    initialPresetIndex: Int = 1,
    zoomEnabled: Boolean = false,
    onDateRangeChanged: (Long, Long) -> Unit = { _, _ -> },
    onPresetSelected: (RangePreset) -> Unit = {},
) {
    val context = LocalContext.current
    val analyticsManager: AnalyticsManager = koinInject()
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()
    val buildShareTextUseCase = remember { BuildShareTextUseCase(context) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fmt = LocalCurrencyFormat.current
    val shareLabel = stringResource(R.string.share)
    var selectedPreset by remember { mutableIntStateOf(initialPresetIndex) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val chartCardContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh

    // Sync selectedPreset when initialPresetIndex changes
    androidx.compose.runtime.LaunchedEffect(initialPresetIndex) {
        selectedPreset = initialPresetIndex
    }

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    // Ripartizione per metodo di pagamento con etichette tradotte, pronta per essere
    // renderizzata dallo stesso CategoryPieChartCard usato per entrate/uscite.
    val electronicLabel = stringResource(R.string.payment_type_electronic)
    val cashLabel = stringResource(R.string.payment_type_cash)
    val mealVouchersLabel = stringResource(R.string.payment_type_meal_vouchers)
    val paymentBreakdownByLabel = remember(chartData.paymentTypeBreakdown, electronicLabel, cashLabel, mealVouchersLabel) {
        chartData.paymentTypeBreakdown.mapKeys { (type, _) ->
            when (type) {
                PaymentType.ELECTRONIC -> electronicLabel
                PaymentType.CASH -> cashLabel
                PaymentType.MEAL_VOUCHERS -> mealVouchersLabel
            }
        }
    }

    // Categorie principali per importo assoluto, calcolate qui (nessun dato nuovo dal ViewModel).
    val topIncomeCategories = remember(chartData.incomeByCategory) {
        chartData.incomeByCategory.entries
            .sortedByDescending { abs(it.value) }
            .take(ChartsConstant.TOP_CATEGORIES_MAX_ENTRIES)
            .map { it.key to it.value }
    }
    val topExpenseCategories = remember(chartData.expenseByCategory) {
        chartData.expenseByCategory.entries
            .sortedByDescending { abs(it.value) }
            .take(ChartsConstant.TOP_CATEGORIES_MAX_ENTRIES)
            .map { it.key to it.value }
    }

    val pieChartHeight = if (adaptiveLayoutInfo.isCompact) {
        ChartsConstant.PIE_CHART_HEIGHT_COMPACT_DP.dp
    } else {
        ChartsConstant.PIE_CHART_HEIGHT_TABLET_DP.dp
    }
    val monthlyBarChartHeight = if (adaptiveLayoutInfo.isCompact) {
        ChartsConstant.BAR_CHART_HEIGHT_COMPACT_DP.dp
    } else {
        ChartsConstant.BAR_CHART_HEIGHT_TABLET_DP.dp
    }
    val yearlyBarChartHeight = if (adaptiveLayoutInfo.isCompact) {
        ChartsConstant.YEARLY_BAR_CHART_HEIGHT_COMPACT_DP.dp
    } else {
        ChartsConstant.YEARLY_BAR_CHART_HEIGHT_TABLET_DP.dp
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
                title = stringResource(R.string.common_charts),
                actions = {
                    HelpButton(
                        onHelpClick = {
                            analyticsManager.logEvent("chart_help_opened")
                            showHelpDialog = true
                        },
                    )
                },
            )
            Spacer(modifier = Modifier.height(16.dp))

            PeriodFilterCard(
                chartCardContainerColor = chartCardContainerColor,
                selectedPreset = selectedPreset,
                dateRange = dateRange,
                dateFormat = dateFormat,
                onPresetSelected = { index, preset ->
                    selectedPreset = index
                    analyticsManager.logEvent("chart_date_filter_changed")
                    onPresetSelected(preset)
                },
                onShowFromPicker = { showFromPicker = true },
                onShowToPicker = { showToPicker = true },
            )
            Spacer(modifier = Modifier.height(16.dp))

            ChartsSummaryRow(chartData = chartData, fmt = fmt)
            Spacer(modifier = Modifier.height(20.dp))

            if (adaptiveLayoutInfo.isCompact) {
                // Telefono: colonna singola, ordine invariato + nuove sezioni in coda.
                if (chartData.incomeByCategory.isNotEmpty()) {
                    CategoryPieChartCard(
                        title = stringResource(R.string.charts_income_by_category),
                        data = chartData.incomeByCategory,
                        maskMode = AmountMaskMode.PROTECT_SALARY,
                        translateKeys = true,
                        shareSubjectRes = R.string.share_categories_subject,
                        chartHeight = pieChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        buildShareTextUseCase = buildShareTextUseCase,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (chartData.expenseByCategory.isNotEmpty()) {
                    CategoryPieChartCard(
                        title = stringResource(R.string.charts_expense_by_category),
                        data = chartData.expenseByCategory,
                        maskMode = AmountMaskMode.NONE,
                        translateKeys = true,
                        shareSubjectRes = R.string.share_categories_subject,
                        chartHeight = pieChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        buildShareTextUseCase = buildShareTextUseCase,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (topIncomeCategories.isNotEmpty()) {
                    TopCategoriesCard(
                        title = stringResource(R.string.charts_top_income_categories),
                        entries = topIncomeCategories,
                        maskMode = AmountMaskMode.PROTECT_SALARY,
                        fmt = fmt,
                        chartCardContainerColor = chartCardContainerColor,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (topExpenseCategories.isNotEmpty()) {
                    TopCategoriesCard(
                        title = stringResource(R.string.charts_top_expense_categories),
                        entries = topExpenseCategories,
                        maskMode = AmountMaskMode.NONE,
                        fmt = fmt,
                        chartCardContainerColor = chartCardContainerColor,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (paymentBreakdownByLabel.isNotEmpty()) {
                    CategoryPieChartCard(
                        title = stringResource(R.string.charts_payment_breakdown_title),
                        data = paymentBreakdownByLabel,
                        maskMode = AmountMaskMode.ALL,
                        translateKeys = false,
                        shareSubjectRes = R.string.share_categories_subject,
                        chartHeight = pieChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        buildShareTextUseCase = buildShareTextUseCase,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (chartData.monthlyData.isNotEmpty()) {
                    MonthlyBarChartCard(
                        data = chartData.monthlyData,
                        chartHeight = monthlyBarChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        buildShareTextUseCase = buildShareTextUseCase,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (chartData.yearlyData.isNotEmpty()) {
                    YearlyBarChartCard(
                        data = chartData.yearlyData,
                        chartHeight = yearlyBarChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        buildShareTextUseCase = buildShareTextUseCase,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                    )
                }
            } else {
                // Tablet (isMedium 600-839dp, isExpanded >=840dp): più dati visibili
                // affiancando le sezioni invece di impilarle.
                val showPaymentColumn = adaptiveLayoutInfo.isExpanded && paymentBreakdownByLabel.isNotEmpty()
                val hasIncomeColumn = chartData.incomeByCategory.isNotEmpty() || topIncomeCategories.isNotEmpty()
                val hasExpenseColumn = chartData.expenseByCategory.isNotEmpty() || topExpenseCategories.isNotEmpty()

                if (hasIncomeColumn || hasExpenseColumn || showPaymentColumn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ChartsConstant.TABLET_COLUMNS_SPACING_DP.dp),
                    ) {
                        if (hasIncomeColumn) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                if (chartData.incomeByCategory.isNotEmpty()) {
                                    CategoryPieChartCard(
                                        title = stringResource(R.string.charts_income_by_category),
                                        data = chartData.incomeByCategory,
                                        maskMode = AmountMaskMode.PROTECT_SALARY,
                                        translateKeys = true,
                                        shareSubjectRes = R.string.share_categories_subject,
                                        chartHeight = pieChartHeight,
                                        zoomEnabled = zoomEnabled,
                                        chartCardContainerColor = chartCardContainerColor,
                                        buildShareTextUseCase = buildShareTextUseCase,
                                        fmt = fmt,
                                        shareLabel = shareLabel,
                                        context = context,
                                        onShared = { analyticsManager.logEvent("chart_shared") },
                                    )
                                }
                                if (topIncomeCategories.isNotEmpty()) {
                                    TopCategoriesCard(
                                        title = stringResource(R.string.charts_top_income_categories),
                                        entries = topIncomeCategories,
                                        maskMode = AmountMaskMode.PROTECT_SALARY,
                                        fmt = fmt,
                                        chartCardContainerColor = chartCardContainerColor,
                                    )
                                }
                            }
                        }
                        if (hasExpenseColumn) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                if (chartData.expenseByCategory.isNotEmpty()) {
                                    CategoryPieChartCard(
                                        title = stringResource(R.string.charts_expense_by_category),
                                        data = chartData.expenseByCategory,
                                        maskMode = AmountMaskMode.NONE,
                                        translateKeys = true,
                                        shareSubjectRes = R.string.share_categories_subject,
                                        chartHeight = pieChartHeight,
                                        zoomEnabled = zoomEnabled,
                                        chartCardContainerColor = chartCardContainerColor,
                                        buildShareTextUseCase = buildShareTextUseCase,
                                        fmt = fmt,
                                        shareLabel = shareLabel,
                                        context = context,
                                        onShared = { analyticsManager.logEvent("chart_shared") },
                                    )
                                }
                                if (topExpenseCategories.isNotEmpty()) {
                                    TopCategoriesCard(
                                        title = stringResource(R.string.charts_top_expense_categories),
                                        entries = topExpenseCategories,
                                        maskMode = AmountMaskMode.NONE,
                                        fmt = fmt,
                                        chartCardContainerColor = chartCardContainerColor,
                                    )
                                }
                            }
                        }
                        if (showPaymentColumn) {
                            Column(modifier = Modifier.weight(1f)) {
                                CategoryPieChartCard(
                                    title = stringResource(R.string.charts_payment_breakdown_title),
                                    data = paymentBreakdownByLabel,
                                    maskMode = AmountMaskMode.ALL,
                                    translateKeys = false,
                                    shareSubjectRes = R.string.share_categories_subject,
                                    chartHeight = pieChartHeight,
                                    zoomEnabled = zoomEnabled,
                                    chartCardContainerColor = chartCardContainerColor,
                                    buildShareTextUseCase = buildShareTextUseCase,
                                    fmt = fmt,
                                    shareLabel = shareLabel,
                                    context = context,
                                    onShared = { analyticsManager.logEvent("chart_shared") },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Su isMedium la ripartizione pagamenti non entra come terza colonna:
                // la mostriamo a piena larghezza sotto le prime due.
                if (paymentBreakdownByLabel.isNotEmpty() && !showPaymentColumn) {
                    CategoryPieChartCard(
                        title = stringResource(R.string.charts_payment_breakdown_title),
                        data = paymentBreakdownByLabel,
                        maskMode = AmountMaskMode.ALL,
                        translateKeys = false,
                        shareSubjectRes = R.string.share_categories_subject,
                        chartHeight = pieChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        buildShareTextUseCase = buildShareTextUseCase,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (chartData.monthlyData.isNotEmpty() || chartData.yearlyData.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ChartsConstant.TABLET_COLUMNS_SPACING_DP.dp),
                    ) {
                        if (chartData.monthlyData.isNotEmpty()) {
                            Column(modifier = Modifier.weight(1f)) {
                                MonthlyBarChartCard(
                                    data = chartData.monthlyData,
                                    chartHeight = monthlyBarChartHeight,
                                    zoomEnabled = zoomEnabled,
                                    chartCardContainerColor = chartCardContainerColor,
                                    buildShareTextUseCase = buildShareTextUseCase,
                                    fmt = fmt,
                                    shareLabel = shareLabel,
                                    context = context,
                                    onShared = { analyticsManager.logEvent("chart_shared") },
                                )
                            }
                        }
                        if (chartData.yearlyData.isNotEmpty()) {
                            Column(modifier = Modifier.weight(1f)) {
                                YearlyBarChartCard(
                                    data = chartData.yearlyData,
                                    chartHeight = yearlyBarChartHeight,
                                    zoomEnabled = zoomEnabled,
                                    chartCardContainerColor = chartCardContainerColor,
                                    buildShareTextUseCase = buildShareTextUseCase,
                                    fmt = fmt,
                                    shareLabel = shareLabel,
                                    context = context,
                                    onShared = { analyticsManager.logEvent("chart_shared") },
                                )
                            }
                        }
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
                    state.selectedDateMillis?.let {
                        analyticsManager.logEvent("chart_custom_date_range_set")
                        onDateRangeChanged(it, dateRange.to)
                    }
                    selectedPreset = -1
                    showFromPicker = false
                }) { AppText(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFromPicker = false
                }) { AppText(stringResource(R.string.common_cancel)) }
            },
        ) { DatePicker(state = state) }
    }
    if (showToPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateRange.to)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        analyticsManager.logEvent("chart_custom_date_range_set")
                        onDateRangeChanged(dateRange.from, it)
                    }
                    selectedPreset = -1
                    showToPicker = false
                }) { AppText(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showToPicker = false
                }) { AppText(stringResource(R.string.common_cancel)) }
            },
        ) { DatePicker(state = state) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTIONS
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodFilterCard(
    chartCardContainerColor: Color,
    selectedPreset: Int,
    dateRange: DateRange,
    dateFormat: SimpleDateFormat,
    onPresetSelected: (Int, RangePreset) -> Unit,
    onShowFromPicker: () -> Unit,
    onShowToPicker: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = chartCardContainerColor,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = stringResource(R.string.charts_period),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
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
                        onClick = { onPresetSelected(index, preset) },
                        label = {
                            AppText(
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
                AppText(
                    text = stringResource(
                        R.string.charts_from,
                        dateFormat.format(Date(dateRange.from))
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onShowFromPicker,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.charts_pick_start_date),
                        modifier = Modifier.size(20.dp)
                    )
                }
                AppText(
                    text = stringResource(
                        R.string.charts_to,
                        dateFormat.format(Date(dateRange.to))
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onShowToPicker,
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
}

@Composable
private fun ChartsSummaryRow(chartData: ChartData, fmt: CurrencyFormat) {
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
                fmt = fmt,
                includesIncome = true,
            )
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            state = SummaryCardState(
                label = stringResource(R.string.charts_expenses),
                amount = chartData.totalExpense,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                fmt = fmt,
                includesIncome = false,
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
                isBalance = true,
                includesIncome = true,
            )
        )
    }
}

/**
 * Card generica per un grafico a torta (entrate/uscite per categoria o ripartizione per
 * metodo di pagamento). [translateKeys] applica [translateCategory] alle chiavi di [data]
 * (necessario solo per le categorie, non per le etichette già localizzate del metodo di
 * pagamento).
 */
@Composable
private fun CategoryPieChartCard(
    title: String,
    data: Map<String, Double>,
    translateKeys: Boolean,
    @androidx.annotation.StringRes shareSubjectRes: Int,
    chartHeight: Dp,
    zoomEnabled: Boolean,
    chartCardContainerColor: Color,
    buildShareTextUseCase: BuildShareTextUseCase,
    fmt: CurrencyFormat,
    shareLabel: String,
    context: android.content.Context,
    onShared: () -> Unit,
    maskMode: AmountMaskMode = AmountMaskMode.NONE,
) {
    val shareSubject = stringResource(shareSubjectRes)
    // L'aggregato totale del breakdown resta mascherato in blocco se può contenere entrate
    // (PROTECT_SALARY/ALL), anche se le singole voci non-stipendio sotto sono mostrate in chiaro.
    val masked = LocalAmountsMasked.current && maskMode != AmountMaskMode.NONE
    val displayData = if (translateKeys) {
        data.entries.associate { (key, value) -> translateCategory(key) to value }
    } else {
        data
    }
    val protectedCategoryLabel = if (translateKeys) translateCategory(PROTECTED_INCOME_CATEGORY) else PROTECTED_INCOME_CATEGORY
    val chartSummaryDescription = stringResource(
        R.string.charts_chart_summary_cd,
        title,
        displayData.size,
        formatAmount(displayData.values.sumOf { abs(it) }, fmt).let { if (masked) maskDigits(it) else it },
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = chartCardContainerColor,
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = {
                        val shareText = buildShareTextUseCase.buildCategoryShareText(
                            data = displayData,
                            fmt = fmt,
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        onShared()
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
                data = displayData,
                zoomEnabled = zoomEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .semantics { contentDescription = chartSummaryDescription }
            )
            Spacer(modifier = Modifier.height(12.dp))
            PieLegend(data = displayData, maskMode = maskMode, protectedCategoryLabel = protectedCategoryLabel)
        }
    }
}

/**
 * Lista compatta delle categorie principali (per importo assoluto) di entrate o uscite.
 * Utile scorciatoia visiva accanto ai grafici a torta, soprattutto su schermi piccoli
 * dove le fette della torta sono meno leggibili.
 */
@Composable
private fun TopCategoriesCard(
    title: String,
    entries: List<Pair<String, Double>>,
    fmt: CurrencyFormat,
    chartCardContainerColor: Color,
    maskMode: AmountMaskMode = AmountMaskMode.NONE,
) {
    val total = entries.sumOf { abs(it.second) }
    val maskEnabled = LocalAmountsMasked.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = chartCardContainerColor,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                entries.forEachIndexed { index, (category, amount) ->
                    val categoryLabel = translateCategory(category)
                    val percent = if (total > 0) (abs(amount) / total) * 100 else 0.0
                    val percentText = "%.0f".format(percent)
                    val masked = maskEnabled && when (maskMode) {
                        AmountMaskMode.NONE -> false
                        AmountMaskMode.PROTECT_SALARY -> category == PROTECTED_INCOME_CATEGORY
                        AmountMaskMode.ALL -> true
                    }
                    val itemDescription = stringResource(
                        R.string.charts_category_item_cd,
                        categoryLabel,
                        formatAmount(abs(amount), fmt).let { if (masked) maskDigits(it) else it },
                        percentText,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = itemDescription
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppText(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        AppText(
                            text = categoryLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        AppText(
                            text = formatAmount(abs(amount), fmt).let { if (masked) maskDigits(it) else it },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyBarChartCard(
    data: List<MonthlyAmount>,
    chartHeight: Dp,
    zoomEnabled: Boolean,
    chartCardContainerColor: Color,
    buildShareTextUseCase: BuildShareTextUseCase,
    fmt: CurrencyFormat,
    shareLabel: String,
    context: android.content.Context,
    onShared: () -> Unit,
) {
    val monthlyShareSubject = stringResource(R.string.share_monthly_subject)
    val title = stringResource(R.string.charts_monthly_overview)
    val masked = LocalAmountsMasked.current
    val chartSummaryDescription = stringResource(
        R.string.charts_chart_summary_cd,
        title,
        data.size,
        formatAmount(data.sumOf { it.income - it.expense }, fmt).let { if (masked) maskDigits(it) else it },
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = chartCardContainerColor,
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = {
                        val shareText = buildShareTextUseCase.buildMonthlyShareText(
                            data = data,
                            fmt = fmt,
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, monthlyShareSubject)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        onShared()
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
            val chartWidth = (data.size * 80).coerceAtLeast(300)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                ZoomableBarChart(
                    data = data,
                    zoomEnabled = zoomEnabled,
                    modifier = Modifier
                        .width(chartWidth.dp)
                        .height(chartHeight)
                        .semantics { contentDescription = chartSummaryDescription }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun YearlyBarChartCard(
    data: List<YearlyAmount>,
    chartHeight: Dp,
    zoomEnabled: Boolean,
    chartCardContainerColor: Color,
    buildShareTextUseCase: BuildShareTextUseCase,
    fmt: CurrencyFormat,
    shareLabel: String,
    context: android.content.Context,
    onShared: () -> Unit,
) {
    val yearlyShareSubject = stringResource(R.string.share_yearly_subject)
    val title = stringResource(R.string.charts_yearly_overview)
    val masked = LocalAmountsMasked.current
    val chartSummaryDescription = stringResource(
        R.string.charts_chart_summary_cd,
        title,
        data.size,
        formatAmount(data.sumOf { it.income - it.expense }, fmt).let { if (masked) maskDigits(it) else it },
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = chartCardContainerColor,
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = {
                        val shareText = buildShareTextUseCase.buildYearlyShareText(
                            data = data,
                            fmt = fmt,
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, yearlyShareSubject)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        onShared()
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
            val yearlyChartWidth = (data.size * 100).coerceAtLeast(300)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                ZoomableYearlyBarChart(
                    data = data,
                    zoomEnabled = zoomEnabled,
                    modifier = Modifier
                        .width(yearlyChartWidth.dp)
                        .height(chartHeight)
                        .semantics { contentDescription = chartSummaryDescription }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
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
            AppText(
                text = state.label,
                style = MaterialTheme.typography.labelSmall,
                color = state.contentColor.copy(alpha = ThemeConstants.HIGH_EMPHASIS_TEXT_ALPHA),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            AppText(
                text = formatAmount(abs(state.amount), state.fmt)
                    .let { if (LocalAmountsMasked.current && state.includesIncome) maskDigits(it) else it },
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
    val isBalance: Boolean = false,
    // Le uscite pure non possono contenere lo stipendio, quindi mostrano sempre la cifra reale;
    // saldo ed entrate restano mascherati per intero perché potrebbero includerlo.
    val includesIncome: Boolean = false,
)

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, name = "ChartsScreen - Default")
@Preview(showBackground = true, name = "ChartsScreen - 7 inch", widthDp = 600, heightDp = 960)
@Preview(showBackground = true, name = "ChartsScreen - 10 inch", widthDp = 840, heightDp = 1280)
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
@Preview(showBackground = true, name = "ChartsScreen - With Data - 7 inch", widthDp = 600, heightDp = 960)
@Preview(showBackground = true, name = "ChartsScreen - With Data - 10 inch", widthDp = 840, heightDp = 1280)
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
                paymentTypeBreakdown = mapOf(
                    PaymentType.ELECTRONIC to 500.0,
                    PaymentType.CASH to 150.0,
                    PaymentType.MEAL_VOUCHERS to 100.0,
                ),
            ),
            dateRange = DateRange(
                System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
                System.currentTimeMillis()
            ),
        )
    }
}
