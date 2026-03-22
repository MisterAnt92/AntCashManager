package com.antcashmanager.android.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppCard
import com.antcashmanager.android.ui.components.AppCardSectionHeader
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    navController: androidx.navigation.NavController,
) {
    Logger.d("SettingsScreen") { "Displaying SettingsScreen" }
    val viewModel: SettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(settingsRepository, transactionRepository, categoryRepository) as T
        },
    )
    val currentTheme by viewModel.theme.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()
    val deleteResult by viewModel.deleteResult.collectAsState()
    val showCharts by viewModel.showCharts.collectAsState()
    val highContrast by viewModel.highContrast.collectAsState()
    val largeText by viewModel.largeText.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val decimalDigits by viewModel.decimalDigits.collectAsState()
    val decimalSeparator by viewModel.decimalSeparator.collectAsState()
    val thousandsSeparator by viewModel.thousandsSeparator.collectAsState()

    SettingsContent(
        currentTheme = currentTheme,
        onThemeSelected = { viewModel.setTheme(it) },
        currentLanguage = currentLanguage,
        onLanguageSelected = { viewModel.setLanguage(it) },
        versionName = BuildConfig.VERSION_NAME,
        onDeleteAllData = { viewModel.deleteAllData() },
        deleteResult = deleteResult,
        onResetDeleteResult = { viewModel.resetDeleteResult() },
        showCharts = showCharts,
        onShowChartsChanged = { viewModel.setShowCharts(it) },
        highContrast = highContrast,
        onHighContrastChanged = { viewModel.setHighContrast(it) },
        largeText = largeText,
        onLargeTextChanged = { viewModel.setLargeText(it) },
        reduceMotion = reduceMotion,
        onReduceMotionChanged = { viewModel.setReduceMotion(it) },
        currencySymbol = currencySymbol,
        onCurrencySymbolSelected = { viewModel.setCurrencySymbol(it) },
        decimalDigits = decimalDigits,
        onDecimalDigitsSelected = { viewModel.setDecimalDigits(it) },
        decimalSeparator = decimalSeparator,
        onDecimalSeparatorSelected = { viewModel.setDecimalSeparator(it) },
        thousandsSeparator = thousandsSeparator,
        onThousandsSeparatorSelected = { viewModel.setThousandsSeparator(it) },
        onResetAllPreferences = { viewModel.resetAllPreferences() },
        onImportDebugData = { ctx -> viewModel.importDebugData(ctx) },
        navController = navController,
    )
}

