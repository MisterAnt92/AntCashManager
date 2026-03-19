package com.antcashmanager.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository

@Composable
fun SettingsScreen(settingsRepository: SettingsRepository) {
    Logger.d("SettingsScreen") { "Displaying SettingsScreen" }
    val viewModel: SettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(settingsRepository) as T
        },
    )
    val currentTheme by viewModel.theme.collectAsState()

    SettingsContent(
        currentTheme = currentTheme,
        onThemeSelected = { viewModel.setTheme(it) },
        versionName = BuildConfig.VERSION_NAME,
    )
}

@Composable
internal fun SettingsContent(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    versionName: String,
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Theme Setting
        ListItem(
            headlineContent = { Text("Theme") },
            supportingContent = {
                Text(
                    when (currentTheme) {
                        AppTheme.LIGHT -> "Light"
                        AppTheme.DARK -> "Dark"
                        AppTheme.SYSTEM -> "System Default"
                    },
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showThemeDialog = true },
        )
        HorizontalDivider()

        // Privacy Policy
        ListItem(
            headlineContent = { Text("Privacy Policy") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPrivacyDialog = true },
        )
        HorizontalDivider()

        // App Version
        ListItem(
            headlineContent = { Text("App Version") },
            supportingContent = { Text(versionName) },
            modifier = Modifier.fillMaxWidth(),
        )
        HorizontalDivider()
    }

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
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                        modifier = Modifier.clickable { onThemeSelected(theme) },
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
        title = { Text("Privacy Policy") },
        text = {
            Text(
                text = "AntCashManager is committed to protecting your privacy. " +
                    "This application does not collect or share personal data with third parties. " +
                    "All financial data is stored locally on your device and is never transmitted to external servers.\n\n" +
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

@Preview(showBackground = true, name = "SettingsScreen - Light")
@Composable
private fun SettingsContentLightPreview() {
    AntCashManagerTheme {
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
    AntCashManagerTheme(darkTheme = true) {
        SettingsContent(
            currentTheme = AppTheme.DARK,
            onThemeSelected = {},
            versionName = "1.0.0",
        )
    }
}

@Preview(showBackground = true, name = "SettingsScreen - System Theme")
@Composable
private fun SettingsContentSystemPreview() {
    AntCashManagerTheme {
        SettingsContent(
            currentTheme = AppTheme.SYSTEM,
            onThemeSelected = {},
            versionName = "1.0.0",
        )
    }
}

@Preview(showBackground = true, name = "ThemeSelectionDialog")
@Composable
private fun ThemeSelectionDialogPreview() {
    AntCashManagerTheme {
        ThemeSelectionDialog(
            currentTheme = AppTheme.SYSTEM,
            onThemeSelected = {},
            onDismiss = {},
        )
    }
}
