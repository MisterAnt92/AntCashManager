package com.antcashmanager.android.ui.screen.charts.view

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.data.formatter.ShareTextFormatter
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import com.antcashmanager.android.navigation.ScreenHeaderConfig
import com.antcashmanager.android.ui.base.LocalMultiPaneCoordinator
import com.antcashmanager.android.ui.components.dialog.HelpButton
import com.antcashmanager.android.ui.components.layout.LocalDisplayFeatures
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.state.AntEmptyState
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.charts.ChartData
import com.antcashmanager.android.ui.screen.charts.ChartEvent
import com.antcashmanager.android.ui.screen.charts.ChartsConstant
import com.antcashmanager.android.ui.screen.charts.ChartsViewModel
import com.antcashmanager.android.ui.screen.charts.MonthlyAmount
import com.antcashmanager.android.ui.screen.charts.RangePreset
import com.antcashmanager.android.ui.screen.charts.YearlyAmount
import com.antcashmanager.android.ui.theme.ThemeConstants
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.PROTECTED_INCOME_CATEGORY
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.android.util.maskDigits
import com.antcashmanager.android.util.translateCategory
import com.antcashmanager.android.util.translateCategoryPlain
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.PaymentType
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
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val dateRange by viewModel.dateRange.collectAsStateWithLifecycle()
    val selectedPresetIndex by viewModel.selectedPresetIndex.collectAsStateWithLifecycle()
    val chartsZoomEnabled by viewModel.chartsZoomEnabled.collectAsStateWithLifecycle(initialValue = false)
    val chartsCardOrder by viewModel.chartsCardOrder.collectAsStateWithLifecycle()

    ChartsContent(
        chartData = chartData,
        dateRange = dateRange,
        initialPresetIndex = selectedPresetIndex,
        zoomEnabled = chartsZoomEnabled,
        chartsCardOrderRaw = chartsCardOrder,
        onDateRangeChanged = { from, to -> viewModel.onEvent(ChartEvent.SetDateRange(from, to)) },
        onPresetSelected = { preset -> viewModel.onEvent(ChartEvent.SetPresetRange(preset)) },
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChartsContent(
    chartData: ChartData,
    dateRange: DateRange,
    initialPresetIndex: Int = 1,
    zoomEnabled: Boolean = false,
    chartsCardOrderRaw: String,
    onDateRangeChanged: (Long, Long) -> Unit = { _, _ -> },
    onPresetSelected: (RangePreset) -> Unit = {},
    onEvent: (ChartEvent) -> Unit = {},
) {
    val context = LocalContext.current
    val analyticsManager: AnalyticsManager = koinInject()
    val scope = rememberCoroutineScope()
    // Foldable device support
    val displayFeatures = LocalDisplayFeatures.current
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo(displayFeatures = displayFeatures)
    val multiPaneCoordinator = LocalMultiPaneCoordinator.current
    val foldingFeature = adaptiveLayoutInfo.foldingFeature
    val isGridLayout = adaptiveLayoutInfo.isExpanded && !adaptiveLayoutInfo.hasFold

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fmt = LocalCurrencyFormat.current
    val shareLabel = stringResource(R.string.share)
    var selectedPreset by remember { mutableIntStateOf(initialPresetIndex) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var isVisualizationsSectionExpanded by remember { mutableStateOf(true) }
    var selectedChartDetails by remember { mutableStateOf<ChartDetailsData?>(null) }

    // Charts card ordering state - persists across session and restores from settings
    var localChartsCardOrderRaw by remember(chartsCardOrderRaw) {
        mutableStateOf(chartsCardOrderRaw)
    }
    val chartsCardOrder =
        remember(localChartsCardOrderRaw) {
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType
                .parse(localChartsCardOrderRaw)
        }
    var showChartsCardsOrderDialog by remember { mutableStateOf(false) }

    val chartCardContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh

    // Handle back press when details sheet is open
    BackHandler(enabled = selectedChartDetails != null) {
        selectedChartDetails = null
    }

    // Sync selectedPreset when initialPresetIndex changes
    LaunchedEffect(initialPresetIndex) {
        selectedPreset = initialPresetIndex
    }

    // Track chart loading completion
    LaunchedEffect(chartData) {
        if (chartData.incomeByCategory.isNotEmpty() || chartData.expenseByCategory.isNotEmpty()) {
            val totalDataPoints =
                (
                    chartData.incomeByCategory.size + chartData.expenseByCategory.size +
                        chartData.monthlyData.size + chartData.yearlyData.size
                )
            val params =
                android.os.Bundle().apply {
                    putInt("data_points", totalDataPoints)
                }
            analyticsManager.logEvent("chart_loading_completed", params)
        }
    }

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    // Configure screen header with actions
    val headerConfigCallback = LocalScreenHeaderConfigCallback.current
    val chartsTitle = stringResource(R.string.common_charts)
    LaunchedEffect(Unit) {
        headerConfigCallback?.invoke(
            ScreenHeaderConfig(
                title = chartsTitle,
                hasOrderOption = true,
                onOrderClick = { showChartsCardsOrderDialog = true },
                actions = {
                    HelpButton(
                        onHelpClick = {
                            analyticsManager.logEvent("chart_help_opened")
                            showHelpDialog = true
                        },
                    )
                },
            ),
        )
    }

    // Charts cards order dialog
    if (showChartsCardsOrderDialog) {
        ChartsCardsOrderDialog(
            order = chartsCardOrder,
            onMoveUp = { index ->
                if (index > 0) {
                    val mutableOrder = chartsCardOrder.toMutableList()
                    val temp = mutableOrder[index]
                    mutableOrder[index] = mutableOrder[index - 1]
                    mutableOrder[index - 1] = temp
                    localChartsCardOrderRaw =
                        com.antcashmanager.android.ui.screen.charts.model.ChartCardType
                            .serialize(mutableOrder)
                }
            },
            onMoveDown = { index ->
                if (index < chartsCardOrder.size - 1) {
                    val mutableOrder = chartsCardOrder.toMutableList()
                    val temp = mutableOrder[index]
                    mutableOrder[index] = mutableOrder[index + 1]
                    mutableOrder[index + 1] = temp
                    localChartsCardOrderRaw =
                        com.antcashmanager.android.ui.screen.charts.model.ChartCardType
                            .serialize(mutableOrder)
                }
            },
            onDismiss = { showChartsCardsOrderDialog = false },
            onConfirm = {
                // Persist card order to settings for backup/restore
                onEvent(ChartEvent.SetChartCardsOrder(localChartsCardOrderRaw))
                showChartsCardsOrderDialog = false
            },
        )
    }

    // Ripartizione per metodo di pagamento con etichette tradotte, pronta per essere
    // renderizzata dallo stesso CategoryPieChartCard usato per entrate/uscite.
    val electronicLabel = stringResource(R.string.payment_type_electronic)
    val cashLabel = stringResource(R.string.payment_type_cash)
    val mealVouchersLabel = stringResource(R.string.payment_type_meal_vouchers)
    val paymentBreakdownByLabel =
        remember(chartData.paymentTypeBreakdown, electronicLabel, cashLabel, mealVouchersLabel) {
            chartData.paymentTypeBreakdown.mapKeys { (type, _) ->
                when (type) {
                    PaymentType.ELECTRONIC -> electronicLabel
                    PaymentType.CASH -> cashLabel
                    PaymentType.MEAL_VOUCHERS -> mealVouchersLabel
                }
            }
        }

    // Categorie principali per importo assoluto, calcolate qui (nessun dato nuovo dal ViewModel).
    val topIncomeCategories =
        remember(chartData.incomeByCategory) {
            chartData.incomeByCategory.entries
                .sortedByDescending { abs(it.value) }
                .take(ChartsConstant.TOP_CATEGORIES_MAX_ENTRIES)
                .map { it.key to it.value }
        }
    val topExpenseCategories =
        remember(chartData.expenseByCategory) {
            chartData.expenseByCategory.entries
                .sortedByDescending { abs(it.value) }
                .take(ChartsConstant.TOP_CATEGORIES_MAX_ENTRIES)
                .map { it.key to it.value }
        }

    val pieChartHeight =
        if (adaptiveLayoutInfo.isCompact) {
            ChartsConstant.PIE_CHART_HEIGHT_COMPACT_DP.dp
        } else {
            ChartsConstant.PIE_CHART_HEIGHT_TABLET_DP.dp
        }
    val monthlyBarChartHeight =
        if (adaptiveLayoutInfo.isCompact) {
            ChartsConstant.BAR_CHART_HEIGHT_COMPACT_DP.dp
        } else {
            ChartsConstant.BAR_CHART_HEIGHT_TABLET_DP.dp
        }
    val yearlyBarChartHeight =
        if (adaptiveLayoutInfo.isCompact) {
            ChartsConstant.YEARLY_BAR_CHART_HEIGHT_COMPACT_DP.dp
        } else {
            ChartsConstant.YEARLY_BAR_CHART_HEIGHT_TABLET_DP.dp
        }

    // Function to render a single chart card based on type
    @Composable
    fun RenderChartCard(
        cardType: com.antcashmanager.android.ui.screen.charts.model.ChartCardType,
        showSpacer: Boolean = true,
    ) {
        when (cardType) {
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.SPENDING_FORECAST_CARD -> {
                SpendingForecastCard(chartData = chartData)
                if (showSpacer) VerticalSpacer(SpacingSize.MD)
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.QUICK_STATS_CARD -> {
                QuickStatsCard(chartData = chartData)
                if (showSpacer) VerticalSpacer(SpacingSize.MD)
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.DAILY_EXPENSE_CHART_CARD -> {
                DailyExpenseLineChartCard(chartData = chartData)
                if (showSpacer) VerticalSpacer(SpacingSize.MD)
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.WEEKDAY_DISTRIBUTION_CARD -> {
                WeekdayExpenseCard(chartData = chartData)
                if (showSpacer) VerticalSpacer(SpacingSize.MD)
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.INCOME_CATEGORY_PIE_CHART -> {
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
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                        onCategorySelected = { category, amount, color ->
                            val params =
                                android.os.Bundle().apply {
                                    putString("chart_type", "pie_income")
                                    putString("category", category)
                                }
                            analyticsManager.logEvent("chart_item_clicked", params)
                            selectedChartDetails =
                                ChartDetailsData(
                                    categoryName = category,
                                    amount = amount,
                                    percentage =
                                        if (chartData.totalIncome != 0.0) {
                                            ((abs(amount) / chartData.totalIncome) * 100).toInt().coerceIn(0, 100)
                                        } else {
                                            0
                                        },
                                    colorHex = color,
                                    transactionCount = 0,
                                    trend = TrendDirection.NEUTRAL,
                                )
                        },
                    )
                    if (showSpacer) VerticalSpacer(SpacingSize.MD)
                }
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.EXPENSE_CATEGORY_PIE_CHART -> {
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
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                        onCategorySelected = { category, amount, color ->
                            val params =
                                android.os.Bundle().apply {
                                    putString("chart_type", "pie_expense")
                                    putString("category", category)
                                }
                            analyticsManager.logEvent("chart_item_clicked", params)
                            selectedChartDetails =
                                ChartDetailsData(
                                    categoryName = category,
                                    amount = amount,
                                    percentage =
                                        if (chartData.totalExpense != 0.0) {
                                            ((abs(amount) / chartData.totalExpense) * 100).toInt().coerceIn(0, 100)
                                        } else {
                                            0
                                        },
                                    colorHex = color,
                                    transactionCount = 0,
                                    trend = TrendDirection.NEUTRAL,
                                )
                        },
                    )
                    if (showSpacer) VerticalSpacer(SpacingSize.MD)
                }
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.TOP_INCOME_CATEGORIES -> {
                if (topIncomeCategories.isNotEmpty()) {
                    TopCategoriesCard(
                        title = stringResource(R.string.charts_top_income_categories),
                        entries = topIncomeCategories,
                        maskMode = AmountMaskMode.PROTECT_SALARY,
                        fmt = fmt,
                        chartCardContainerColor = chartCardContainerColor,
                    )
                    if (showSpacer) VerticalSpacer(SpacingSize.MD)
                }
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.TOP_EXPENSE_CATEGORIES -> {
                if (topExpenseCategories.isNotEmpty()) {
                    TopCategoriesCard(
                        title = stringResource(R.string.charts_top_expense_categories),
                        entries = topExpenseCategories,
                        maskMode = AmountMaskMode.NONE,
                        fmt = fmt,
                        chartCardContainerColor = chartCardContainerColor,
                    )
                    if (showSpacer) VerticalSpacer(SpacingSize.MD)
                }
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.PAYMENT_TYPE_BREAKDOWN -> {
                if (paymentBreakdownByLabel.isNotEmpty()) {
                    CategoryPieChartCard(
                        title = stringResource(R.string.charts_payment_breakdown_title),
                        data = paymentBreakdownByLabel,
                        maskMode = AmountMaskMode.NONE,
                        translateKeys = false,
                        shareSubjectRes = R.string.share_categories_subject,
                        chartHeight = pieChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                        onCategorySelected = { paymentLabel, amount, color ->
                            val params =
                                android.os.Bundle().apply {
                                    putString("chart_type", "payment_breakdown")
                                    putString("payment_type", paymentLabel)
                                }
                            analyticsManager.logEvent("chart_item_clicked", params)
                            selectedChartDetails =
                                ChartDetailsData(
                                    categoryName = paymentLabel,
                                    amount = amount,
                                    percentage = 0,
                                    colorHex = color,
                                    transactionCount = 0,
                                    trend = TrendDirection.NEUTRAL,
                                )
                        },
                    )
                    if (showSpacer) VerticalSpacer(SpacingSize.MD)
                }
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.MONTHLY_BAR_CHART -> {
                if (chartData.monthlyData.isNotEmpty()) {
                    MonthlyBarChartCard(
                        data = chartData.monthlyData,
                        chartHeight = monthlyBarChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                    )
                    if (showSpacer) VerticalSpacer(SpacingSize.MD)
                }
            }
            com.antcashmanager.android.ui.screen.charts.model.ChartCardType.YEARLY_BAR_CHART -> {
                if (chartData.yearlyData.isNotEmpty()) {
                    YearlyBarChartCard(
                        data = chartData.yearlyData,
                        chartHeight = yearlyBarChartHeight,
                        zoomEnabled = zoomEnabled,
                        chartCardContainerColor = chartCardContainerColor,
                        fmt = fmt,
                        shareLabel = shareLabel,
                        context = context,
                        onShared = { analyticsManager.logEvent("chart_shared") },
                    )
                    if (showSpacer) VerticalSpacer(SpacingSize.MD)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.testTag("charts_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        top = 12.dp,
                        end = padding.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                        bottom = padding.calculateBottomPadding(),
                    ).verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp),
            // Extra space for visibility
        ) {
            PeriodFilterCard(
                chartCardContainerColor = chartCardContainerColor,
                selectedPreset = selectedPreset,
                dateRange = dateRange,
                dateFormat = dateFormat,
                onPresetSelected = { index, preset ->
                    selectedPreset = index
                    analyticsManager.logEvent("chart_date_filter_changed")
                    // Track filter combination
                    analyticsManager.logEvent(
                        "filter_combination_applied",
                        android.os.Bundle().apply {
                            putInt("filter_count", 1)
                            putString("types", "date")
                        },
                    )
                    onPresetSelected(preset)
                },
                onShowFromPicker = { showFromPicker = true },
                onShowToPicker = { showToPicker = true },
            )
            VerticalSpacer(SpacingSize.MD)

            ChartsSummaryRow(chartData = chartData, fmt = fmt)
            VerticalSpacer(SpacingSize.MD)

            // Render all charts cards in custom order
            // FASE 2: 2-column layout for expanded screens without fold
            if (isGridLayout) {
                chartsCardOrder.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        pair.forEach { cardType ->
                            Box(modifier = Modifier.weight(1f)) {
                                RenderChartCard(cardType, showSpacer = false)
                            }
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    VerticalSpacer(SpacingSize.MD)
                }
            } else {
                chartsCardOrder.forEach { cardType ->
                    RenderChartCard(cardType)
                }
            }

            // Empty state - shown when no data is available
            if (chartData.expenseByCategory.isEmpty() &&
                chartData.monthlyData.isEmpty() &&
                chartData.incomeByCategory.isEmpty()
            ) {
                VerticalSpacer(SpacingSize.XXL)
                AntEmptyState(
                    mascotRes = R.drawable.ic_ant_mascot,
                    title = stringResource(R.string.charts_no_data),
                    subtitle = stringResource(R.string.charts_empty_ant),
                )
            }
        }
    } // Closing Scaffold

    // Chart details bottom sheet for tap-to-details interaction
    ChartsDetailsBottomSheet(
        chartDetails = selectedChartDetails,
        onDismiss = { selectedChartDetails = null },
    )

    // Date pickers
    if (showFromPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateRange.from)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        analyticsManager.logEvent("chart_custom_date_range_set")
                        // Track filter combination
                        analyticsManager.logEvent(
                            "filter_combination_applied",
                            android.os.Bundle().apply {
                                putInt("filter_count", 1)
                                putString("types", "date")
                            },
                        )
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
                        // Track filter combination
                        analyticsManager.logEvent(
                            "filter_combination_applied",
                            android.os.Bundle().apply {
                                putInt("filter_count", 1)
                                putString("types", "date")
                            },
                        )
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
        colors =
            CardDefaults.cardColors(
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
            VerticalSpacer(SpacingSize.XS)
            Row(
                modifier =
                    Modifier
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
            VerticalSpacer(SpacingSize.XS)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppText(
                    text =
                        stringResource(
                            R.string.charts_from,
                            dateFormat.format(Date(dateRange.from)),
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onShowFromPicker,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.charts_pick_start_date),
                        modifier = Modifier.size(20.dp),
                    )
                }
                AppText(
                    text =
                        stringResource(
                            R.string.charts_to,
                            dateFormat.format(Date(dateRange.to)),
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onShowToPicker,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.charts_pick_end_date),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewVisualizationsSection(
    chartData: ChartData,
    adaptiveLayoutInfo: com.antcashmanager.android.ui.components.layout.AdaptiveLayoutInfo,
) {
    if (adaptiveLayoutInfo.isCompact) {
        // Phone: single column
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SpendingForecastCard(chartData = chartData)
            QuickStatsCard(chartData = chartData)
            DailyExpenseLineChartCard(chartData = chartData)
            WeekdayExpenseCard(chartData = chartData)
        }
    } else if (adaptiveLayoutInfo.isExpanded) {
        // Tablet 10"+: 3-column layout for maximum screen utilization
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SpendingForecastCard(chartData = chartData)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    QuickStatsCard(chartData = chartData)
                }
            }
            // Full width charts
            DailyExpenseLineChartCard(chartData = chartData)
            WeekdayExpenseCard(chartData = chartData)
        }
    } else {
        // Tablet 7" (medium): 2-column layout
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickStatsCard(chartData = chartData)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpendingForecastCard(chartData = chartData)
                    DailyExpenseLineChartCard(chartData = chartData)
                }
            }
            WeekdayExpenseCard(chartData = chartData)
        }
    }
}

@Composable
private fun ChartsSummaryRow(
    chartData: ChartData,
    fmt: CurrencyFormat,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val balance = chartData.totalIncome - chartData.totalExpense

        SummaryCard(
            modifier = Modifier.weight(1f),
            state =
                SummaryCardState(
                    label = stringResource(R.string.charts_income),
                    amount = chartData.totalIncome,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    fmt = fmt,
                    includesIncome = true,
                ),
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            state =
                SummaryCardState(
                    label = stringResource(R.string.charts_expenses),
                    amount = chartData.totalExpense,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    fmt = fmt,
                    includesIncome = false,
                ),
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            state =
                SummaryCardState(
                    label = stringResource(R.string.share_balance),
                    amount = balance,
                    containerColor =
                        if (balance >= 0) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                    contentColor =
                        if (balance >= 0) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                    fmt = fmt,
                    isBalance = true,
                    includesIncome = true,
                ),
        )
    }
}

/**
 * Card generica per un grafico a torta (entrate/uscite per categoria o ripartizione per
 * metodo di pagamento). [translateKeys] applica [translateCategory] alle chiavi di [data]
 * (necessario solo per le categorie, non per le etichette già localizzate del metodo di
 * pagamento).
 *
 * Supporta tap-to-details interaction tramite onCategorySelected callback.
 */
@Composable
private fun CategoryPieChartCard(
    title: String,
    data: Map<String, Double>,
    translateKeys: Boolean,
    @StringRes shareSubjectRes: Int,
    chartHeight: Dp,
    zoomEnabled: Boolean,
    chartCardContainerColor: Color,
    fmt: CurrencyFormat,
    shareLabel: String,
    context: Context,
    onShared: () -> Unit,
    onCategorySelected: (String, Double, Long) -> Unit = { _, _, _ -> },
    maskMode: AmountMaskMode = AmountMaskMode.NONE,
) {
    val shareSubject = stringResource(shareSubjectRes)
    // L'aggregato totale del breakdown resta mascherato in blocco se può contenere entrate
    // (PROTECT_SALARY/ALL), anche se le singole voci non-stipendio sotto sono mostrate in chiaro.
    val masked = LocalAmountsMasked.current && maskMode != AmountMaskMode.NONE
    val displayData =
        if (translateKeys) {
            data.entries.associate { (key, value) -> translateCategory(key) to value }
        } else {
            data
        }
    val protectedCategoryLabel =
        if (translateKeys) translateCategory(PROTECTED_INCOME_CATEGORY) else PROTECTED_INCOME_CATEGORY
    val chartSummaryDescription =
        stringResource(
            R.string.charts_chart_summary_cd,
            title,
            displayData.size,
            formatAmount(
                displayData.values.sumOf { abs(it) },
                fmt,
            ).let { if (masked) maskDigits(it) else it },
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
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
                        val shareText =
                            ShareTextFormatter.buildCategoryShareText(
                                context = context,
                                data = displayData,
                                fmt = fmt,
                            )
                        val intent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                        onShared()
                        context.startActivity(Intent.createChooser(intent, shareLabel))
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = shareLabel,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            VerticalSpacer(SpacingSize.SM)

            // Interactive pie chart with tap-to-details
            InteractivePieChart(
                data = displayData,
                onCategorySelected = { category, amount, colorHex ->
                    // Map translated category back to original if needed
                    val originalCategory =
                        if (translateKeys) {
                            data.entries
                                .find {
                                    translateCategoryPlain(context, it.key) == category
                                }?.key ?: category
                        } else {
                            category
                        }
                    onCategorySelected(originalCategory, amount, colorHex)
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .semantics { contentDescription = chartSummaryDescription },
            )

            VerticalSpacer(SpacingSize.SM)
            PieLegend(
                data = displayData,
                maskMode = maskMode,
                protectedCategoryLabel = protectedCategoryLabel,
            )
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
        colors =
            CardDefaults.cardColors(
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
            VerticalSpacer(SpacingSize.SM)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                entries.forEachIndexed { index, (category, amount) ->
                    val categoryLabel = translateCategory(category)
                    val percent = if (total > 0) (abs(amount) / total) * 100 else 0.0
                    val percentText = "%.0f".format(percent)
                    val masked =
                        maskEnabled &&
                            when (maskMode) {
                                AmountMaskMode.NONE -> false
                                AmountMaskMode.PROTECT_SALARY -> category == PROTECTED_INCOME_CATEGORY
                                AmountMaskMode.ALL -> true
                            }
                    val itemDescription =
                        stringResource(
                            R.string.charts_category_item_cd,
                            categoryLabel,
                            formatAmount(abs(amount), fmt).let { if (masked) maskDigits(it) else it },
                            percentText,
                        )
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    contentDescription = itemDescription
                                },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape,
                                    ),
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
                            text =
                                formatAmount(
                                    abs(amount),
                                    fmt,
                                ).let { if (masked) maskDigits(it) else it },
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
    fmt: CurrencyFormat,
    shareLabel: String,
    context: Context,
    onShared: () -> Unit,
) {
    val monthlyShareSubject = stringResource(R.string.share_monthly_subject)
    val title = stringResource(R.string.charts_monthly_overview)
    val masked = LocalAmountsMasked.current
    val chartSummaryDescription =
        stringResource(
            R.string.charts_chart_summary_cd,
            title,
            data.size,
            formatAmount(
                data.sumOf { it.income - it.expense },
                fmt,
            ).let { if (masked) maskDigits(it) else it },
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
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
                        val shareText =
                            ShareTextFormatter.buildMonthlyShareText(
                                context = context,
                                data = data,
                                fmt = fmt,
                            )
                        val intent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, monthlyShareSubject)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                        onShared()
                        context.startActivity(Intent.createChooser(intent, shareLabel))
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = shareLabel,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            VerticalSpacer(SpacingSize.SM)

            // Legend at top
            BarChartLegend()

            VerticalSpacer(SpacingSize.XS)

            // Scrollable bar chart for many months
            val chartWidth = (data.size * 80).coerceAtLeast(300)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
            ) {
                ZoomableBarChart(
                    data = data,
                    zoomEnabled = zoomEnabled,
                    modifier =
                        Modifier
                            .width(chartWidth.dp)
                            .height(chartHeight)
                            .semantics { contentDescription = chartSummaryDescription },
                )
            }

            VerticalSpacer(SpacingSize.XS)
        }
    }
}

@Composable
private fun YearlyBarChartCard(
    data: List<YearlyAmount>,
    chartHeight: Dp,
    zoomEnabled: Boolean,
    chartCardContainerColor: Color,
    fmt: CurrencyFormat,
    shareLabel: String,
    context: Context,
    onShared: () -> Unit,
) {
    val yearlyShareSubject = stringResource(R.string.share_yearly_subject)
    val title = stringResource(R.string.charts_yearly_overview)
    val masked = LocalAmountsMasked.current
    val chartSummaryDescription =
        stringResource(
            R.string.charts_chart_summary_cd,
            title,
            data.size,
            formatAmount(
                data.sumOf { it.income - it.expense },
                fmt,
            ).let { if (masked) maskDigits(it) else it },
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
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
                        val shareText =
                            ShareTextFormatter.buildYearlyShareText(
                                context = context,
                                data = data,
                                fmt = fmt,
                            )
                        val intent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, yearlyShareSubject)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                        onShared()
                        context.startActivity(Intent.createChooser(intent, shareLabel))
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = shareLabel,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            VerticalSpacer(SpacingSize.SM)

            // Legend at top
            BarChartLegend()

            VerticalSpacer(SpacingSize.XS)

            // Scrollable bar chart for years
            val yearlyChartWidth = (data.size * 100).coerceAtLeast(300)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
            ) {
                ZoomableYearlyBarChart(
                    data = data,
                    zoomEnabled = zoomEnabled,
                    modifier =
                        Modifier
                            .width(yearlyChartWidth.dp)
                            .height(chartHeight)
                            .semantics { contentDescription = chartSummaryDescription },
                )
            }

            VerticalSpacer(SpacingSize.XS)
        }
    }
}

@Composable
private fun SummaryCard(
    state: SummaryCardState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = state.containerColor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AppText(
                text = state.label,
                style = MaterialTheme.typography.labelSmall,
                color = state.contentColor.copy(alpha = ThemeConstants.HIGH_EMPHASIS_TEXT_ALPHA),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            VerticalSpacer(SpacingSize.XXXS)
            AppText(
                text =
                    formatAmount(abs(state.amount), state.fmt)
                        .let { if (LocalAmountsMasked.current && state.includesIncome) maskDigits(it) else it },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = state.contentColor,
                maxLines = 1,
            )
            if (state.isBalance) {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .background(
                                if (state.amount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                CircleShape,
                            ),
                )
            } else {
                // Spacer to maintain same height across all cards
                VerticalSpacer(SpacingSize.SM)
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
    val includesIncome: Boolean = true,
)