@Composable
internal fun SettingsContent(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    versionName: String,
    onDeleteAllData: () -> Unit = {},
    deleteResult: DeleteResult = DeleteResult.Idle,
    onResetDeleteResult: () -> Unit = {},
    showCharts: Boolean = true,
    onShowChartsChanged: (Boolean) -> Unit = {},
    highContrast: Boolean = false,
    onHighContrastChanged: (Boolean) -> Unit = {},
    largeText: Boolean = false,
    onLargeTextChanged: (Boolean) -> Unit = {},
    reduceMotion: Boolean = false,
    onReduceMotionChanged: (Boolean) -> Unit = {},
    currencySymbol: String = "\u20ac",
    onCurrencySymbolSelected: (String) -> Unit = {},
    decimalDigits: Int = 2,
    onDecimalDigitsSelected: (Int) -> Unit = {},
    decimalSeparator: String = ",",
    onDecimalSeparatorSelected: (String) -> Unit = {},
    thousandsSeparator: String = ".",
    onThousandsSeparatorSelected: (String) -> Unit = {},
    onResetAllPreferences: () -> Unit = {},
    onImportDebugData: (android.content.Context) -> Unit = {},
    navController: androidx.navigation.NavController? = null,
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteSuccessDialog by remember { mutableStateOf(false) }
    var showLibrariesDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDecimalDigitsDialog by remember { mutableStateOf(false) }
    var showDecimalSeparatorDialog by remember { mutableStateOf(false) }
    var showThousandsSeparatorDialog by remember { mutableStateOf(false) }
    var showResetPreferencesDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(deleteResult) {
        when (deleteResult) {
            is DeleteResult.Success -> {
                showDeleteSuccessDialog = true
                onResetDeleteResult()
            }
            is DeleteResult.Error -> {
                onResetDeleteResult()
            }
            DeleteResult.Idle -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        // Detect multiple taps on title to trigger debug import when in debug build
        var titleTapCount by remember { mutableStateOf(0) }
        val context = LocalContext.current

        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.clickable {
                if (com.antcashmanager.android.BuildConfig.DEBUG) {
                    titleTapCount += 1
                    if (titleTapCount >= 5) {
                        titleTapCount = 0
                        // call the provided callback which will perform import in ViewModel
                        onImportDebugData(context)
                        android.widget.Toast.makeText(context, context.getString(R.string.debug_import_started), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )

        // ── Appearance Section ──
        AppCardSectionHeader(title = stringResource(R.string.settings_appearance))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = stringResource(R.string.settings_theme),
                subtitle = when (currentTheme) {
                    AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
                    AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
                    AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
                },
                leadingIcon = Icons.Default.Palette,
                onClick = { showThemeDialog = true },
            )
            AppCard(
                title = stringResource(R.string.settings_language),
                subtitle = languageDisplayName(currentLanguage),
                leadingIcon = Icons.Default.Language,
                onClick = { showLanguageDialog = true },
            )
        }

        // ── Display Section ──
        AppCardSectionHeader(title = stringResource(R.string.settings_display))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = stringResource(R.string.settings_display),
                subtitle = stringResource(R.string.settings_display_subtitle),
                leadingIcon = Icons.Default.TextFields,
                onClick = { navController?.navigate("display") },
            )
        }

        // ── Accessibility Section ──
        AppCardSectionHeader(title = stringResource(R.string.settings_accessibility))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = stringResource(R.string.settings_high_contrast),
                subtitle = stringResource(R.string.settings_high_contrast_subtitle),
                leadingIcon = Icons.Default.Contrast,
                trailingContent = {
                    Switch(
                        checked = highContrast,
                        onCheckedChange = onHighContrastChanged,
                    )
                },
                onClick = { onHighContrastChanged(!highContrast) },
            )
            AppCard(
                title = stringResource(R.string.settings_large_text),
                subtitle = stringResource(R.string.settings_large_text_subtitle),
                leadingIcon = Icons.Default.FormatSize,
                trailingContent = {
                    Switch(
                        checked = largeText,
                        onCheckedChange = onLargeTextChanged,
                    )
                },
                onClick = { onLargeTextChanged(!largeText) },
            )
            AppCard(
                title = stringResource(R.string.settings_reduce_motion),
                subtitle = stringResource(R.string.settings_reduce_motion_subtitle),
                leadingIcon = Icons.Default.MotionPhotosOff,
                trailingContent = {
                    Switch(
                        checked = reduceMotion,
                        onCheckedChange = onReduceMotionChanged,
                    )
                },
                onClick = { onReduceMotionChanged(!reduceMotion) },
            )
        }

        // ── Data Management Section ──
        AppCardSectionHeader(title = stringResource(R.string.settings_data_management))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = stringResource(R.string.settings_delete_all),
                subtitle = stringResource(R.string.settings_delete_all_subtitle),
                leadingIcon = Icons.Default.Delete,
                iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
                showChevron = false,
                onClick = { showDeleteConfirmDialog = true },
            )
            AppCard(
                title = stringResource(R.string.settings_backup),
                subtitle = stringResource(R.string.settings_backup_subtitle),
                leadingIcon = Icons.Default.Backup,
                iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = {
                    // TODO: Implement backup functionality
                    Logger.d("Settings") { "Backup data tapped" }
                },
            )
            AppCard(
                title = stringResource(R.string.settings_restore),
                subtitle = stringResource(R.string.settings_restore_subtitle),
                leadingIcon = Icons.Default.RestorePage,
                iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = {
                    // TODO: Implement restore functionality
                    Logger.d("Settings") { "Restore data tapped" }
                },
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
        }

        // ── Support Section ──
        AppCardSectionHeader(title = stringResource(R.string.settings_support))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = stringResource(R.string.settings_send_feedback),
                subtitle = stringResource(R.string.settings_send_feedback_subtitle),
                leadingIcon = Icons.Default.Feedback,
                iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = {
                    val emailIntent = Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:")
                        putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("feedback@antcashmanager.com"))
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "AntCashManager Feedback - v$versionName")
                    }
                    if (emailIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(emailIntent)
                    }
                },
            )
            AppCard(
                title = stringResource(R.string.settings_privacy_policy),
                subtitle = stringResource(R.string.settings_privacy_policy_subtitle),
                leadingIcon = Icons.Default.PrivacyTip,
                iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { showPrivacyDialog = true },
            )
            DonationCard(context = context)
        }

        // ── About Section ──
        AppCardSectionHeader(title = stringResource(R.string.settings_about))
        AppCard(
            title = stringResource(R.string.settings_app_version),
            subtitle = versionName,
            leadingIcon = Icons.Default.Info,
            showChevron = false,
        )

        // ── Third-party Libraries Section ──
        AppCardSectionHeader(title = stringResource(R.string.settings_third_party))
        AppCard(
            title = stringResource(R.string.settings_third_party_libraries),
            subtitle = stringResource(R.string.settings_third_party_subtitle),
            leadingIcon = Icons.AutoMirrored.Filled.LibraryBooks,
            onClick = { showLibrariesDialog = true },
        )
    }

    // ── Dialogs ──
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                onThemeSelected(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                onLanguageSelected(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { Text(stringResource(R.string.dialog_delete_all_title)) },
            text = {
                Text("Test")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAllData()
                        showDeleteConfirmDialog = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    if (showDeleteSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSuccessDialog = false },
            title = { Text(stringResource(R.string.dialog_data_deleted)) },
            text = { Text(stringResource(R.string.dialog_data_deleted_message)) },
            confirmButton = {
                TextButton(onClick = { showDeleteSuccessDialog = false }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    if (showLibrariesDialog) {
        ThirdPartyLibrariesDialog(
            context = context,
            onDismiss = { showLibrariesDialog = false },
        )
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
private fun languageDisplayName(language: AppLanguage): String = when (language) {
    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
    AppLanguage.ENGLISH -> stringResource(R.string.language_english)
    AppLanguage.ITALIAN -> stringResource(R.string.language_italian)
    AppLanguage.FRENCH -> stringResource(R.string.language_french)
    AppLanguage.GERMAN -> stringResource(R.string.language_german)
    AppLanguage.SPANISH -> stringResource(R.string.language_spanish)
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
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

/**
 * Card di donazione con due opzioni: PayPal e Buy Me a Coffee.
 * Ciascuna riga è cliccabile e apre il rispettivo link di donazione.
 */
@Composable
private fun DonationCard(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            // ── Card header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD54F)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = Color(0xFF5D4037),
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_buy_coffee),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.settings_buy_coffee_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // ── PayPal row ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.paypal.com/paypalme/misterant92"),
                        )
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF003087)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PayPal",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.settings_donate_paypal_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // ── Buy Me a Coffee row ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://buymeacoffee.com/misterant92"),
                        )
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFDD00)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = Color(0xFF000000),
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Buy Me a Coffee",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.settings_donate_bmc_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class LibraryInfo(
    val name: String,
    val url: String,
)

private val thirdPartyLibraries = listOf(
    LibraryInfo("Jetpack Compose", "https://developer.android.com/jetpack/compose"),
    LibraryInfo("Room Database", "https://developer.android.com/training/data-storage/room"),
    LibraryInfo("Navigation Compose", "https://developer.android.com/jetpack/compose/navigation"),
    LibraryInfo("DataStore Preferences", "https://developer.android.com/topic/libraries/architecture/datastore"),
    LibraryInfo("Material Icons Extended", "https://fonts.google.com/icons"),
    LibraryInfo("Kotlinx Coroutines", "https://github.com/Kotlin/kotlinx.coroutines"),
    LibraryInfo("Kermit Logger", "https://github.com/touchlab/Kermit"),
)

@Composable
private fun ThirdPartyLibrariesDialog(
    context: android.content.Context,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.settings_third_party_libraries)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                thirdPartyLibraries.forEach { lib ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = lib.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lib.url))
                            context.startActivity(intent)
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_close)) }
        },
    )
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.dialog_choose_theme)) },
        text = {
            Column {
                AppTheme.entries.forEach { theme ->
                    ListItem(
                        headlineContent = {
                            Text(
                                when (theme) {
                                    AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
                                    AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
                                    AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
                                },
                            )
                        },
                        leadingContent = {
                            RadioButton(
                                selected = theme == currentTheme,
                                onClick = { onThemeSelected(theme) },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
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
private fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.dialog_choose_language)) },
        text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    ListItem(
                        headlineContent = {
                            Text(languageDisplayName(language))
                        },
                        leadingContent = {
                            RadioButton(
                                selected = language == currentLanguage,
                                onClick = { onLanguageSelected(language) },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
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
private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.PrivacyTip,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.privacy_policy_title)) },
        text = {
            Text(
                text = stringResource(R.string.privacy_policy_content),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_close)) }
        },
    )
}

