package com.antcashmanager.android.ui.screen.settings.displaySettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.card.AppCard
import com.antcashmanager.android.ui.components.card.AppCardSectionHeader
import com.antcashmanager.android.ui.components.common.AppSlider
import com.antcashmanager.android.ui.components.common.AppSwitch
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.settings.displaySettings.DisplayEvent
import com.antcashmanager.android.ui.screen.settings.view.CurrencySymbolDialog
import com.antcashmanager.android.ui.screen.settings.view.DateFormatDialog
import com.antcashmanager.android.ui.screen.settings.view.DecimalDigitsDialog
import com.antcashmanager.android.ui.screen.settings.view.MealVoucherDialog
import com.antcashmanager.android.ui.screen.settings.view.SeparatorDialog
import com.antcashmanager.android.ui.screen.settings.view.TransactionDisplayDialog
import com.antcashmanager.android.ui.screen.settings.view.WidgetBackgroundColorDialog
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.TransactionDisplayType
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen wrapper per la schermata "Visualizzazione".
 * Si occupa di creare il ViewModel, raccogliere gli StateFlow e passare i
 * valori al composable di presentazione `DisplayContent`.
 *
 * @param settingsRepository repository per le impostazioni (iniettato manualmente)
 * @param navController NavController per la navigazione
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(navController: NavController) {
    val viewModel: DisplayViewModel = koinViewModel()

    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val decimalDigits by viewModel.decimalDigits.collectAsStateWithLifecycle()
    val decimalSeparator by viewModel.decimalSeparator.collectAsStateWithLifecycle()
    val thousandsSeparator by viewModel.thousandsSeparator.collectAsStateWithLifecycle()
    val mealVoucherValue by viewModel.mealVoucherValue.collectAsStateWithLifecycle()
    val showChartsSection by viewModel.showChartsSection.collectAsStateWithLifecycle()
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle()
    val showTransactionNotes by viewModel.showTransactionNotes.collectAsStateWithLifecycle()
    val maskAmounts by viewModel.maskAmounts.collectAsStateWithLifecycle()
    val showPaymentTypeBreakdown by viewModel.showPaymentTypeBreakdown.collectAsStateWithLifecycle()
    val showQuickInsightsCard by viewModel.showQuickInsightsCard.collectAsStateWithLifecycle()
    val defaultPaymentType by viewModel.defaultPaymentType.collectAsStateWithLifecycle()
    val transactionDisplayType by viewModel.transactionDisplayType.collectAsStateWithLifecycle()
    val transactionsTransactionDisplayType by viewModel.transactionsTransactionDisplayType.collectAsStateWithLifecycle()
    val chartsZoomEnabled by viewModel.chartsZoomEnabled.collectAsStateWithLifecycle()
    val widgetBackgroundColor by viewModel.widgetBackgroundColor.collectAsStateWithLifecycle()
    val widgetOpacity by viewModel.widgetOpacity.collectAsStateWithLifecycle()

    DisplayContent(
        currencySymbol = currencySymbol,
        onCurrencySymbolSelected = { viewModel.onEvent(DisplayEvent.SetCurrencySymbol(it)) },
        decimalDigits = decimalDigits,
        onDecimalDigitsSelected = { viewModel.onEvent(DisplayEvent.SetDecimalDigits(it)) },
        decimalSeparator = decimalSeparator,
        onDecimalSeparatorSelected = { viewModel.onEvent(DisplayEvent.SetDecimalSeparator(it)) },
        thousandsSeparator = thousandsSeparator,
        onThousandsSeparatorSelected = { viewModel.onEvent(DisplayEvent.SetThousandsSeparator(it)) },
        mealVoucherValue = mealVoucherValue,
        onMealVoucherValueSelected = { viewModel.onEvent(DisplayEvent.SetMealVoucherValue(it)) },
        showTransactionNotes = showTransactionNotes,
        onShowTransactionNotesChanged = { viewModel.onEvent(DisplayEvent.SetShowTransactionNotes(it)) },
        maskAmounts = maskAmounts,
        onMaskAmountsChanged = { viewModel.onEvent(DisplayEvent.SetMaskAmounts(it)) },
        showChartsSection = showChartsSection,
        onShowChartsSectionChanged = { viewModel.onEvent(DisplayEvent.SetShowChartsSection(it)) },
        chartsZoomEnabled = chartsZoomEnabled,
        onChartsZoomEnabledChanged = { viewModel.onEvent(DisplayEvent.SetChartsZoomEnabled(it)) },
        dateFormat = dateFormat,
        onDateFormatSelected = { viewModel.onEvent(DisplayEvent.SetDateFormat(it)) },
        showPaymentTypeBreakdown = showPaymentTypeBreakdown,
        onShowPaymentTypeBreakdownChanged = { viewModel.onEvent(DisplayEvent.SetShowPaymentTypeBreakdown(it)) },
        showQuickInsightsCard = showQuickInsightsCard,
        onShowQuickInsightsCardChanged = { viewModel.onEvent(DisplayEvent.SetShowQuickInsightsCard(it)) },
        defaultPaymentType = defaultPaymentType,
        onDefaultPaymentTypeSelected = { viewModel.onEvent(DisplayEvent.SetDefaultPaymentType(it)) },
        transactionDisplayType = transactionDisplayType,
        onTransactionDisplayTypeSelected = { viewModel.onEvent(DisplayEvent.SetTransactionDisplayType(it)) },
        transactionsTransactionDisplayType = transactionsTransactionDisplayType,
        onTransactionsTransactionDisplayTypeSelected = {
            viewModel.onEvent(DisplayEvent.SetTransactionsTransactionDisplayType(it))
        },
        widgetBackgroundColor = widgetBackgroundColor,
        onWidgetBackgroundColorSelected = { viewModel.onEvent(DisplayEvent.SetWidgetBackgroundColor(it)) },
        widgetOpacity = widgetOpacity,
        onWidgetOpacityChanged = { viewModel.onEvent(DisplayEvent.SetWidgetOpacity(it)) },
        onResetAllPreferences = { viewModel.resetAllPreferences() },
        onNavigateBack = { navController.popBackStack() },
    )
}

