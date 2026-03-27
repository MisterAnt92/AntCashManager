package com.antcashmanager.android.ui.screen.home.settings.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppCard
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.repository.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(
    settingsRepository: SettingsRepository,
    navController: NavController,
) {
    val viewModel: DisplayViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DisplayViewModel(settingsRepository) as T
        },
    )

    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val decimalDigits by viewModel.decimalDigits.collectAsState()
    val decimalSeparator by viewModel.decimalSeparator.collectAsState()
    val thousandsSeparator by viewModel.thousandsSeparator.collectAsState()
    val showTransactionNotes by viewModel.showTransactionNotes.collectAsState()

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
        onResetAllPreferences = { viewModel.resetAllPreferences() },
        onNavigateBack = { navController.popBackStack() },
    )
}

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
    onResetAllPreferences: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDecimalDigitsDialog by remember { mutableStateOf(false) }
    var showDecimalSeparatorDialog by remember { mutableStateOf(false) }
    var showThousandsSeparatorDialog by remember { mutableStateOf(false) }
    var showResetPreferencesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_display)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppCard(
                title = stringResource(R.string.settings_currency_symbol),
                subtitle = CurrencyFormat.SUPPORTED_CURRENCIES.find { it.first == currencySymbol }?.second ?: currencySymbol,
                leadingIcon = Icons.Default.MonetizationOn,
                onClick = { showCurrencyDialog = true },
            )
            AppCard(
                title = stringResource(R.string.settings_decimal_digits),
                subtitle = stringResource(R.string.settings_decimal_digits_subtitle, decimalDigits),
                leadingIcon = Icons.Default.Exposure,
                onClick = { showDecimalDigitsDialog = true },
            )
            AppCard(
                title = stringResource(R.string.settings_decimal_separator),
                subtitle = separatorLabel(decimalSeparator, isThou = false),
                leadingIcon = Icons.Default.TextFields,
                onClick = { showDecimalSeparatorDialog = true },
            )
            AppCard(
                title = stringResource(R.string.settings_thousands_separator),
                subtitle = separatorLabel(thousandsSeparator, isThou = true),
                leadingIcon = Icons.Default.MoreHoriz,
                onClick = { showThousandsSeparatorDialog = true },
            )
            AppCard(
                title = "Mostra Note Transazioni",
                subtitle = if (showTransactionNotes) "Note visibili negli item" else "Note nascoste",
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
                title = stringResource(R.string.settings_reset_preferences),
                subtitle = stringResource(R.string.settings_reset_preferences_subtitle),
                leadingIcon = Icons.Default.Refresh,
                iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
                showChevron = false,
                onClick = { showResetPreferencesDialog = true },
            )
            // Live format preview
            Text(
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
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
        }
    }

    if (showCurrencyDialog) {
        CurrencySymbolDialog(
            currentSymbol = currencySymbol,
            onSymbolSelected = { onCurrencySymbolSelected(it); showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false },
        )
    }

    if (showDecimalDigitsDialog) {
        DecimalDigitsDialog(
            currentDigits = decimalDigits,
            onDigitsSelected = { onDecimalDigitsSelected(it); showDecimalDigitsDialog = false },
            onDismiss = { showDecimalDigitsDialog = false },
        )
    }

    if (showDecimalSeparatorDialog) {
        SeparatorDialog(
            title = stringResource(R.string.dialog_choose_decimal_separator),
            options = CurrencyFormat.DECIMAL_SEPARATORS.filter { it.first != thousandsSeparator },
            currentValue = decimalSeparator,
            onSelected = { onDecimalSeparatorSelected(it); showDecimalSeparatorDialog = false },
            onDismiss = { showDecimalSeparatorDialog = false },
        )
    }

    if (showThousandsSeparatorDialog) {
        SeparatorDialog(
            title = stringResource(R.string.dialog_choose_thousands_separator),
            options = CurrencyFormat.THOUSANDS_SEPARATORS.filter { it.first != decimalSeparator },
            currentValue = thousandsSeparator,
            onSelected = { onThousandsSeparatorSelected(it); showThousandsSeparatorDialog = false },
            onDismiss = { showThousandsSeparatorDialog = false },
        )
    }

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
            title = { Text(stringResource(R.string.dialog_reset_preferences_title)) },
            text = {
                Text(
                    stringResource(R.string.dialog_reset_preferences_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onResetAllPreferences()
                    showResetPreferencesDialog = false
                }) {
                    Text(stringResource(R.string.dialog_reset), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPreferencesDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

@Composable
private fun separatorLabel(value: String, isThou: Boolean): String {
    val options = if (isThou) CurrencyFormat.THOUSANDS_SEPARATORS else CurrencyFormat.DECIMAL_SEPARATORS
    return options.find { it.first == value }?.second ?: when (value) {
        "," -> stringResource(R.string.settings_separator_comma)
        "." -> stringResource(R.string.settings_separator_period)
        " " -> stringResource(R.string.settings_separator_space)
        ""  -> stringResource(R.string.settings_separator_none)
        else -> value
    }
}

@Composable
private fun CurrencySymbolDialog(
    currentSymbol: String,
    onSymbolSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.dialog_choose_currency)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                CurrencyFormat.SUPPORTED_CURRENCIES.forEach { (symbol, label) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            RadioButton(
                                selected = symbol == currentSymbol,
                                onClick = { onSymbolSelected(symbol) },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@Composable
private fun DecimalDigitsDialog(
    currentDigits: Int,
    onDigitsSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Exposure,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.dialog_choose_decimal_digits)) },
        text = {
            Column {
                (0..4).forEach { digits ->
                    ListItem(
                        headlineContent = {
                            Text(stringResource(R.string.settings_decimal_digits_subtitle, digits))
                        },
                        leadingContent = {
                            RadioButton(
                                selected = digits == currentDigits,
                                onClick = { onDigitsSelected(digits) },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@Composable
private fun SeparatorDialog(
    title: String,
    options: List<Pair<String, String>>,
    currentValue: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            RadioButton(
                                selected = value == currentValue,
                                onClick = { onSelected(value) },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@Preview(showBackground = true)
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
            onResetAllPreferences = {},
            onNavigateBack = {},
        )
    }
}
