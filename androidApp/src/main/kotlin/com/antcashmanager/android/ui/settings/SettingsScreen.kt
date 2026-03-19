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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.ui.components.AppCard
import com.antcashmanager.android.ui.components.AppCardSectionHeader
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
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
    val deleteResult by viewModel.deleteResult.collectAsState()

    SettingsContent(
        currentTheme = currentTheme,
        onThemeSelected = { viewModel.setTheme(it) },
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
    versionName: String,
    onDeleteAllData: () -> Unit = {},
    deleteResult: DeleteResult = DeleteResult.Idle,
    onResetDeleteResult: () -> Unit = {},
) {
    var showThemeDialog by remember { mutableStateOf(false) }
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
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // ── Appearance Section ──
        AppCardSectionHeader(title = "Appearance")
        AppCard(
            title = "Theme",
            subtitle = when (currentTheme) {
                AppTheme.LIGHT -> "Light"
                AppTheme.DARK -> "Dark"
                AppTheme.SYSTEM -> "System Default"
            },
            leadingIcon = Icons.Default.Palette,
            onClick = { showThemeDialog = true },
        )

        // ── Data Management Section ──
        AppCardSectionHeader(title = "Data Management")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = "Delete All Data",
                subtitle = "Permanently remove all transactions and categories",
                leadingIcon = Icons.Default.Delete,
                iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
                showChevron = false,
                onClick = { showDeleteConfirmDialog = true },
            )
            AppCard(
                title = "Backup Data",
                subtitle = "Export your data for safekeeping",
                leadingIcon = Icons.Default.Backup,
                iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = {
                    // TODO: Implement backup functionality
                    Logger.d("Settings") { "Backup data tapped" }
                },
            )
            AppCard(
                title = "Restore Data",
                subtitle = "Import data from a backup file",
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
        AppCardSectionHeader(title = "Support")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = "Send Feedback",
                subtitle = "Help us improve the app",
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
                title = "Privacy Policy",
                subtitle = "How we handle your data",
                leadingIcon = Icons.Default.PrivacyTip,
                iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { showPrivacyDialog = true },
            )
        }

        // ── About Section ──
        AppCardSectionHeader(title = "About")
        AppCard(
            title = "App Version",
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
            title = { Text("Delete All Data?") },
            text = {
                Text(
                    "This action will permanently delete all transactions and categories. " +
                        "This cannot be undone.",
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
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showDeleteSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSuccessDialog = false },
            title = { Text("Data Deleted") },
            text = { Text("All data has been successfully deleted.") },
            confirmButton = {
                TextButton(onClick = { showDeleteSuccessDialog = false }) {
                    Text("OK")
                }
            },
        )
    }
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
        title = { Text("Choose Theme") },
        text = {
            Column {
                AppTheme.entries.forEach { theme ->
                    ListItem(
                        headlineContent = {
                            Text(
                                when (theme) {
                                    AppTheme.LIGHT -> "Light"
                                    AppTheme.DARK -> "Dark"
                                    AppTheme.SYSTEM -> "System Default"
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
        title = { Text("Privacy Policy") },
        text = {
            Text(
                text = "AntCashManager is committed to protecting your privacy. " +
                    "This application does not collect or share personal data with third parties. " +
                    "All financial data is stored locally on your device and is never transmitted " +
                    "to external servers.\n\n" +
                    "Your data remains entirely on your device and under your control. " +
                    "We do not use analytics, tracking, or advertising services.\n\n" +
                    "For any questions about privacy, please contact us through the app store listing.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
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