/**
 * Composable che rappresenta il contenuto della schermata "Visualizzazione".
 * È stato scomposto in sotto-sezioni (`CurrencySection`, `DateSection`,
 * `ChartsSection`, `HomeDisplaySection`, `TransactionsDisplaySection`, `OtherSection`) per migliorare la
 * testabilità e ridurre la complessità (detekt warnings).
 *
 * I dialog vengono mostrati da variabili locali di stato e sono definiti in
 * `com.antcashmanager.android.ui.screen.settings.view.SettingsDialogs.kt`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DisplayContent(
    currencySymbol: String,
    onCurrencySymbolSelected: (String) -> Unit,
    decimalDigits: Int,
    onDecimalDigitsSelected: (Int) -> Unit,
    decimalSeparator: String,
    onDecimalSeparatorSelected: (String) -> Unit,
    thousandsSeparator: String,
    onThousandsSeparatorSelected: (String) -> Unit,
    mealVoucherValue: Double,
    onMealVoucherValueSelected: (Double) -> Unit,
    showTransactionNotes: Boolean,
    onShowTransactionNotesChanged: (Boolean) -> Unit,
    maskAmounts: Boolean,
    onMaskAmountsChanged: (Boolean) -> Unit,
    showChartsSection: Boolean,
    onShowChartsSectionChanged: (Boolean) -> Unit,
    chartsZoomEnabled: Boolean,
    onChartsZoomEnabledChanged: (Boolean) -> Unit,
    dateFormat: String,
    onDateFormatSelected: (String) -> Unit,
    showPaymentTypeBreakdown: Boolean,
    onShowPaymentTypeBreakdownChanged: (Boolean) -> Unit,
    showQuickInsightsCard: Boolean,
    onShowQuickInsightsCardChanged: (Boolean) -> Unit,
    defaultPaymentType: String,
    onDefaultPaymentTypeSelected: (String) -> Unit,
    transactionDisplayType: TransactionDisplayType,
    onTransactionDisplayTypeSelected: (TransactionDisplayType) -> Unit,
    transactionsTransactionDisplayType: TransactionDisplayType,
    onTransactionsTransactionDisplayTypeSelected: (TransactionDisplayType) -> Unit,
    widgetBackgroundColor: Long,
    onWidgetBackgroundColorSelected: (Long) -> Unit,
    widgetOpacity: Int,
    onWidgetOpacityChanged: (Int) -> Unit,
    onResetAllPreferences: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDecimalDigitsDialog by remember { mutableStateOf(false) }
    var showDecimalSeparatorDialog by remember { mutableStateOf(false) }
    var showThousandsSeparatorDialog by remember { mutableStateOf(false) }
    var showMealVoucherDialog by remember { mutableStateOf(false) }
    var showPaymentTypeDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showResetPreferencesDialog by remember { mutableStateOf(false) }
    var showTransactionDisplayDialog by remember { mutableStateOf(false) }
    var showTransactionsDisplayDialog by remember { mutableStateOf(false) }
    var showWidgetBackgroundColorDialog by remember { mutableStateOf(false) }
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()
    val analyticsManager: AnalyticsManager = koinInject()
    val handleShowQuickInsightsCardChanged: (Boolean) -> Unit = { show ->
        analyticsManager.logEvent("home_quick_insights_toggled")
        onShowQuickInsightsCardChanged(show)
    }
    val handleShowChartsSectionChanged: (Boolean) -> Unit = { show ->
        analyticsManager.logEvent("show_charts_section_toggled")
        onShowChartsSectionChanged(show)
    }
    val handleChartsZoomEnabledChanged: (Boolean) -> Unit = { enabled ->
        analyticsManager.logEvent("charts_zoom_toggled")
        onChartsZoomEnabledChanged(enabled)
    }
    val handleShowPaymentTypeBreakdownChanged: (Boolean) -> Unit = { show ->
        analyticsManager.logEvent("show_payment_breakdown_toggled")
        onShowPaymentTypeBreakdownChanged(show)
    }
    val handleShowTransactionNotesChanged: (Boolean) -> Unit = { show ->
        analyticsManager.logEvent("show_transaction_notes_toggled")
        onShowTransactionNotesChanged(show)
    }
    val handleMaskAmountsChanged: (Boolean) -> Unit = { mask ->
        analyticsManager.logEvent("mask_amounts_toggled")
        onMaskAmountsChanged(mask)
    }
    val handleWidgetBackgroundColorSelected: (Long) -> Unit = { color ->
        analyticsManager.logEvent("widget_background_color_changed")
        onWidgetBackgroundColorSelected(color)
        showWidgetBackgroundColorDialog = false
    }
    val handleWidgetOpacityChanged: (Int) -> Unit = { opacity ->
        analyticsManager.logEvent("widget_opacity_changed")
        onWidgetOpacityChanged(opacity)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { AppText(stringResource(R.string.settings_display)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (adaptiveLayoutInfo.isCompact) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start =
                            innerPadding.calculateStartPadding(LayoutDirection.Ltr) +
                                DisplayConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                        top = innerPadding.calculateTopPadding() + DisplayConstant.CONTENT_TOP_PADDING_DP.dp,
                        end =
                            innerPadding.calculateEndPadding(LayoutDirection.Ltr) +
                                DisplayConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                        bottom = innerPadding.calculateBottomPadding() + DisplayConstant.CONTENT_BOTTOM_PADDING_DP.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    CurrencySection(
                        currencySymbol = currencySymbol,
                        decimalDigits = decimalDigits,
                        decimalSeparator = decimalSeparator,
                        thousandsSeparator = thousandsSeparator,
                        onShowCurrencyDialog = { showCurrencyDialog = true },
                        onShowDecimalDigitsDialog = { showDecimalDigitsDialog = true },
                        onShowDecimalSeparatorDialog = { showDecimalSeparatorDialog = true },
                        onShowThousandsSeparatorDialog = { showThousandsSeparatorDialog = true },
                    )
                }

                item {
                    PaymentTypeSection(
                        currencySymbol = currencySymbol,
                        decimalDigits = decimalDigits,
                        decimalSeparator = decimalSeparator,
                        thousandsSeparator = thousandsSeparator,
                        mealVoucherValue = mealVoucherValue,
                        onShowMealVoucherDialog = { showMealVoucherDialog = true },
                        defaultPaymentType = defaultPaymentType,
                        onShowPaymentTypeDialog = { showPaymentTypeDialog = true },
                    )
                }

                item {
                    DateSection(
                        dateFormat = dateFormat,
                        onShowDateFormatDialog = { showDateFormatDialog = true },
                    )
                }

                item {
                    ChartsDisplaySection(
                        showChartsSection = showChartsSection,
                        onShowChartsSectionChanged = handleShowChartsSectionChanged,
                        chartsZoomEnabled = chartsZoomEnabled,
                        onChartsZoomEnabledChanged = handleChartsZoomEnabledChanged,
                    )
                }

                item {
                    HomeDisplaySection(
                        showPaymentTypeBreakdown = showPaymentTypeBreakdown,
                        onShowPaymentTypeBreakdownChanged = handleShowPaymentTypeBreakdownChanged,
                        showQuickInsightsCard = showQuickInsightsCard,
                        onShowQuickInsightsCardChanged = handleShowQuickInsightsCardChanged,
                        transactionDisplayType = transactionDisplayType,
                        onShowTransactionDisplayDialog = { showTransactionDisplayDialog = true },
                    )
                }

                item {
                    TransactionsDisplaySection(
                        transactionDisplayType = transactionsTransactionDisplayType,
                        onShowTransactionDisplayDialog = { showTransactionsDisplayDialog = true },
                    )
                }

                item {
                    OtherSection(
                        showTransactionNotes = showTransactionNotes,
                        onShowTransactionNotesChanged = handleShowTransactionNotesChanged,
                        maskAmounts = maskAmounts,
                        onMaskAmountsChanged = handleMaskAmountsChanged,
                        onShowResetPreferencesDialog = { showResetPreferencesDialog = true },
                    )
                }

                item {
                    WidgetsDisplaySection(
                        widgetBackgroundColor = widgetBackgroundColor,
                        onShowWidgetBackgroundColorDialog = {
                            showWidgetBackgroundColorDialog = true
                        },
                        widgetOpacity = widgetOpacity,
                        onWidgetOpacityChanged = handleWidgetOpacityChanged,
                    )
                }

                item { Surface(modifier = Modifier.height(24.dp)) { } }
            }
        } else {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start =
                                innerPadding.calculateStartPadding(LayoutDirection.Ltr) +
                                    DisplayConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                            top = innerPadding.calculateTopPadding() + DisplayConstant.CONTENT_TOP_PADDING_DP.dp,
                            end =
                                innerPadding.calculateEndPadding(LayoutDirection.Ltr) +
                                    DisplayConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                            bottom =
                                innerPadding.calculateBottomPadding() + DisplayConstant.CONTENT_BOTTOM_PADDING_DP.dp,
                        ).verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(DisplayConstant.TABLET_COLUMNS_SPACING_DP.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CurrencySection(
                        currencySymbol = currencySymbol,
                        decimalDigits = decimalDigits,
                        decimalSeparator = decimalSeparator,
                        thousandsSeparator = thousandsSeparator,
                        onShowCurrencyDialog = { showCurrencyDialog = true },
                        onShowDecimalDigitsDialog = { showDecimalDigitsDialog = true },
                        onShowDecimalSeparatorDialog = { showDecimalSeparatorDialog = true },
                        onShowThousandsSeparatorDialog = { showThousandsSeparatorDialog = true },
                    )
                    PaymentTypeSection(
                        currencySymbol = currencySymbol,
                        decimalDigits = decimalDigits,
                        decimalSeparator = decimalSeparator,
                        thousandsSeparator = thousandsSeparator,
                        mealVoucherValue = mealVoucherValue,
                        onShowMealVoucherDialog = { showMealVoucherDialog = true },
                        defaultPaymentType = defaultPaymentType,
                        onShowPaymentTypeDialog = { showPaymentTypeDialog = true },
                    )
                    DateSection(
                        dateFormat = dateFormat,
                        onShowDateFormatDialog = { showDateFormatDialog = true },
                    )
                    TransactionsDisplaySection(
                        transactionDisplayType = transactionsTransactionDisplayType,
                        onShowTransactionDisplayDialog = { showTransactionsDisplayDialog = true },
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChartsDisplaySection(
                        showChartsSection = showChartsSection,
                        onShowChartsSectionChanged = handleShowChartsSectionChanged,
                        chartsZoomEnabled = chartsZoomEnabled,
                        onChartsZoomEnabledChanged = handleChartsZoomEnabledChanged,
                    )
                    HomeDisplaySection(
                        showPaymentTypeBreakdown = showPaymentTypeBreakdown,
                        onShowPaymentTypeBreakdownChanged = handleShowPaymentTypeBreakdownChanged,
                        showQuickInsightsCard = showQuickInsightsCard,
                        onShowQuickInsightsCardChanged = handleShowQuickInsightsCardChanged,
                        transactionDisplayType = transactionDisplayType,
                        onShowTransactionDisplayDialog = { showTransactionDisplayDialog = true },
                    )
                    OtherSection(
                        showTransactionNotes = showTransactionNotes,
                        onShowTransactionNotesChanged = handleShowTransactionNotesChanged,
                        maskAmounts = maskAmounts,
                        onMaskAmountsChanged = handleMaskAmountsChanged,
                        onShowResetPreferencesDialog = { showResetPreferencesDialog = true },
                    )
                    WidgetsDisplaySection(
                        widgetBackgroundColor = widgetBackgroundColor,
                        onShowWidgetBackgroundColorDialog = {
                            showWidgetBackgroundColorDialog = true
                        },
                        widgetOpacity = widgetOpacity,
                        onWidgetOpacityChanged = handleWidgetOpacityChanged,
                    )
                }
            }
        }
    }

    // Local handlers for dialog results to avoid complex inline lambdas and satisfy detekt
    val handleCurrencySelected: (String) -> Unit = { symbol ->
        onCurrencySymbolSelected(symbol)
        showCurrencyDialog = false
    }
    val dismissCurrency: () -> Unit = { showCurrencyDialog = false }

    val handleDecimalDigitsSelected: (Int) -> Unit = { digits ->
        analyticsManager.logEvent("decimal_digits_changed")
        onDecimalDigitsSelected(digits)
        showDecimalDigitsDialog = false
    }
    val dismissDecimalDigits: () -> Unit = { showDecimalDigitsDialog = false }

    val handleDecimalSeparatorSelected: (String) -> Unit = { value ->
        analyticsManager.logEvent("decimal_separator_changed")
        onDecimalSeparatorSelected(value)
        showDecimalSeparatorDialog = false
    }
    val dismissDecimalSeparator: () -> Unit = { showDecimalSeparatorDialog = false }

    val handleThousandsSeparatorSelected: (String) -> Unit = { value ->
        analyticsManager.logEvent("thousands_separator_changed")
        onThousandsSeparatorSelected(value)
        showThousandsSeparatorDialog = false
    }
    val dismissThousandsSeparator: () -> Unit = { showThousandsSeparatorDialog = false }

    val handleMealVoucherValueSelected: (Double) -> Unit = { value ->
        analyticsManager.logEvent("meal_voucher_value_changed")
        onMealVoucherValueSelected(value)
        showMealVoucherDialog = false
    }
    val dismissMealVoucher: () -> Unit = { showMealVoucherDialog = false }

    val handlePaymentTypeSelected: (String) -> Unit = { paymentType ->
        analyticsManager.logEvent("default_payment_type_changed")
        onDefaultPaymentTypeSelected(paymentType)
        showPaymentTypeDialog = false
    }
    val dismissPaymentType: () -> Unit = { showPaymentTypeDialog = false }

    val handleDateFormatSelected: (String) -> Unit = { fmt ->
        analyticsManager.logEvent("date_format_changed")
        onDateFormatSelected(fmt)
        showDateFormatDialog = false
    }
    val dismissDateFormat: () -> Unit = { showDateFormatDialog = false }

    if (showResetPreferencesDialog) {
        AlertDialog(
            onDismissRequest = { showResetPreferencesDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { AppText(stringResource(R.string.dialog_reset_preferences_title)) },
            text = {
                AppText(
                    stringResource(R.string.dialog_reset_preferences_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onResetAllPreferences()
                    showResetPreferencesDialog = false
                }) {
                    AppText(
                        stringResource(R.string.dialog_reset),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPreferencesDialog = false }) {
                    AppText(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    val handleTransactionDisplaySelected: (TransactionDisplayType) -> Unit = { t ->
        analyticsManager.logEvent("transaction_display_type_changed")
        onTransactionDisplayTypeSelected(t)
        showTransactionDisplayDialog = false
    }
    val dismissTransactionDisplay: () -> Unit = { showTransactionDisplayDialog = false }

    val handleTransactionsDisplaySelected: (TransactionDisplayType) -> Unit = { t ->
        analyticsManager.logEvent("transaction_display_type_changed")
        onTransactionsTransactionDisplayTypeSelected(t)
        showTransactionsDisplayDialog = false
    }
    val dismissTransactionsDisplay: () -> Unit = { showTransactionsDisplayDialog = false }

    if (showCurrencyDialog) {
        CurrencySymbolDialog(
            currentSymbol = currencySymbol,
            onSymbolSelected = handleCurrencySelected,
            onDismiss = dismissCurrency,
        )
    }

    if (showDecimalDigitsDialog) {
        DecimalDigitsDialog(
            currentDigits = decimalDigits,
            onDigitsSelected = handleDecimalDigitsSelected,
            onDismiss = dismissDecimalDigits,
        )
    }

    if (showDecimalSeparatorDialog) {
        SeparatorDialog(
            title = stringResource(R.string.dialog_choose_decimal_separator),
            options =
                translateSeparatorOptions(
                    CurrencyFormat.DECIMAL_SEPARATORS.filter {
                        it.first !=
                            thousandsSeparator
                    },
                ),
            currentValue = decimalSeparator,
            onSelected = handleDecimalSeparatorSelected,
            onDismiss = dismissDecimalSeparator,
        )
    }

    if (showThousandsSeparatorDialog) {
        SeparatorDialog(
            title = stringResource(R.string.dialog_choose_thousands_separator),
            options =
                translateSeparatorOptions(
                    CurrencyFormat.THOUSANDS_SEPARATORS.filter {
                        it.first !=
                            decimalSeparator
                    },
                ),
            currentValue = thousandsSeparator,
            onSelected = handleThousandsSeparatorSelected,
            onDismiss = dismissThousandsSeparator,
        )
    }

    if (showMealVoucherDialog) {
        MealVoucherDialog(
            currentValue = mealVoucherValue,
            onDismiss = dismissMealVoucher,
            onConfirm = handleMealVoucherValueSelected,
        )
    }

    if (showPaymentTypeDialog) {
        val paymentTypeOptions =
            listOf(
                Triple("ELECTRONIC", stringResource(R.string.payment_type_electronic), Icons.Default.Payment),
                Triple("CASH", stringResource(R.string.payment_type_cash), Icons.Default.MonetizationOn),
                Triple("MEAL_VOUCHERS", stringResource(R.string.payment_type_meal_vouchers), Icons.Default.Restaurant),
            )

        AlertDialog(
            onDismissRequest = dismissPaymentType,
            title = { AppText(stringResource(R.string.dialog_payment_type_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentTypeOptions.forEach { (typeKey, typeLabel, typeIcon) ->
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 2.dp,
                                        color =
                                            if (defaultPaymentType == typeKey) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                    ),
                            color =
                                if (defaultPaymentType == typeKey) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            onClick = { handlePaymentTypeSelected(typeKey) },
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint =
                                        if (defaultPaymentType == typeKey) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )

                                AppText(
                                    text = typeLabel,
                                    modifier = Modifier.weight(1f),
                                    color =
                                        if (defaultPaymentType == typeKey) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    fontWeight =
                                        if (defaultPaymentType == typeKey) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                )

                                if (defaultPaymentType == typeKey) {
                                    AppText(
                                        text = "✓",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = dismissPaymentType) {
                    AppText(stringResource(R.string.common_close))
                }
            },
        )
    }

    if (showDateFormatDialog) {
        DateFormatDialog(
            currentFormat = dateFormat,
            onFormatSelected = handleDateFormatSelected,
            onDismiss = dismissDateFormat,
        )
    }

    if (showTransactionDisplayDialog) {
        TransactionDisplayDialog(
            currentDisplayType = transactionDisplayType,
            onDisplayTypeSelected = handleTransactionDisplaySelected,
            onDismiss = dismissTransactionDisplay,
        )
    }

    if (showTransactionsDisplayDialog) {
        TransactionDisplayDialog(
            currentDisplayType = transactionsTransactionDisplayType,
            onDisplayTypeSelected = handleTransactionsDisplaySelected,
            onDismiss = dismissTransactionsDisplay,
        )
    }

    if (showWidgetBackgroundColorDialog) {
        WidgetBackgroundColorDialog(
            currentColor = widgetBackgroundColor,
            onColorSelected = handleWidgetBackgroundColorSelected,
            onDismiss = { showWidgetBackgroundColorDialog = false },
        )
    }
}

@Composable
private fun separatorLabel(
    value: String,
    isThou: Boolean,
): String {
    val comma = stringResource(R.string.settings_separator_comma)
    val period = stringResource(R.string.settings_separator_period)
    val space = stringResource(R.string.settings_separator_space)
    val none = stringResource(R.string.settings_separator_none)

    return when (value) {
        "," -> comma
        "." -> period
        " " -> space
        "" -> none
        else -> value
    }
}

@Composable
private fun CurrencySection(
    currencySymbol: String,
    decimalDigits: Int,
    decimalSeparator: String,
    thousandsSeparator: String,
    onShowCurrencyDialog: () -> Unit,
    onShowDecimalDigitsDialog: () -> Unit,
    onShowDecimalSeparatorDialog: () -> Unit,
    onShowThousandsSeparatorDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_section_currency))
    VerticalSpacer(SpacingSize.XS)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_currency_symbol),
            subtitle =
                CurrencyFormat.SUPPORTED_CURRENCIES.find { it.first == currencySymbol }?.second
                    ?: currencySymbol,
            leadingIcon = Icons.Default.MonetizationOn,
            onClick = onShowCurrencyDialog,
        )

        AppCard(
            title = stringResource(R.string.settings_decimal_digits),
            subtitle = stringResource(R.string.settings_decimal_digits_subtitle, decimalDigits),
            leadingIcon = Icons.Default.Exposure,
            onClick = onShowDecimalDigitsDialog,
        )

        AppCard(
            title = stringResource(R.string.settings_decimal_separator),
            subtitle = separatorLabel(decimalSeparator, isThou = false),
            leadingIcon = Icons.Default.TextFields,
            onClick = onShowDecimalSeparatorDialog,
        )

        AppCard(
            title = stringResource(R.string.settings_thousands_separator),
            subtitle = separatorLabel(thousandsSeparator, isThou = true),
            leadingIcon = Icons.Default.MoreHoriz,
            onClick = onShowThousandsSeparatorDialog,
        )
    }

    VerticalSpacer(SpacingSize.XS)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        AppText(
            text = stringResource(R.string.settings_format_preview),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        AppText(
            text =
                formatAmount(
                    1234567.89,
                    CurrencyFormat(
                        currencySymbol = currencySymbol,
                        decimalDigits = decimalDigits,
                        decimalSeparator = decimalSeparator,
                        thousandsSeparator = thousandsSeparator,
                    ),
                ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PaymentTypeSection(
    currencySymbol: String,
    decimalDigits: Int,
    decimalSeparator: String,
    thousandsSeparator: String,
    mealVoucherValue: Double,
    onShowMealVoucherDialog: () -> Unit,
    defaultPaymentType: String,
    onShowPaymentTypeDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_payment_type_section))
    VerticalSpacer(SpacingSize.XS)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_meal_voucher_value),
            subtitle =
                stringResource(
                    R.string.settings_meal_voucher_value_subtitle,
                    formatAmount(
                        mealVoucherValue,
                        CurrencyFormat(
                            currencySymbol = currencySymbol,
                            decimalDigits = decimalDigits,
                            decimalSeparator = decimalSeparator,
                            thousandsSeparator = thousandsSeparator,
                        ),
                    ),
                ),
            leadingIcon = Icons.Default.Payment,
            onClick = onShowMealVoucherDialog,
        )

        val paymentTypeLabel =
            when (defaultPaymentType) {
                "CASH" -> stringResource(R.string.payment_type_cash)
                "MEAL_VOUCHERS" -> stringResource(R.string.payment_type_meal_vouchers)
                else -> stringResource(R.string.payment_type_electronic)
            }

        AppCard(
            title = stringResource(R.string.settings_default_payment_type),
            subtitle = stringResource(R.string.settings_default_payment_type_desc),
            leadingIcon = Icons.Default.Payment,
            onClick = onShowPaymentTypeDialog,
            trailingContent = {
                AppText(
                    text = paymentTypeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        )
    }
}

@Composable
private fun DateSection(
    dateFormat: String,
    onShowDateFormatDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_section_dates))
    VerticalSpacer(SpacingSize.XS)

    val currentDateExample =
        remember(dateFormat) {
            runCatching {
                val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
                formatter.format(Date())
            }.getOrElse { dateFormat }
        }

    AppCard(
        title = stringResource(R.string.settings_date_format),
        subtitle = stringResource(R.string.settings_date_format_subtitle, currentDateExample),
        leadingIcon = Icons.Default.CalendarMonth,
        onClick = onShowDateFormatDialog,
    )
}

@Composable
private fun ChartsDisplaySection(
    showChartsSection: Boolean,
    onShowChartsSectionChanged: (Boolean) -> Unit,
    chartsZoomEnabled: Boolean,
    onChartsZoomEnabledChanged: (Boolean) -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.common_charts))
    VerticalSpacer(SpacingSize.XS)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_show_charts_section),
            subtitle =
                if (showChartsSection) {
                    stringResource(R.string.settings_show_charts_section_visible)
                } else {
                    stringResource(
                        R.string.settings_show_charts_section_hidden,
                    )
                },
            leadingIcon = Icons.Default.BarChart,
            trailingContent = {
                Switch(checked = showChartsSection, onCheckedChange = onShowChartsSectionChanged)
            },
            onClick = { onShowChartsSectionChanged(!showChartsSection) },
        )

        AppCard(
            title = stringResource(R.string.settings_charts_zoom),
            subtitle = stringResource(R.string.settings_charts_zoom_desc),
            leadingIcon = Icons.Default.Visibility,
            trailingContent = {
                Switch(checked = chartsZoomEnabled, onCheckedChange = onChartsZoomEnabledChanged)
            },
            onClick = { onChartsZoomEnabledChanged(!chartsZoomEnabled) },
        )
    }
}

@Composable
private fun HomeDisplaySection(
    showPaymentTypeBreakdown: Boolean,
    onShowPaymentTypeBreakdownChanged: (Boolean) -> Unit,
    showQuickInsightsCard: Boolean,
    onShowQuickInsightsCardChanged: (Boolean) -> Unit,
    transactionDisplayType: TransactionDisplayType,
    onShowTransactionDisplayDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.common_home))
    VerticalSpacer(SpacingSize.XS)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_show_payment_breakdown),
            subtitle = stringResource(R.string.settings_show_payment_breakdown_desc),
            leadingIcon = Icons.Default.Payment,
            trailingContent = {
                AppSwitch(
                    checked = showPaymentTypeBreakdown,
                    onCheckedChange = onShowPaymentTypeBreakdownChanged,
                )
            },
            onClick = { onShowPaymentTypeBreakdownChanged(!showPaymentTypeBreakdown) },
        )

        AppCard(
            title = stringResource(R.string.settings_show_quick_insights_card),
            subtitle = stringResource(R.string.settings_show_quick_insights_card_desc),
            leadingIcon = Icons.Default.TipsAndUpdates,
            trailingContent = {
                AppSwitch(
                    checked = showQuickInsightsCard,
                    onCheckedChange = onShowQuickInsightsCardChanged,
                )
            },
            onClick = { onShowQuickInsightsCardChanged(!showQuickInsightsCard) },
        )

        AppCard(
            title = stringResource(R.string.settings_transaction_display),
            subtitle = stringResource(R.string.settings_transaction_display_desc),
            leadingIcon = Icons.Default.Visibility,
            onClick = onShowTransactionDisplayDialog,
        )

        VerticalSpacer(SpacingSize.SM)
        TransactionDisplayPreviewHome(transactionDisplayType)
    }
}

@Composable
private fun TransactionsDisplaySection(
    transactionDisplayType: TransactionDisplayType,
    onShowTransactionDisplayDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.common_transactions))
    VerticalSpacer(SpacingSize.XS)

    AppCard(
        title = stringResource(R.string.settings_transaction_display),
        subtitle = stringResource(R.string.settings_transaction_display_desc),
        leadingIcon = Icons.Default.Visibility,
        onClick = onShowTransactionDisplayDialog,
    )

    VerticalSpacer(SpacingSize.SM)
    TransactionDisplayPreview(transactionDisplayType)
}

@Composable
private fun TransactionDisplayPreviewHome(displayType: TransactionDisplayType) {
    TransactionDisplayPreview(displayType)
}

@Composable
private fun TransactionDisplayPreview(displayType: TransactionDisplayType) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppText(
            text = stringResource(R.string.common_preview),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        when (displayType) {
            TransactionDisplayType.TREND -> {
                TrendPreviewCard()
            }

            TransactionDisplayType.CATEGORY -> {
                CategoryPreviewCard()
            }

            TransactionDisplayType.NONE -> {
                NoIconPreviewCard()
            }
        }
    }
}

@Composable
private fun TrendPreviewCard() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp),
                ).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppText(
            text = stringResource(R.string.settings_transaction_display_trend_desc),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                AppText(
                    "↑",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            AppText(
                "Income +€50,00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center,
            ) {
                AppText(
                    "↓",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            AppText(
                "Expense -€15,00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CategoryPreviewCard() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp),
                ).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppText(
            text = stringResource(R.string.settings_transaction_display_category_desc),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            AppText(
                "Salary +€2500,00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp),
            )
            AppText(
                "Restaurant -€25,00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun NoIconPreviewCard() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp),
                ).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppText(
            text = stringResource(R.string.settings_transaction_display_none_desc),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppText(
                "Income +€2500,00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppText(
                "Expense -€25,00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun OtherSection(
    showTransactionNotes: Boolean,
    onShowTransactionNotesChanged: (Boolean) -> Unit,
    maskAmounts: Boolean,
    onMaskAmountsChanged: (Boolean) -> Unit,
    onShowResetPreferencesDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_section_other))
    VerticalSpacer(SpacingSize.XS)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_show_transaction_notes),
            subtitle =
                if (showTransactionNotes) {
                    stringResource(R.string.settings_show_transaction_notes_subtitle_enabled)
                } else {
                    stringResource(
                        R.string.settings_show_transaction_notes_subtitle_disabled,
                    )
                },
            leadingIcon = Icons.Default.TextFields,
            trailingContent = {
                Switch(
                    checked = showTransactionNotes,
                    onCheckedChange = onShowTransactionNotesChanged,
                )
            },
            onClick = { onShowTransactionNotesChanged(!showTransactionNotes) },
        )

        AppCard(
            title = stringResource(R.string.settings_mask_amounts),
            subtitle =
                if (maskAmounts) {
                    stringResource(R.string.settings_mask_amounts_subtitle_enabled)
                } else {
                    stringResource(R.string.settings_mask_amounts_subtitle_disabled)
                },
            leadingIcon = Icons.Default.VisibilityOff,
            trailingContent = {
                Switch(
                    checked = maskAmounts,
                    onCheckedChange = onMaskAmountsChanged,
                )
            },
            onClick = { onMaskAmountsChanged(!maskAmounts) },
        )

        AppCard(
            title = stringResource(R.string.settings_reset_preferences),
            subtitle = stringResource(R.string.settings_reset_preferences_subtitle),
            leadingIcon = Icons.Default.Refresh,
            iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
            iconTint = MaterialTheme.colorScheme.onErrorContainer,
            showChevron = false,
            onClick = onShowResetPreferencesDialog,
        )
    }
}

@Composable
private fun WidgetsDisplaySection(
    widgetBackgroundColor: Long,
    onShowWidgetBackgroundColorDialog: () -> Unit,
    widgetOpacity: Int,
    onWidgetOpacityChanged: (Int) -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_section_widgets))
    VerticalSpacer(SpacingSize.XS)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_widget_background_color),
            leadingIcon = Icons.Default.Palette,
            trailingContent = {
                Box(
                    modifier =
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(widgetBackgroundColor))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                )
            },
            onClick = onShowWidgetBackgroundColorDialog,
        )

        var sliderValue by remember(widgetOpacity) { mutableFloatStateOf(widgetOpacity.toFloat()) }

        AppCard(
            title = stringResource(R.string.settings_widget_opacity),
            subtitle = stringResource(R.string.settings_widget_opacity_value, sliderValue.toInt()),
            leadingIcon = Icons.Default.Opacity,
            showChevron = false,
            bottomContent = {
                AppSlider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onWidgetOpacityChanged(sliderValue.toInt()) },
                    valueRange = 0f..100f,
                    steps = 99,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )

        WidgetPreviewMock(backgroundColor = widgetBackgroundColor, opacity = sliderValue.toInt())
    }
}

@Composable
private fun WidgetPreviewMock(
    backgroundColor: Long,
    opacity: Int,
) {
    val resolvedColor = Color(backgroundColor).copy(alpha = opacity / 100f)
    val isLight = isColorPerceptuallyLight(backgroundColor)
    val primaryText = if (isLight) Color(0xFF212121) else Color.White
    val secondaryText = if (isLight) Color(0xFF757575) else Color(0xFFE0E0E0)

    AppText(
        text = stringResource(R.string.settings_widget_preview_title),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    VerticalSpacer(SpacingSize.XXXS)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF90A4AE), Color(0xFF64B5F6)),
                    ),
                ).padding(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(resolvedColor)
                        .padding(10.dp),
            ) {
                AppText(
                    text = stringResource(R.string.settings_widget_preview_recent),
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryText,
                )
                VerticalSpacer(SpacingSize.XXS)
                MockTransactionRows.forEach { (color, label, amount) ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(color)),
                            )
                            HorizontalSpacer(SpacingSize.XXS)
                            AppText(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryText,
                            )
                        }
                        AppText(
                            text = amount,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText,
                        )
                    }
                }
            }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(resolvedColor)
                        .padding(10.dp),
            ) {
                AppText(
                    text = stringResource(R.string.settings_widget_preview_category),
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryText,
                )
                VerticalSpacer(SpacingSize.XXS)
                AppText(
                    text = stringResource(R.string.charts_expenses),
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText,
                )
                MockCategoryBars.forEach { (color, fraction) ->
                    VerticalSpacer(SpacingSize.XXXS)
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(fraction)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(color)),
                    )
                }
            }
        }
    }
}

private val MockTransactionRows =
    listOf(
        Triple(0xFF81C784L, "Spesa", "-45,00 €"),
        Triple(0xFF64B5F6L, "Stipendio", "+1.200,00 €"),
        Triple(0xFFFFB74DL, "Benzina", "-30,00 €"),
    )

private val MockCategoryBars =
    listOf(
        0xFFE57373L to 1f,
        0xFFFFB74DL to 0.6f,
    )

private fun isColorPerceptuallyLight(color: Long): Boolean {
    val r = (color shr 16 and 0xFF) / 255f
    val g = (color shr 8 and 0xFF) / 255f
    val b = (color and 0xFF) / 255f
    val luminance = 0.299f * r + 0.587f * g + 0.114f * b
    return luminance >= 0.5f
}

@Composable
fun translateSeparatorOptions(options: List<Pair<String, String>>): List<Pair<String, String>> {
    val comma = stringResource(R.string.settings_separator_comma)
    val period = stringResource(R.string.settings_separator_period)
    val space = stringResource(R.string.settings_separator_space)
    val none = stringResource(R.string.settings_separator_none)

    return options.map { (value, label) ->
        value to
            when (label) {
                "Comma (,)" -> comma
                "Period (.)" -> period
                "Space" -> space
                "None" -> none
                else -> label
            }
    }
}

// Dialog implementations moved to SettingsDialogs.kt

@Preview(showBackground = true)
@Preview(showBackground = true, name = "DisplayScreen - 7 inch", widthDp = 600, heightDp = 960)
@Preview(showBackground = true, name = "DisplayScreen - 10 inch", widthDp = 840, heightDp = 1280)
@Composable
private fun DisplayContentPreview() {
    AntCashManagerTheme {
        DisplayContent(
            currencySymbol = "\u20ac",
            onCurrencySymbolSelected = {},
            decimalDigits = 2,
            onDecimalDigitsSelected = {},
            decimalSeparator = ",",
            onDecimalSeparatorSelected = {},
            thousandsSeparator = "",
            onThousandsSeparatorSelected = {},
            mealVoucherValue = 5.29,
            onMealVoucherValueSelected = {},
            showTransactionNotes = true,
            onShowTransactionNotesChanged = {},
            maskAmounts = false,
            onMaskAmountsChanged = {},
            showChartsSection = true,
            onShowChartsSectionChanged = {},
            chartsZoomEnabled = false,
            onChartsZoomEnabledChanged = {},
            dateFormat = "dd/MM/yyyy",
            onDateFormatSelected = {},
            showPaymentTypeBreakdown = true,
            onShowPaymentTypeBreakdownChanged = {},
            showQuickInsightsCard = false,
            onShowQuickInsightsCardChanged = {},
            defaultPaymentType = "ELECTRONIC",
            onDefaultPaymentTypeSelected = {},
            transactionDisplayType = TransactionDisplayType.TREND,
            onTransactionDisplayTypeSelected = {},
            transactionsTransactionDisplayType = TransactionDisplayType.TREND,
            onTransactionsTransactionDisplayTypeSelected = {},
            widgetBackgroundColor = 0xFFFFFFFFL,
            onWidgetBackgroundColorSelected = {},
            widgetOpacity = 100,
            onWidgetOpacityChanged = {},
            onResetAllPreferences = {},
            onNavigateBack = {},
        )
    }
}
