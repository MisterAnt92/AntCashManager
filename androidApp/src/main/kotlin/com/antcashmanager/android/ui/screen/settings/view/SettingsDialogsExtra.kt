package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.HelpDialogContent
import com.antcashmanager.android.ui.components.SimpleHelpFeature
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(stringResource(R.string.dialog_choose_theme)) },
        text = {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                        AppText(
                            text = stringResource(
                                when (theme) {
                                    AppTheme.LIGHT -> R.string.settings_theme_light
                                    AppTheme.DARK -> R.string.settings_theme_dark
                                    AppTheme.SYSTEM -> R.string.settings_theme_system
                                }
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_close))
            }
        },
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(stringResource(R.string.dialog_choose_language)) },
        text = {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                AppLanguage.entries.forEach { language ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                    ) {
                        RadioButton(
                            selected = currentLanguage == language,
                            onClick = { onLanguageSelected(language) }
                        )
                        AppText(
                            text = stringResource(
                                when (language) {
                                    AppLanguage.SYSTEM -> R.string.language_system
                                    AppLanguage.ENGLISH -> R.string.language_english
                                    AppLanguage.ITALIAN -> R.string.language_italian
                                    AppLanguage.FRENCH -> R.string.language_french
                                    AppLanguage.GERMAN -> R.string.language_german
                                    AppLanguage.SPANISH -> R.string.language_spanish
                                }
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_close))
            }
        },
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(stringResource(R.string.privacy_policy_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AppText(
                    text = stringResource(R.string.privacy_policy_content),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_close))
            }
        },
    )
}

@Composable
fun ThirdPartyLibrariesDialog(onDismiss: () -> Unit) {
    val libraries = listOf(
        "Jetpack Compose" to "Google",
        "Room" to "Google",
        "Coroutines" to "JetBrains",
        "Hilt" to "Google",
        "Timber" to "Jake Wharton",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(stringResource(R.string.settings_third_party_libraries)) },
        text = {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                libraries.forEach { (name, author) ->
                    Column {
                        AppText(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        AppText(
                            text = stringResource(R.string.settings_third_party_author, author),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_close))
            }
        },
    )
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        SimpleHelpFeature(
            title = "Tema Scuro/Chiaro",
            description = "Personalizza l'aspetto dell'app secondo le tue preferenze",
            icon = Icons.Default.Settings,
        ),
        SimpleHelpFeature(
            title = "Valuta",
            description = "Seleziona la valuta preferita per visualizzare gli importi",
            icon = Icons.Default.Settings,
        ),
        SimpleHelpFeature(
            title = "Lingua",
            description = "Scegli la lingua dell'interfaccia",
            icon = Icons.Default.Settings,
        ),
    )

    HelpDialogContent(
        isVisible = true,
        title = "Guida Impostazioni",
        description = "Personalizza l'app secondo le tue preferenze!",
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}

