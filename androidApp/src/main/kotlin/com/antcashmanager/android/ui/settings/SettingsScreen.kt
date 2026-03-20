package com.antcashmanager.android.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
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

    SettingsContent(
        currentTheme = currentTheme,
        onThemeSelected = { viewModel.setTheme(it) },
        currentLanguage = currentLanguage,
        onLanguageSelected = { viewModel.setLanguage(it) },
        versionName = BuildConfig.VERSION_NAME,
        onDeleteAllData = { viewModel.deleteAllData() },
        deleteResult = deleteResult,
        onResetDeleteResult = { viewModel.resetDeleteResult() },
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
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteSuccessDialog by remember { mutableStateOf(false) }
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
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
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
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback@antcashmanager.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "AntCashManager Feedback - v$versionName")
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
        }

        // ── About Section ──
        AppCardSectionHeader(title = stringResource(R.string.settings_about))
        AppCard(
            title = stringResource(R.string.settings_app_version),
            subtitle = versionName,
            leadingIcon = Icons.Default.Info,
            showChevron = false,
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
                Text(
                    stringResource(R.string.dialog_delete_all_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
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
