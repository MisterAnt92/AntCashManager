package com.antcashmanager.android.ui.screen.settingsDisplay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppSwitch
import com.antcashmanager.android.ui.components.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.card.AppCard
import com.antcashmanager.android.ui.components.card.AppCardSectionHeader
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.settings.view.CurrencySymbolDialog
import com.antcashmanager.android.ui.screen.settings.view.DateFormatDialog
import com.antcashmanager.android.ui.screen.settings.view.DecimalDigitsDialog
import com.antcashmanager.android.ui.screen.settings.view.SeparatorDialog
import com.antcashmanager.android.ui.screen.settings.view.TransactionDisplayDialog
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.TransactionDisplayType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

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
fun DisplayScreen(
    navController: NavController,
) {
    val viewModel: DisplayViewModel = koinViewModel()

    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val decimalDigits by viewModel.decimalDigits.collectAsState()
    val decimalSeparator by viewModel.decimalSeparator.collectAsState()
    val thousandsSeparator by viewModel.thousandsSeparator.collectAsState()
    val showChartsSection by viewModel.showChartsSection.collectAsState()
    val dateFormat by viewModel.dateFormat.collectAsState()
    val showTransactionNotes by viewModel.showTransactionNotes.collectAsState()
    val showPaymentTypeBreakdown by viewModel.showPaymentTypeBreakdown.collectAsState()
    val showQuickInsightsCard by viewModel.showQuickInsightsCard.collectAsState()
    val transactionDisplayType by viewModel.transactionDisplayType.collectAsState()
    val transactionsTransactionDisplayType by viewModel.transactionsTransactionDisplayType.collectAsState()
    val chartsZoomEnabled by viewModel.chartsZoomEnabled.collectAsState()

    DisplayContent(
        currencySymbol = currencySymbol,
        onCurrencySymbolSelected = { viewModel.setCurrencySymbol(it) },
        decimalDigits = decimalDigits,
        onDecimalDigitsSelected = { viewModel.setDecimalDigits(it) },
        decimalSeparator = decimalSeparator,
        onDecimalSeparatorSelected = { viewModel.setDecimalSeparator(it) },
        thousandsSeparator = thousandsSeparator,
        onThousandsSeparatorSelected = { viewModel.setThousandsSeparator(it) },
        showTransactionNotes = showTransactionNotes,
        onShowTransactionNotesChanged = { viewModel.setShowTransactionNotes(it) },
        showChartsSection = showChartsSection,
        onShowChartsSectionChanged = { viewModel.setShowChartsSection(it) },
        chartsZoomEnabled = chartsZoomEnabled,
        onChartsZoomEnabledChanged = { viewModel.setChartsZoomEnabled(it) },
        dateFormat = dateFormat,
        onDateFormatSelected = { viewModel.setDateFormat(it) },
        showPaymentTypeBreakdown = showPaymentTypeBreakdown,
        onShowPaymentTypeBreakdownChanged = { viewModel.setShowPaymentTypeBreakdown(it) },
        showQuickInsightsCard = showQuickInsightsCard,
        onShowQuickInsightsCardChanged = { viewModel.setShowQuickInsightsCard(it) },
        transactionDisplayType = transactionDisplayType,
        onTransactionDisplayTypeSelected = { viewModel.setTransactionDisplayType(it) },
        transactionsTransactionDisplayType = transactionsTransactionDisplayType,
        onTransactionsTransactionDisplayTypeSelected = {
            viewModel.setTransactionsTransactionDisplayType(
                it
            )
        },
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
    showTransactionNotes: Boolean,
    onShowTransactionNotesChanged: (Boolean) -> Unit,
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
    transactionDisplayType: TransactionDisplayType,
    onTransactionDisplayTypeSelected: (TransactionDisplayType) -> Unit,
    transactionsTransactionDisplayType: TransactionDisplayType,
    onTransactionsTransactionDisplayTypeSelected: (TransactionDisplayType) -> Unit,
    onResetAllPreferences: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDecimalDigitsDialog by remember { mutableStateOf(false) }
    var showDecimalSeparatorDialog by remember { mutableStateOf(false) }
    var showThousandsSeparatorDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showResetPreferencesDialog by remember { mutableStateOf(false) }
    var showTransactionDisplayDialog by remember { mutableStateOf(false) }
    var showTransactionsDisplayDialog by remember { mutableStateOf(false) }
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { AppText(stringResource(R.string.settings_display)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (adaptiveLayoutInfo.isCompact) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) +
                        DisplayConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                    top = innerPadding.calculateTopPadding() + DisplayConstant.CONTENT_TOP_PADDING_DP.dp,
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr) +
                        DisplayConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                    bottom = innerPadding.calculateBottomPadding() + DisplayConstant.CONTENT_BOTTOM_PADDING_DP.dp,
                ),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
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
                    DateSection(
                        dateFormat = dateFormat,
                        onShowDateFormatDialog = { showDateFormatDialog = true },
                    )
                }

                item {
                    ChartsDisplaySection(
                        showChartsSection = showChartsSection,
                        onShowChartsSectionChanged = onShowChartsSectionChanged,
                        chartsZoomEnabled = chartsZoomEnabled,
                        onChartsZoomEnabledChanged = onChartsZoomEnabledChanged,
                    )
                }

                item {
                    HomeDisplaySection(
                        showPaymentTypeBreakdown = showPaymentTypeBreakdown,
                        onShowPaymentTypeBreakdownChanged = onShowPaymentTypeBreakdownChanged,
                        showQuickInsightsCard = showQuickInsightsCard,
                        onShowQuickInsightsCardChanged = onShowQuickInsightsCardChanged,
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
                        onShowTransactionNotesChanged = onShowTransactionNotesChanged,
                        onShowResetPreferencesDialog = { showResetPreferencesDialog = true },
                    )
                }

                item { androidx.compose.material3.Surface(modifier = Modifier.height(24.dp)) { } }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) +
                            DisplayConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                        top = innerPadding.calculateTopPadding() + DisplayConstant.CONTENT_TOP_PADDING_DP.dp,
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr) +
                            DisplayConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                        bottom = innerPadding.calculateBottomPadding() + DisplayConstant.CONTENT_BOTTOM_PADDING_DP.dp,
                    )
                    .verticalScroll(rememberScrollState()),
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
                        onShowChartsSectionChanged = onShowChartsSectionChanged,
                        chartsZoomEnabled = chartsZoomEnabled,
                        onChartsZoomEnabledChanged = onChartsZoomEnabledChanged,
                    )
                    HomeDisplaySection(
                        showPaymentTypeBreakdown = showPaymentTypeBreakdown,
                        onShowPaymentTypeBreakdownChanged = onShowPaymentTypeBreakdownChanged,
                        showQuickInsightsCard = showQuickInsightsCard,
                        onShowQuickInsightsCardChanged = onShowQuickInsightsCardChanged,
                        transactionDisplayType = transactionDisplayType,
                        onShowTransactionDisplayDialog = { showTransactionDisplayDialog = true },
                    )
                    OtherSection(
                        showTransactionNotes = showTransactionNotes,
                        onShowTransactionNotesChanged = onShowTransactionNotesChanged,
                        onShowResetPreferencesDialog = { showResetPreferencesDialog = true },
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
        onDecimalDigitsSelected(digits)
        showDecimalDigitsDialog = false
    }
    val dismissDecimalDigits: () -> Unit = { showDecimalDigitsDialog = false }

    val handleDecimalSeparatorSelected: (String) -> Unit = { value ->
        onDecimalSeparatorSelected(value)
        showDecimalSeparatorDialog = false
    }
    val dismissDecimalSeparator: () -> Unit = { showDecimalSeparatorDialog = false }

    val handleThousandsSeparatorSelected: (String) -> Unit = { value ->
        onThousandsSeparatorSelected(value)
        showThousandsSeparatorDialog = false
    }
    val dismissThousandsSeparator: () -> Unit = { showThousandsSeparatorDialog = false }

    val handleDateFormatSelected: (String) -> Unit = { fmt ->
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
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPreferencesDialog = false }) {
                    AppText(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    val handleTransactionDisplaySelected: (TransactionDisplayType) -> Unit = { t ->
        onTransactionDisplayTypeSelected(t)
        showTransactionDisplayDialog = false
    }
    val dismissTransactionDisplay: () -> Unit = { showTransactionDisplayDialog = false }

    val handleTransactionsDisplaySelected: (TransactionDisplayType) -> Unit = { t ->
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
            options = CurrencyFormat.DECIMAL_SEPARATORS.filter { it.first != thousandsSeparator },
            currentValue = decimalSeparator,
            onSelected = handleDecimalSeparatorSelected,
            onDismiss = dismissDecimalSeparator,
        )
    }

    if (showThousandsSeparatorDialog) {
        SeparatorDialog(
            title = stringResource(R.string.dialog_choose_thousands_separator),
            options = CurrencyFormat.THOUSANDS_SEPARATORS.filter { it.first != decimalSeparator },
            currentValue = thousandsSeparator,
            onSelected = handleThousandsSeparatorSelected,
            onDismiss = dismissThousandsSeparator,
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
}

@Composable
private fun separatorLabel(value: String, isThou: Boolean): String {
    val options =
        if (isThou) CurrencyFormat.THOUSANDS_SEPARATORS else CurrencyFormat.DECIMAL_SEPARATORS
    return options.find { it.first == value }?.second ?: when (value) {
        "," -> stringResource(R.string.settings_separator_comma)
        "." -> stringResource(R.string.settings_separator_period)
        " " -> stringResource(R.string.settings_separator_space)
        "" -> stringResource(R.string.settings_separator_none)
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
    Spacer(modifier = Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_currency_symbol),
            subtitle = CurrencyFormat.SUPPORTED_CURRENCIES.find { it.first == currencySymbol }?.second
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

    AppText(
        text = stringResource(
            R.string.settings_format_preview,
            formatAmount(
                1234567.89,
                CurrencyFormat(
                    currencySymbol = currencySymbol,
                    decimalDigits = decimalDigits,
                    decimalSeparator = decimalSeparator,
                    thousandsSeparator = thousandsSeparator,
                ),
            ),
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 8.dp),
    )
}

@Composable
private fun DateSection(
    dateFormat: String,
    onShowDateFormatDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_section_dates))
    Spacer(modifier = Modifier.height(8.dp))

    val currentDateExample = remember(dateFormat) {
        kotlin.runCatching {
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
    AppCardSectionHeader(title = stringResource(R.string.settings_display_charts))
    Spacer(modifier = Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_show_charts_section),
            subtitle = if (showChartsSection) stringResource(R.string.settings_show_charts_section_visible) else stringResource(
                R.string.settings_show_charts_section_hidden
            ),
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
    AppCardSectionHeader(title = stringResource(R.string.settings_display_home))
    Spacer(modifier = Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_show_payment_breakdown),
            subtitle = stringResource(R.string.settings_show_payment_breakdown_desc),
            leadingIcon = Icons.Default.Payment,
            trailingContent = {
                AppSwitch(
                    checked = showPaymentTypeBreakdown,
                    onCheckedChange = onShowPaymentTypeBreakdownChanged
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
            subtitle = when (transactionDisplayType) {
                TransactionDisplayType.TREND -> stringResource(R.string.settings_transaction_display_trend)
                TransactionDisplayType.CATEGORY -> stringResource(R.string.settings_transaction_display_category)
                TransactionDisplayType.NONE -> stringResource(R.string.settings_transaction_display_none)
            },
            leadingIcon = Icons.Default.Visibility,
            onClick = onShowTransactionDisplayDialog,
        )
    }
}

@Composable
private fun TransactionsDisplaySection(
    transactionDisplayType: TransactionDisplayType,
    onShowTransactionDisplayDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_transaction_display_section))
    Spacer(modifier = Modifier.height(8.dp))

    AppCard(
        title = stringResource(R.string.settings_transaction_display),
        subtitle = when (transactionDisplayType) {
            TransactionDisplayType.TREND -> stringResource(R.string.settings_transaction_display_trend)
            TransactionDisplayType.CATEGORY -> stringResource(R.string.settings_transaction_display_category)
            TransactionDisplayType.NONE -> stringResource(R.string.settings_transaction_display_none)
        },
        leadingIcon = Icons.Default.Visibility,
        onClick = onShowTransactionDisplayDialog,
    )
}

@Composable
private fun OtherSection(
    showTransactionNotes: Boolean,
    onShowTransactionNotesChanged: (Boolean) -> Unit,
    onShowResetPreferencesDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_section_other))
    Spacer(modifier = Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            title = stringResource(R.string.settings_show_transaction_notes),
            subtitle = if (showTransactionNotes) stringResource(R.string.settings_show_transaction_notes_subtitle_enabled) else stringResource(
                R.string.settings_show_transaction_notes_subtitle_disabled
            ),
            leadingIcon = Icons.Default.TextFields,
            trailingContent = {
                Switch(
                    checked = showTransactionNotes,
                    onCheckedChange = onShowTransactionNotesChanged
                )
            },
            onClick = { onShowTransactionNotesChanged(!showTransactionNotes) },
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
            showTransactionNotes = true,
            onShowTransactionNotesChanged = {},
            showChartsSection = true,
            onShowChartsSectionChanged = {},
            chartsZoomEnabled = true,
            onChartsZoomEnabledChanged = {},
            dateFormat = "dd/MM/yyyy",
            onDateFormatSelected = {},
            showPaymentTypeBreakdown = true,
            onShowPaymentTypeBreakdownChanged = {},
            showQuickInsightsCard = false,
            onShowQuickInsightsCardChanged = {},
            transactionDisplayType = TransactionDisplayType.TREND,
            onTransactionDisplayTypeSelected = {},
            transactionsTransactionDisplayType = TransactionDisplayType.TREND,
            onTransactionsTransactionDisplayTypeSelected = {},
            onResetAllPreferences = {},
            onNavigateBack = {},
        )
    }
}
