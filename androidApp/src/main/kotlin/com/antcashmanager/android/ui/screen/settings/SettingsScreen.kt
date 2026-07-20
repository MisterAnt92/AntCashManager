package com.antcashmanager.android.ui.screen.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.navigation.BottomNavItem
import com.antcashmanager.android.ui.components.AppListItem
import com.antcashmanager.android.ui.components.AppRadioButton
import com.antcashmanager.android.ui.components.AppSwitch
import com.antcashmanager.android.ui.components.HelpButton
import com.antcashmanager.android.ui.components.animation.AntEasterEggAnimation
import com.antcashmanager.android.ui.components.card.AppCard
import com.antcashmanager.android.ui.components.card.AppCardSectionHeader
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.settings.view.DecimalDigitsDialog
import com.antcashmanager.android.ui.screen.settings.view.HelpDialog
import com.antcashmanager.android.ui.screen.settings.view.LanguageSelectionDialog
import com.antcashmanager.android.ui.screen.settings.view.PrivacyPolicyDialog
import com.antcashmanager.android.ui.screen.settings.view.SeparatorDialog
import com.antcashmanager.android.ui.screen.settings.view.ThemeSelectionDialog
import com.antcashmanager.android.ui.screen.settings.view.ThirdPartyLibrariesDialog
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.CurrencyFormat
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Logger.d("SettingsScreen") { "Displaying SettingsScreen" }
    val context = LocalContext.current
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    SettingsContent(
        currentTheme = state.theme,
        onThemeSelected = { viewModel.setTheme(it) },
        currentLanguage = state.language,
        onLanguageSelected = { viewModel.setLanguage(it) },
        versionName = BuildConfig.VERSION_NAME,
        showCharts = state.showCharts,
        onShowChartsChanged = { viewModel.setShowCharts(it) },
        highContrast = state.highContrast,
        onHighContrastChanged = { viewModel.setHighContrast(it) },
        largeText = state.largeText,
        onLargeTextChanged = { viewModel.setLargeText(it) },
        reduceMotion = state.reduceMotion,
        onReduceMotionChanged = { viewModel.setReduceMotion(it) },
        currencySymbol = state.currencySymbol,
        onCurrencySymbolSelected = { viewModel.setCurrencySymbol(it) },
        decimalDigits = state.decimalDigits,
        onDecimalDigitsSelected = { viewModel.setDecimalDigits(it) },
        decimalSeparator = state.decimalSeparator,
        onDecimalSeparatorSelected = { viewModel.setDecimalSeparator(it) },
        thousandsSeparator = state.thousandsSeparator,
        onThousandsSeparatorSelected = { viewModel.setThousandsSeparator(it) },
        onImportDebugData = { ctx -> viewModel.importDebugData(ctx) },
        onSendFeedbackEmail = { emailBody ->
            val success = viewModel.sendFeedbackEmail(emailBody, context)
            if (!success) {
                Toast.makeText(
                    context,
                    context.getString(R.string.no_email_app_installed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        onShowTutorial = {
            viewModel.setIsTutorialCompleted(false)
            navController.navigate(BottomNavItem.Home.route) {
                launchSingleTop = true
            }
        },
        navController = navController,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsContent(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    versionName: String,
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
    thousandsSeparator: String = "",
    onThousandsSeparatorSelected: (String) -> Unit = {},
    onImportDebugData: (Context) -> Unit = {},
    onSendFeedbackEmail: (String) -> Unit = {},
    onShowTutorial: () -> Unit = {},
    navController: NavController? = null,
    modifier: Modifier = Modifier,
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLibrariesDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDecimalDigitsDialog by remember { mutableStateOf(false) }
    var showDecimalSeparatorDialog by remember { mutableStateOf(false) }
    var showThousandsSeparatorDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAntAnimation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val analyticsManager: AnalyticsManager = koinInject()

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
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
                .padding(bottom = 24.dp),
        ) {
            // Detect multiple taps on title to trigger debug import when in debug build
            var titleTapCount by remember { mutableStateOf(0) }
            val context = LocalContext.current

            // Custom header with debug tap functionality
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppText(
                    text = stringResource(R.string.common_settings),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (BuildConfig.DEBUG) {
                                titleTapCount += 1
                                if (titleTapCount >= 5) {
                                    titleTapCount = 0
                                    // call the provided callback which will perform import in ViewModel
                                    onImportDebugData(context)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.debug_import_started),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                )
                HelpButton(onHelpClick = { showHelpDialog = true })
            }
            Spacer(modifier = Modifier.height(12.dp))

            // ── Appearance Section ──
            // ... rest of Column content ...
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
                        AppSwitch(
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
                        AppSwitch(
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
                        AppSwitch(
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
                    title = stringResource(R.string.settings_data_management),
                    leadingIcon = Icons.Default.Backup,
                    iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = { navController?.navigate("settings_data") },
                )
            }

            // ── Support Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_support))
            val feedbackEmailBody = stringResource(
                when (currentLanguage) {
                    AppLanguage.ITALIAN -> R.string.feedback_email_body_italian
                    AppLanguage.ENGLISH -> R.string.feedback_email_body_english
                    AppLanguage.FRENCH -> R.string.feedback_email_body_french
                    AppLanguage.GERMAN -> R.string.feedback_email_body_german
                    AppLanguage.SPANISH -> R.string.feedback_email_body_spanish
                    AppLanguage.SYSTEM -> R.string.feedback_email_body_english
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCard(
                    title = stringResource(R.string.settings_send_feedback),
                    subtitle = stringResource(R.string.settings_send_feedback_subtitle),
                    leadingIcon = Icons.Default.Feedback,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        analyticsManager.logEvent("feedback_email_sent")
                        onSendFeedbackEmail(feedbackEmailBody)
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
            }

            // ── Tutorial Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_tutorial))
            AppCard(
                title = stringResource(R.string.settings_show_tutorial),
                subtitle = stringResource(R.string.settings_show_tutorial_subtitle),
                leadingIcon = Icons.Default.Info,
                onClick = {
                    analyticsManager.logEvent("tutorial_replay_requested")
                    onShowTutorial()
                },
            )

            // ── About Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_about))
            AppCard(
                title = stringResource(R.string.settings_app_version),
                subtitle = versionName,
                leadingIcon = Icons.Default.Info,
                showChevron = false,
                onClick = {
                    showAntAnimation = true
                },
            )

            // ── Third-party Libraries Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_third_party))
            AppCard(
                title = stringResource(R.string.settings_third_party_libraries),
                subtitle = stringResource(R.string.settings_third_party_subtitle),
                leadingIcon = Icons.AutoMirrored.Filled.LibraryBooks,
                onClick = { showLibrariesDialog = true },
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppText(
                text = stringResource(R.string.settings_project_by),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    } // Closing Scaffold

    // ── Dialogs ──
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                analyticsManager.logEvent("theme_changed")
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
                analyticsManager.logEvent("language_changed")
                onLanguageSelected(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
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
            onSymbolSelected = {
                analyticsManager.logEvent("currency_format_changed")
                onCurrencySymbolSelected(it)
                showCurrencyDialog = false
            },
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

    if (showAntAnimation) {
        AntEasterEggAnimation(
            versionName = versionName,
            onDismiss = { showAntAnimation = false }
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
        title = { AppText(stringResource(R.string.dialog_choose_currency)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                CurrencyFormat.SUPPORTED_CURRENCIES.forEach { (symbol, label) ->
                    AppListItem(
                        headlineContent = { AppText(label) },
                        leadingContent = {
                            AppRadioButton(
                                selected = symbol == currentSymbol,
                                onClick = { onSymbolSelected(symbol) },
                            )
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { AppText(stringResource(R.string.common_cancel)) }
        },
    )
}

// ── Previews ──

@Preview(showBackground = true, name = "SettingsScreen - Light")
@Preview(showBackground = true, name = "SettingsScreen - 7 inch", widthDp = 600, heightDp = 960)
@Preview(showBackground = true, name = "SettingsScreen - 10 inch", widthDp = 840, heightDp = 1280)
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