// ── Previews ──

@Preview(showBackground = true, name = "SettingsScreen - Light")
@Composable
private fun SettingsContentLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        SettingsContent(
            currentTheme = AppTheme.LIGHT,
            onThemeSelected = {},
            versionName = "1.0.0",
        )
    }
}

@Preview(showBackground = true, name = "SettingsScreen - Dark")
@Composable
private fun SettingsContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        SettingsContent(
            currentTheme = AppTheme.DARK,
            onThemeSelected = {},
            versionName = "1.0.0",
        )
    }
}

@Preview(showBackground = true, name = "ThemeSelectionDialog")
@Composable
private fun ThemeSelectionDialogPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ThemeSelectionDialog(
            currentTheme = AppTheme.SYSTEM,
            onThemeSelected = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "LanguageSelectionDialog")
@Composable
private fun LanguageSelectionDialogPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        LanguageSelectionDialog(
            currentLanguage = AppLanguage.SYSTEM,
            onLanguageSelected = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "DonationCard - Light")
@Composable
private fun DonationCardLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
            DonationCard(context = androidx.compose.ui.platform.LocalContext.current)
        }
    }
}

@Preview(showBackground = true, name = "DonationCard - Dark")
@Composable
private fun DonationCardDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
            DonationCard(context = androidx.compose.ui.platform.LocalContext.current)
        }
    }
}
