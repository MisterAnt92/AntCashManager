package com.antcashmanager.android.ui.screen.settings.dataManagement

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.card.AppCard
import com.antcashmanager.android.ui.components.card.AppCardSectionHeader
import com.antcashmanager.android.ui.components.common.AppSwitch
import com.antcashmanager.android.ui.components.dialog.BlockingProgressDialog
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDataScreen(
    navController: NavController,
) {
    val viewModel: SettingsDataViewModel = koinViewModel()

    val state by viewModel.state.collectAsState()

    SettingsDataContent(
        state = state,
        onDeleteAllData = viewModel::deleteAllData,
        onCreateBackup = viewModel::createBackup,
        onRestoreBackup = viewModel::restoreBackup,
        onResetAllPreferences = viewModel::resetAllPreferences,
        onShowDeleteConfirmDialog = viewModel::showDeleteConfirmDialog,
        onDismissDeleteConfirmDialog = viewModel::dismissDeleteConfirmDialog,
        onDismissDeleteSuccessDialog = viewModel::dismissDeleteSuccessDialog,
        onShowResetPreferencesDialog = viewModel::showResetPreferencesDialog,
        onDismissResetPreferencesDialog = viewModel::dismissResetPreferencesDialog,
        onDismissBackupSuccessDialog = viewModel::dismissBackupSuccessDialog,
        onDismissBackupErrorDialog = viewModel::dismissBackupErrorDialog,
        onDismissRestoreSuccessDialog = viewModel::dismissRestoreSuccessDialog,
        onDismissRestoreErrorDialog = viewModel::dismissRestoreErrorDialog,
        onBackupFileSaved = viewModel::onBackupFileSaved,
        onBackupFileSaveError = viewModel::onBackupFileSaveError,
        onRestoreFileReadError = viewModel::onRestoreFileReadError,
        onClearPendingBackupRequest = viewModel::clearPendingBackupRequest,
        onDataEncryptionEnabledChange = viewModel::setDataEncryptionEnabled,
        onSuggestionsEnabledChange = viewModel::setSuggestionsEnabled,
        onShowDeleteSuggestionsDialog = viewModel::showDeleteSuggestionsDialog,
        onDismissDeleteSuggestionsDialog = viewModel::dismissDeleteSuggestionsDialog,
        onDeleteAllSuggestions = viewModel::deleteAllSuggestions,
        onNavigateBack = { navController.popBackStack() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsDataContent(
    state: SettingsDataState = SettingsDataState(),
    onDeleteAllData: () -> Unit = {},
    onCreateBackup: () -> Unit = {},
    onRestoreBackup: (String) -> Unit = {},
    onResetAllPreferences: () -> Unit = {},
    onShowDeleteConfirmDialog: () -> Unit = {},
    onDismissDeleteConfirmDialog: () -> Unit = {},
    onDismissDeleteSuccessDialog: () -> Unit = {},
    onShowResetPreferencesDialog: () -> Unit = {},
    onDismissResetPreferencesDialog: () -> Unit = {},
    onDismissBackupSuccessDialog: () -> Unit = {},
    onDismissBackupErrorDialog: () -> Unit = {},
    onDismissRestoreSuccessDialog: () -> Unit = {},
    onDismissRestoreErrorDialog: () -> Unit = {},
    onBackupFileSaved: () -> Unit = {},
    onBackupFileSaveError: (String) -> Unit = {},
    onRestoreFileReadError: (String) -> Unit = {},
    onClearPendingBackupRequest: () -> Unit = {},
    onDataEncryptionEnabledChange: (Boolean) -> Unit = {},
    onSuggestionsEnabledChange: (Boolean) -> Unit = {},
    onShowDeleteSuggestionsDialog: () -> Unit = {},
    onDismissDeleteSuggestionsDialog: () -> Unit = {},
    onDeleteAllSuggestions: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val filePickerUnavailableMessage =
        stringResource(R.string.settings_data_file_picker_unavailable)
    val analyticsManager: AnalyticsManager = koinInject()
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()
    val handleDataEncryptionEnabledChange: (Boolean) -> Unit = { enabled ->
        analyticsManager.logEvent("data_encryption_toggled")
        onDataEncryptionEnabledChange(enabled)
    }
    val handleSuggestionsEnabledChange: (Boolean) -> Unit = { enabled ->
        analyticsManager.logEvent("suggestions_toggled")
        onSuggestionsEnabledChange(enabled)
    }
    val backupLauncher =
        if (isPreview) null else rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            if (uri == null) {
                onClearPendingBackupRequest()
                return@rememberLauncherForActivityResult
            }

            val jsonData = state.pendingBackupData
            if (jsonData.isNullOrBlank()) {
                onBackupFileSaveError(SettingsDataConstant.UNKNOWN_ERROR)
                return@rememberLauncherForActivityResult
            }

            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IllegalStateException("Unable to open destination file")

                outputStream.use { stream ->
                    stream.write(jsonData.toByteArray(StandardCharsets.UTF_8))
                    stream.flush()
                }

                analyticsManager.logEvent("backup_file_saved")
                onBackupFileSaved()
            } catch (error: Exception) {
                analyticsManager.logEvent("backup_file_save_error")
                onBackupFileSaveError(error.message ?: SettingsDataConstant.UNKNOWN_ERROR)
            }
        }

    LaunchedEffect(state.pendingBackupFileName) {
        state.pendingBackupFileName?.let { fileName ->
            if (backupLauncher != null) {
                backupLauncher.launch(fileName)
            } else {
                onBackupFileSaveError(filePickerUnavailableMessage)
            }
        }
    }

    // Helper function to read backup file with charset fallback
    fun readBackupFileContent(inputStream: java.io.InputStream, fileName: String?): String? {
        return try {
            // Attempt 1: UTF-8 (standard)
            inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            // Attempt 2: ISO-8859-1 (fallback for legacy files)
            try {
                inputStream.bufferedReader(StandardCharsets.ISO_8859_1).use { reader ->
                    reader.readText()
                }
            } catch (iso8859e: Exception) {
                // Attempt 3: System default charset (last resort)
                try {
                    inputStream.bufferedReader().use { reader ->
                        reader.readText()
                    }
                } catch (fallbackError: Exception) {
                    analyticsManager.logEvent("backup_file_read_charset_error")
                    onRestoreFileReadError(
                        "Impossibile leggere il file: ${fallbackError.message ?: "Charset error"}"
                    )
                    null
                }
            }
        }
    }

    val restoreLauncher =
        if (isPreview) null else rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            analyticsManager.logEvent("restore_file_selected")

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Unable to open selected backup file")

                // ✅ Use new function with charset fallback
                val payload = readBackupFileContent(inputStream, uri.lastPathSegment)
                    ?: return@rememberLauncherForActivityResult

                if (payload.isBlank()) {
                    throw IllegalArgumentException("Selected backup file is empty")
                }

                onRestoreBackup(payload)
            } catch (error: Exception) {
                analyticsManager.logEvent("backup_file_read_error")
                onRestoreFileReadError(error.message ?: SettingsDataConstant.UNKNOWN_ERROR)
            }
        }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { AppText(stringResource(R.string.settings_data_management)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (adaptiveLayoutInfo.isCompact) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) +
                            SettingsDataConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                    top = innerPadding.calculateTopPadding() + SettingsDataConstant.CONTENT_TOP_PADDING_DP.dp,
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr) +
                            SettingsDataConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                    bottom = innerPadding.calculateBottomPadding() + SettingsDataConstant.CONTENT_BOTTOM_PADDING_DP.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(SettingsDataConstant.CARD_SPACING_DP.dp),
            ) {
                item {
                    DataManagementSection(
                        lastBackupTimestamp = state.lastBackupTimestamp,
                        lastRestoreTimestamp = state.lastRestoreTimestamp,
                        onCreateBackup = {
                            analyticsManager.logEvent("backup_create_requested")
                            onCreateBackup()
                        },
                        onRestoreBackup = {
                            if (restoreLauncher != null) {
                                analyticsManager.logEvent("restore_open_requested")
                                restoreLauncher.launch(
                                    arrayOf(
                                        "application/json",      // Standard JSON
                                        "application/backup",    // Custom MIME type for .backup
                                        "text/plain",           // Generic text file
                                        "text/json",            // JSON as text
                                        "*/*",                  // Fallback: accept any file type
                                    ),
                                )
                            } else {
                                onRestoreFileReadError(filePickerUnavailableMessage)
                            }
                        },
                        onShowResetPreferencesDialog = onShowResetPreferencesDialog,
                        onShowDeleteConfirmDialog = onShowDeleteConfirmDialog,
                    )
                }

                item {
                    SecuritySection(
                        dataEncryptionEnabled = state.dataEncryptionEnabled,
                        onDataEncryptionEnabledChange = handleDataEncryptionEnabledChange,
                    )
                }

                item {
                    SuggestionsSection(
                        suggestionsEnabled = state.suggestionsEnabled,
                        onSuggestionsEnabledChange = handleSuggestionsEnabledChange,
                        onShowDeleteSuggestionsDialog = onShowDeleteSuggestionsDialog,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) +
                                SettingsDataConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                        top = innerPadding.calculateTopPadding() + SettingsDataConstant.CONTENT_TOP_PADDING_DP.dp,
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr) +
                                SettingsDataConstant.CONTENT_HORIZONTAL_PADDING_DP.dp,
                        bottom = innerPadding.calculateBottomPadding() +
                                SettingsDataConstant.CONTENT_BOTTOM_PADDING_DP.dp,
                    )
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SettingsDataConstant.TABLET_COLUMNS_SPACING_DP.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SettingsDataConstant.CARD_SPACING_DP.dp),
                ) {
                    DataManagementSection(
                        lastBackupTimestamp = state.lastBackupTimestamp,
                        lastRestoreTimestamp = state.lastRestoreTimestamp,
                        onCreateBackup = {
                            analyticsManager.logEvent("backup_create_requested")
                            onCreateBackup()
                        },
                        onRestoreBackup = {
                            if (restoreLauncher != null) {
                                analyticsManager.logEvent("restore_open_requested")
                                restoreLauncher.launch(
                                    arrayOf(
                                        "application/json",      // Standard JSON
                                        "application/backup",    // Custom MIME type for .backup
                                        "text/plain",           // Generic text file
                                        "text/json",            // JSON as text
                                        "*/*",                  // Fallback: accept any file type
                                    ),
                                )
                            } else {
                                onRestoreFileReadError(filePickerUnavailableMessage)
                            }
                        },
                        onShowResetPreferencesDialog = onShowResetPreferencesDialog,
                        onShowDeleteConfirmDialog = onShowDeleteConfirmDialog,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SettingsDataConstant.CARD_SPACING_DP.dp),
                ) {
                    SecuritySection(
                        dataEncryptionEnabled = state.dataEncryptionEnabled,
                        onDataEncryptionEnabledChange = handleDataEncryptionEnabledChange,
                    )
                    SuggestionsSection(
                        suggestionsEnabled = state.suggestionsEnabled,
                        onSuggestionsEnabledChange = handleSuggestionsEnabledChange,
                        onShowDeleteSuggestionsDialog = onShowDeleteSuggestionsDialog,
                    )
                }
            }
        }
    }

    if (state.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteConfirmDialog,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { AppText(stringResource(R.string.dialog_delete_all_title)) },
            text = { AppText(stringResource(R.string.dialog_delete_all_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        analyticsManager.logEvent("delete_all_data_confirmed")
                        onDeleteAllData()
                    },
                ) {
                    AppText(
                        stringResource(R.string.dialog_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteConfirmDialog) {
                    AppText(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (state.showDeleteSuccessDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteSuccessDialog,
            title = { AppText(stringResource(R.string.dialog_data_deleted)) },
            text = { AppText(stringResource(R.string.dialog_data_deleted_message)) },
            confirmButton = {
                TextButton(onClick = onDismissDeleteSuccessDialog) {
                    AppText(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    if (state.showBackupSuccessDialog) {
        AlertDialog(
            onDismissRequest = onDismissBackupSuccessDialog,
            title = { AppText(stringResource(R.string.backup_success_title)) },
            text = { AppText(stringResource(R.string.backup_success_message)) },
            confirmButton = {
                TextButton(onClick = onDismissBackupSuccessDialog) {
                    AppText(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    if (state.showBackupErrorDialog) {
        AlertDialog(
            onDismissRequest = onDismissBackupErrorDialog,
            title = { AppText(stringResource(R.string.backup_error_title)) },
            text = {
                AppText(
                    stringResource(
                        R.string.backup_error_message,
                        state.backupErrorMessage,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissBackupErrorDialog) {
                    AppText(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    if (state.showRestoreSuccessDialog) {
        AlertDialog(
            onDismissRequest = onDismissRestoreSuccessDialog,
            title = { AppText(stringResource(R.string.restore_success_title)) },
            text = {
                state.restoreSuccessInfo?.let { info ->
                    AppText(
                        stringResource(
                            R.string.restore_success_message,
                            info.transactions,
                            info.categories,
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRestoreSuccessDialog) {
                    AppText(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    if (state.showRestoreErrorDialog) {
        AlertDialog(
            onDismissRequest = onDismissRestoreErrorDialog,
            title = { AppText(stringResource(R.string.restore_error_title)) },
            text = {
                AppText(
                    stringResource(
                        R.string.restore_error_message,
                        state.restoreErrorMessage,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissRestoreErrorDialog) {
                    AppText(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    if (state.showResetPreferencesDialog) {
        AlertDialog(
            onDismissRequest = onDismissResetPreferencesDialog,
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
                    analyticsManager.logEvent("reset_preferences_confirmed")
                    onResetAllPreferences()
                }) {
                    AppText(
                        stringResource(R.string.dialog_reset),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissResetPreferencesDialog) {
                    AppText(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (state.showDeleteSuggestionsDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteSuggestionsDialog,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { AppText(stringResource(R.string.dialog_delete_suggestions_title)) },
            text = {
                AppText(
                    stringResource(R.string.dialog_delete_suggestions_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    analyticsManager.logEvent("delete_suggestions_confirmed")
                    onDeleteAllSuggestions()
                }) {
                    AppText(
                        stringResource(R.string.dialog_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteSuggestionsDialog) {
                    AppText(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (state.backupResult is BackupResult.Loading) {
        BlockingProgressDialog(
            message = stringResource(R.string.backup_in_progress_message),
            icon = Icons.Default.Backup,
        )
    }

    if (state.restoreResult is RestoreOperationResult.Loading) {
        BlockingProgressDialog(
            message = stringResource(R.string.restore_in_progress_message),
            icon = Icons.Default.RestorePage,
        )
    }
}

@Composable
private fun DataManagementSection(
    lastBackupTimestamp: Long?,
    lastRestoreTimestamp: Long?,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onShowResetPreferencesDialog: () -> Unit,
    onShowDeleteConfirmDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_data_management))
    VerticalSpacer(SpacingSize.XS)
    Column(verticalArrangement = Arrangement.spacedBy(SettingsDataConstant.CARD_SPACING_DP.dp)) {
        AppCard(
            title = stringResource(R.string.settings_backup),
            subtitle = stringResource(R.string.settings_backup_subtitle),
            leadingIcon = Icons.Default.Backup,
            iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onCreateBackup,
        )
        LastOperationLabel(
            timestamp = lastBackupTimestamp,
            labelRes = R.string.settings_last_backup_label,
            neverRes = R.string.settings_last_backup_never,
        )
        AppCard(
            title = stringResource(R.string.settings_restore),
            subtitle = stringResource(R.string.settings_restore_subtitle),
            leadingIcon = Icons.Default.RestorePage,
            iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onRestoreBackup,
        )
        LastOperationLabel(
            timestamp = lastRestoreTimestamp,
            labelRes = R.string.settings_last_restore_label,
            neverRes = R.string.settings_last_restore_never,
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
        AppCard(
            title = stringResource(R.string.settings_delete_all),
            subtitle = stringResource(R.string.settings_delete_all_subtitle),
            leadingIcon = Icons.Default.Delete,
            iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
            iconTint = MaterialTheme.colorScheme.onErrorContainer,
            showChevron = false,
            onClick = onShowDeleteConfirmDialog,
        )
    }
}

/**
 * Piccola label mostrata sotto le card di backup/ripristino con la data dell'ultima
 * operazione effettuata su questo device, o un messaggio "mai effettuato" se [timestamp] è null.
 */
@Composable
private fun LastOperationLabel(
    timestamp: Long?,
    @StringRes labelRes: Int,
    @StringRes neverRes: Int,
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val text = if (timestamp != null) {
        stringResource(labelRes, dateFormat.format(Date(timestamp)))
    } else {
        stringResource(neverRes)
    }
    AppText(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp),
    )
}

@Composable
private fun SecuritySection(
    dataEncryptionEnabled: Boolean,
    onDataEncryptionEnabledChange: (Boolean) -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_security))
    VerticalSpacer(SpacingSize.XS)
    AppCard(
        title = stringResource(R.string.settings_security_data_encryption),
        subtitle = stringResource(R.string.settings_security_data_encryption_subtitle),
        leadingIcon = Icons.Default.Refresh,
        trailingContent = {
            AppSwitch(
                checked = dataEncryptionEnabled,
                onCheckedChange = onDataEncryptionEnabledChange,
            )
        },
        onClick = { onDataEncryptionEnabledChange(!dataEncryptionEnabled) },
    )
}

@Composable
private fun SuggestionsSection(
    suggestionsEnabled: Boolean,
    onSuggestionsEnabledChange: (Boolean) -> Unit,
    onShowDeleteSuggestionsDialog: () -> Unit,
) {
    AppCardSectionHeader(title = stringResource(R.string.settings_suggestions))
    VerticalSpacer(SpacingSize.XS)
    Column(verticalArrangement = Arrangement.spacedBy(SettingsDataConstant.CARD_SPACING_DP.dp)) {
        AppCard(
            title = stringResource(R.string.settings_suggestions_enable),
            subtitle = stringResource(R.string.settings_suggestions_enable_subtitle),
            leadingIcon = Icons.Default.Lightbulb,
            trailingContent = {
                AppSwitch(
                    checked = suggestionsEnabled,
                    onCheckedChange = onSuggestionsEnabledChange,
                )
            },
            onClick = { onSuggestionsEnabledChange(!suggestionsEnabled) },
        )
        AppCard(
            title = stringResource(R.string.settings_suggestions_delete_all),
            subtitle = stringResource(R.string.settings_suggestions_delete_all_subtitle),
            leadingIcon = Icons.Default.DeleteSweep,
            iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
            iconTint = MaterialTheme.colorScheme.onErrorContainer,
            showChevron = false,
            onClick = onShowDeleteSuggestionsDialog,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, name = "SettingsDataScreen - 7 inch", widthDp = 600, heightDp = 960)
@Preview(
    showBackground = true,
    name = "SettingsDataScreen - 10 inch",
    widthDp = 840,
    heightDp = 1280
)
@Composable
private fun SettingsDataContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        SettingsDataContent()
    }
}
