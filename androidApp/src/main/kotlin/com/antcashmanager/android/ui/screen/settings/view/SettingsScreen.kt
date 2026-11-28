package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.R
import com.antcashmanager.android.navigation.AppRoute
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import com.antcashmanager.android.navigation.ScreenHeaderConfig
import com.antcashmanager.android.ui.components.animation.AntEasterEggAnimation
import com.antcashmanager.android.ui.components.card.AppCard
import com.antcashmanager.android.ui.components.card.AppCardSectionHeader
import com.antcashmanager.android.ui.components.common.AppSwitch
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.settings.SettingEvent
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.CurrencyFormat
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Logger.d(tag = "SettingsScreen") { "Displaying SettingsScreen" }
    val context = LocalContext.current
    val noEmailAppInstalledMessage = stringResource(R.string.no_email_app_installed)
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val analyticsConsent by viewModel.analyticsConsent.collectAsStateWithLifecycle()

    SettingsContent(
        currentTheme = state.theme,
        onThemeSelected = { viewModel.emitEvent(SettingEvent.SetThemeEvent(it)) },
        currentLanguage = state.language,
        onLanguageSelected = { viewModel.emitEvent(SettingEvent.SetLanguageEvent(it)) },
        versionName = BuildConfig.VERSION_NAME,
        showCharts = state.showCharts,
        onShowChartsChanged = { viewModel.emitEvent(SettingEvent.SetShowChartsEvent(it)) },
        highContrast = state.highContrast,
        onHighContrastChanged = { viewModel.emitEvent(SettingEvent.SetHighContrastEvent(it)) },
        largeText = state.largeText,
        onLargeTextChanged = { viewModel.emitEvent(SettingEvent.SetLargeTextEvent(it)) },
        reduceMotion = state.reduceMotion,
        onReduceMotionChanged = { viewModel.emitEvent(SettingEvent.SetReduceMotionEvent(it)) },
        currencySymbol = state.currencySymbol,
        onCurrencySymbolSelected = { viewModel.emitEvent(SettingEvent.SetCurrencySymbolEvent(it)) },
        decimalDigits = state.decimalDigits,
        onDecimalDigitsSelected = { viewModel.emitEvent(SettingEvent.SetDecimalDigitsEvent(it)) },
        decimalSeparator = state.decimalSeparator,
        onDecimalSeparatorSelected = { viewModel.emitEvent(SettingEvent.SetDecimalSeparatorEvent(it)) },
        thousandsSeparator = state.thousandsSeparator,
        onThousandsSeparatorSelected = { viewModel.emitEvent(SettingEvent.SetThousandsSeparatorEvent(it)) },
        activeProfileName = state.activeProfileName,
        analyticsConsent = analyticsConsent,
        onAnalyticsConsentChanged = { viewModel.emitEvent(SettingEvent.SetAnalyticsConsentEvent(it)) },
        onImportDebugData = { viewModel.emitEvent(SettingEvent.ImportDebugDataEvent) },
        onSendFeedbackEmail = { emailBody ->
            viewModel.emitEvent(SettingEvent.SendFeedbackEmailEvent(emailBody))
        },
        onEvent = viewModel::emitEvent,
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
    activeProfileName: String = "",
    analyticsConsent: Boolean? = null,
    onAnalyticsConsentChanged: (Boolean) -> Unit = {},
    onImportDebugData: () -> Unit = {},
    onSendFeedbackEmail: (String) -> Unit = {},
    onEvent: (SettingEvent) -> Unit = {},
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
    var showAntAnimation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Configure screen header with actions
    val headerConfigCallback = LocalScreenHeaderConfigCallback.current
    val settingsTitle = stringResource(R.string.common_settings)
    LaunchedEffect(Unit) {
        headerConfigCallback?.invoke(
            ScreenHeaderConfig(
                title = settingsTitle,
            ),
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.testTag("settings_screen"),
    ) { padding ->
        Column(
            modifier =
                Modifier
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
            // ── Profile Section ──
            AppCardSectionHeader(title = stringResource(R.string.profiles_section_title))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCard(
                    title = stringResource(R.string.settings_profiles),
                    subtitle = activeProfileName.takeIf { it.isNotEmpty() },
                    leadingIcon = Icons.Default.Person,
                    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = {
                        navController?.navigate(AppRoute.SettingsRoute.Profiles.route)
                    },
                )
            }
            VerticalSpacer(SpacingSize.SM)

            // ── Appearance Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_appearance))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCard(
                    title = stringResource(R.string.settings_theme),
                    subtitle =
                        when (currentTheme) {
                            AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
                            AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
                            AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
                            AppTheme.ANNA -> stringResource(R.string.settings_theme_anna)
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
                AppCard(
                    title = stringResource(R.string.settings_display),
                    subtitle = stringResource(R.string.settings_display_subtitle),
                    leadingIcon = Icons.Default.TextFields,
                    onClick = {
                        onEvent(SettingEvent.SubmenuOpenedEvent("display"))
                        navController?.navigate(AppRoute.SettingsRoute.Display.route)
                    },
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

            // ── Data & Privacy Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_section_data_privacy))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCard(
                    title = stringResource(R.string.settings_data_management),
                    leadingIcon = Icons.Default.Backup,
                    iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = {
                        onEvent(SettingEvent.SubmenuOpenedEvent("data_management"))
                        navController?.navigate(AppRoute.SettingsRoute.DataManagement.route)
                    },
                )
                // Analytics consent toggle (GDPR — user can revoke at any time)
                if (analyticsConsent != null) {
                    AppCard(
                        title = stringResource(R.string.settings_analytics_consent_title),
                        subtitle = stringResource(R.string.settings_analytics_consent_desc),
                        leadingIcon = Icons.Default.PrivacyTip,
                        iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        trailingContent = {
                            AppSwitch(
                                checked = analyticsConsent == true,
                                onCheckedChange = { onAnalyticsConsentChanged(it) },
                            )
                        },
                        onClick = { onAnalyticsConsentChanged(analyticsConsent != true) },
                    )
                }
            }

            // ── Support Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_support))
            val feedbackEmailBody =
                stringResource(
                    when (currentLanguage) {
                        AppLanguage.ITALIAN -> R.string.feedback_email_body_italian
                        AppLanguage.ENGLISH -> R.string.feedback_email_body_english
                        AppLanguage.FRENCH -> R.string.feedback_email_body_french
                        AppLanguage.GERMAN -> R.string.feedback_email_body_german
                        AppLanguage.SPANISH -> R.string.feedback_email_body_spanish
                        AppLanguage.CHINESE_SIMPLIFIED -> R.string.feedback_email_body_english
                        AppLanguage.CHINESE_TRADITIONAL -> R.string.feedback_email_body_english
                        AppLanguage.JAPANESE -> R.string.feedback_email_body_english
                        AppLanguage.POLISH -> R.string.feedback_email_body_english
                        AppLanguage.HINDI -> R.string.feedback_email_body_english
                        AppLanguage.RUSSIAN -> R.string.feedback_email_body_english
                        AppLanguage.UKRAINIAN -> R.string.feedback_email_body_english
                        AppLanguage.KOREAN -> R.string.feedback_email_body_english
                        AppLanguage.SYSTEM -> R.string.feedback_email_body_english
                    },
                )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCard(
                    title = stringResource(R.string.settings_show_tutorial),
                    subtitle = stringResource(R.string.settings_show_tutorial_subtitle),
                    leadingIcon = Icons.Default.PlayArrow,
                    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onEvent(SettingEvent.SetTutorialCompletedEvent(false)) },
                )
                AppCard(
                    title = stringResource(R.string.settings_send_feedback),
                    subtitle = stringResource(R.string.settings_send_feedback_subtitle),
                    leadingIcon = Icons.Default.Feedback,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        onEvent(SettingEvent.FeedbackEmailOpenedEvent)
                        onSendFeedbackEmail(feedbackEmailBody)
                    },
                )
                AppCard(
                    title = stringResource(R.string.settings_privacy_policy),
                    subtitle = stringResource(R.string.settings_privacy_policy_subtitle),
                    leadingIcon = Icons.Default.PrivacyTip,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        onEvent(SettingEvent.PrivacyPolicyOpenedEvent)
                        showPrivacyDialog = true
                    },
                )
            }

            // ── Information Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_about))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCard(
                    title = stringResource(R.string.settings_app_version),
                    subtitle = versionName,
                    leadingIcon = Icons.Default.Info,
                    showChevron = false,
                    onClick = {
                        onEvent(SettingEvent.EasterEggOpenedEvent)
                        showAntAnimation = true
                    },
                )
                AppCard(
                    title = stringResource(R.string.settings_third_party_libraries),
                    subtitle = stringResource(R.string.settings_third_party_subtitle),
                    leadingIcon = Icons.AutoMirrored.Filled.LibraryBooks,
                    onClick = {
                        onEvent(SettingEvent.ThirdPartyLibsOpenedEvent)
                        showLibrariesDialog = true
                    },
                )
            }

            VerticalSpacer(SpacingSize.LG)

            AppText(
                text = stringResource(R.string.settings_project_by),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    } // Closing Scaffold

    // ── Dialogs ──
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                onThemeSelected(theme)
                showThemeDialog = false
            },
            onDismiss = {
                onEvent(SettingEvent.DialogDismissedEvent("theme_selection"))
                showThemeDialog = false
            },
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                onLanguageSelected(language)
                showLanguageDialog = false
            },
            onDismiss = {
                onEvent(SettingEvent.DialogDismissedEvent("language_selection"))
                showLanguageDialog = false
            },
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = {
                onEvent(SettingEvent.DialogDismissedEvent("privacy_policy"))
                showPrivacyDialog = false
            },
        )
    }

    if (showLibrariesDialog) {
        ThirdPartyLibrariesDialog(
            context = context,
            onDismiss = {
                onEvent(SettingEvent.DialogDismissedEvent("third_party_libraries"))
                showLibrariesDialog = false
            },
        )
    }

    if (showCurrencyDialog) {
        CurrencySymbolDialog(
            currentSymbol = currencySymbol,
            onSymbolSelected = {
                onCurrencySymbolSelected(it)
                showCurrencyDialog = false
            },
            onDismiss = {
                onEvent(SettingEvent.DialogDismissedEvent("currency_symbol"))
                showCurrencyDialog = false
            },
        )
    }

    if (showDecimalDigitsDialog) {
        DecimalDigitsDialog(
            currentDigits = decimalDigits,
            onDigitsSelected = {
                onDecimalDigitsSelected(it)
                showDecimalDigitsDialog = false
            },
            onDismiss = {
                onEvent(SettingEvent.DialogDismissedEvent("decimal_digits"))
                showDecimalDigitsDialog = false
            },
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
            onSelected = {
                onDecimalSeparatorSelected(it)
                showDecimalSeparatorDialog = false
            },
            onDismiss = {
                onEvent(SettingEvent.DialogDismissedEvent("decimal_separator"))
                showDecimalSeparatorDialog = false
            },
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
            onSelected = {
                onThousandsSeparatorSelected(it)
                showThousandsSeparatorDialog = false
            },
            onDismiss = {
                onEvent(SettingEvent.DialogDismissedEvent("thousands_separator"))
                showThousandsSeparatorDialog = false
            },
        )
    }

    if (showAntAnimation) {
        AntEasterEggAnimation(
            versionName = versionName,
            onDismiss = { showAntAnimation = false },
        )
    }
}

@Composable
private fun languageDisplayName(language: AppLanguage): String =
    when (language) {
        AppLanguage.SYSTEM -> stringResource(R.string.language_system)
        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
        AppLanguage.ITALIAN -> stringResource(R.string.language_italian)
        AppLanguage.FRENCH -> stringResource(R.string.language_french)
        AppLanguage.GERMAN -> stringResource(R.string.language_german)
        AppLanguage.SPANISH -> stringResource(R.string.language_spanish)
        AppLanguage.CHINESE_SIMPLIFIED -> stringResource(R.string.language_system)
        AppLanguage.CHINESE_TRADITIONAL -> stringResource(R.string.language_system)
        AppLanguage.JAPANESE -> stringResource(R.string.language_system)
        AppLanguage.POLISH -> stringResource(R.string.language_system)
        AppLanguage.HINDI -> stringResource(R.string.language_system)
        AppLanguage.RUSSIAN -> stringResource(R.string.language_system)
        AppLanguage.UKRAINIAN -> stringResource(R.string.language_system)
        AppLanguage.KOREAN -> stringResource(R.string.language_system)
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
