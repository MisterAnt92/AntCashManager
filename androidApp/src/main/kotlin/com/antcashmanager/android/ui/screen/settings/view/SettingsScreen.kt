package com.antcashmanager.android.ui.screen.settings.view

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.animation.AntEasterEggAnimation
import com.antcashmanager.android.ui.components.card.AppCard
import com.antcashmanager.android.ui.components.card.AppCardSectionHeader
import com.antcashmanager.android.ui.components.common.AppSwitch
import com.antcashmanager.android.ui.components.dialog.HelpButton
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.settings.SettingEvent
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.android.navigation.AppRoute
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import com.antcashmanager.android.navigation.ScreenHeaderConfig
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
    Logger.d(tag = "SettingsScreen") { "Displaying SettingsScreen" }
    val context = LocalContext.current
    val noEmailAppInstalledMessage = stringResource(R.string.no_email_app_installed)
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsContent(
        currentTheme = state.theme,
        onThemeSelected = { viewModel.onEvent(SettingEvent.SetTheme(it)) },
        currentLanguage = state.language,
        onLanguageSelected = { viewModel.onEvent(SettingEvent.SetLanguage(it)) },
        versionName = BuildConfig.VERSION_NAME,
        showCharts = state.showCharts,
        onShowChartsChanged = { viewModel.onEvent(SettingEvent.SetShowCharts(it)) },
        highContrast = state.highContrast,
        onHighContrastChanged = { viewModel.onEvent(SettingEvent.SetHighContrast(it)) },
        largeText = state.largeText,
        onLargeTextChanged = { viewModel.onEvent(SettingEvent.SetLargeText(it)) },
        reduceMotion = state.reduceMotion,
        onReduceMotionChanged = { viewModel.onEvent(SettingEvent.SetReduceMotion(it)) },
        currencySymbol = state.currencySymbol,
        onCurrencySymbolSelected = { viewModel.onEvent(SettingEvent.SetCurrencySymbol(it)) },
        decimalDigits = state.decimalDigits,
        onDecimalDigitsSelected = { viewModel.onEvent(SettingEvent.SetDecimalDigits(it)) },
        decimalSeparator = state.decimalSeparator,
        onDecimalSeparatorSelected = { viewModel.onEvent(SettingEvent.SetDecimalSeparator(it)) },
        thousandsSeparator = state.thousandsSeparator,
        onThousandsSeparatorSelected = { viewModel.onEvent(SettingEvent.SetThousandsSeparator(it)) },
        onImportDebugData = { ctx -> viewModel.onEvent(SettingEvent.ImportDebugData(ctx)) },
        onSendFeedbackEmail = { emailBody ->
            viewModel.onEvent(SettingEvent.SendFeedbackEmail(emailBody, context))
            // TODO: handle success/failure via errorState in state (FASE 5)
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
    val handleHighContrastChanged: (Boolean) -> Unit = { enabled ->
        analyticsManager.logEvent("settings_high_contrast_toggled")
        analyticsManager.logEvent("settings_accessibility_change", android.os.Bundle().apply {
            putString("setting", "high_contrast")
            putBoolean("enabled", enabled)
        })
        onHighContrastChanged(enabled)
    }
    val handleLargeTextChanged: (Boolean) -> Unit = { enabled ->
        analyticsManager.logEvent("settings_large_text_toggled")
        analyticsManager.logEvent("settings_accessibility_change", android.os.Bundle().apply {
            putString("setting", "large_text")
            putBoolean("enabled", enabled)
        })
        onLargeTextChanged(enabled)
    }
    val handleReduceMotionChanged: (Boolean) -> Unit = { enabled ->
        analyticsManager.logEvent("settings_reduce_motion_toggled")
        analyticsManager.logEvent("settings_accessibility_change", android.os.Bundle().apply {
            putString("setting", "reduce_motion")
            putBoolean("enabled", enabled)
        })
        onReduceMotionChanged(enabled)
    }

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    // Configure screen header with actions
    val headerConfigCallback = LocalScreenHeaderConfigCallback.current
    val settingsTitle = stringResource(R.string.common_settings)
    LaunchedEffect(Unit) {
        headerConfigCallback?.invoke(
            ScreenHeaderConfig(
                title = settingsTitle,
                actions = {
                    HelpButton(
                        onHelpClick = {
                            analyticsManager.logEvent("settings_help_opened")
                            showHelpDialog = true
                        },
                    )
                }
            )
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.testTag("settings_screen")
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
                    onClick = {
                        val params = android.os.Bundle().apply {
                            putString("submenu", "display")
                        }
                        analyticsManager.logEvent("settings_submenu_opened", params)
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
                            onCheckedChange = handleHighContrastChanged,
                        )
                    },
                    onClick = { handleHighContrastChanged(!highContrast) },
                )
                AppCard(
                    title = stringResource(R.string.settings_large_text),
                    subtitle = stringResource(R.string.settings_large_text_subtitle),
                    leadingIcon = Icons.Default.FormatSize,
                    trailingContent = {
                        AppSwitch(
                            checked = largeText,
                            onCheckedChange = handleLargeTextChanged,
                        )
                    },
                    onClick = { handleLargeTextChanged(!largeText) },
                )
                AppCard(
                    title = stringResource(R.string.settings_reduce_motion),
                    subtitle = stringResource(R.string.settings_reduce_motion_subtitle),
                    leadingIcon = Icons.Default.MotionPhotosOff,
                    trailingContent = {
                        AppSwitch(
                            checked = reduceMotion,
                            onCheckedChange = handleReduceMotionChanged,
                        )
                    },
                    onClick = { handleReduceMotionChanged(!reduceMotion) },
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
                    onClick = {
                        val params = android.os.Bundle().apply {
                            putString("submenu", "data_management")
                        }
                        analyticsManager.logEvent("settings_submenu_opened", params)
                        navController?.navigate(AppRoute.SettingsRoute.DataManagement.route)
                    },
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
                    AppLanguage.CHINESE_SIMPLIFIED -> R.string.feedback_email_body_english
                    AppLanguage.CHINESE_TRADITIONAL -> R.string.feedback_email_body_english
                    AppLanguage.JAPANESE -> R.string.feedback_email_body_english
                    AppLanguage.POLISH -> R.string.feedback_email_body_english
                    AppLanguage.HINDI -> R.string.feedback_email_body_english
                    AppLanguage.RUSSIAN -> R.string.feedback_email_body_english
                    AppLanguage.UKRAINIAN -> R.string.feedback_email_body_english
                    AppLanguage.KOREAN -> R.string.feedback_email_body_english
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
                    onClick = {
                        analyticsManager.logEvent("settings_privacy_policy_opened")
                        showPrivacyDialog = true
                    },
                )
            }

            // ── About Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_about))
            AppCard(
                title = stringResource(R.string.settings_app_version),
                subtitle = versionName,
                leadingIcon = Icons.Default.Info,
                showChevron = false,
                onClick = {
                    analyticsManager.logEvent("easter_egg_animation_opened")
                    showAntAnimation = true
                },
            )

            // ── Third-party Libraries Section ──
            AppCardSectionHeader(title = stringResource(R.string.settings_third_party))
            AppCard(
                title = stringResource(R.string.settings_third_party_libraries),
                subtitle = stringResource(R.string.settings_third_party_subtitle),
                leadingIcon = Icons.AutoMirrored.Filled.LibraryBooks,
                onClick = {
                    analyticsManager.logEvent("settings_third_party_libraries_opened")
                    showLibrariesDialog = true
                },
            )

            VerticalSpacer(SpacingSize.LG)

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
            onDismiss = {
                analyticsManager.logEvent("settings_dialog_dismissed", android.os.Bundle().apply {
                    putString("dialog_type", "theme_selection")
                })
                showThemeDialog = false
            },
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
            onDismiss = {
                analyticsManager.logEvent("settings_dialog_dismissed", android.os.Bundle().apply {
                    putString("dialog_type", "language_selection")
                })
                showLanguageDialog = false
            },
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = {
            analyticsManager.logEvent("settings_dialog_dismissed", android.os.Bundle().apply {
                putString("dialog_type", "privacy_policy")
            })
            showPrivacyDialog = false
        })
    }


    if (showLibrariesDialog) {
        ThirdPartyLibrariesDialog(
            context = context,
            onDismiss = {
                analyticsManager.logEvent("settings_dialog_dismissed", android.os.Bundle().apply {
                    putString("dialog_type", "third_party_libraries")
                })
                showLibrariesDialog = false
            },
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
            onDismiss = {
                analyticsManager.logEvent("settings_dialog_dismissed", android.os.Bundle().apply {
                    putString("dialog_type", "currency_symbol")
                })
                showCurrencyDialog = false
            },
        )
    }

    if (showDecimalDigitsDialog) {
        DecimalDigitsDialog(
            currentDigits = decimalDigits,
            onDigitsSelected = { onDecimalDigitsSelected(it); showDecimalDigitsDialog = false },
            onDismiss = {
                analyticsManager.logEvent("settings_dialog_dismissed", android.os.Bundle().apply {
                    putString("dialog_type", "decimal_digits")
                })
                showDecimalDigitsDialog = false
            },
        )
    }

    if (showDecimalSeparatorDialog) {
        SeparatorDialog(
            title = stringResource(R.string.dialog_choose_decimal_separator),
            options = translateSeparatorOptions(CurrencyFormat.DECIMAL_SEPARATORS.filter { it.first != thousandsSeparator }),
            currentValue = decimalSeparator,
            onSelected = { onDecimalSeparatorSelected(it); showDecimalSeparatorDialog = false },
            onDismiss = {
                analyticsManager.logEvent("settings_dialog_dismissed", android.os.Bundle().apply {
                    putString("dialog_type", "decimal_separator")
                })
                showDecimalSeparatorDialog = false
            },
        )
    }

    if (showThousandsSeparatorDialog) {
        SeparatorDialog(
            title = stringResource(R.string.dialog_choose_thousands_separator),
            options = translateSeparatorOptions(CurrencyFormat.THOUSANDS_SEPARATORS.filter { it.first != decimalSeparator }),
            currentValue = thousandsSeparator,
            onSelected = { onThousandsSeparatorSelected(it); showThousandsSeparatorDialog = false },
            onDismiss = {
                analyticsManager.logEvent("settings_dialog_dismissed", android.os.Bundle().apply {
                    putString("dialog_type", "thousands_separator")
                })
                showThousandsSeparatorDialog = false
            },
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
        value to when (label) {
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
