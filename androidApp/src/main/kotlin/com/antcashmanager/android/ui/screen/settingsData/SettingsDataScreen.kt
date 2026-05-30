package com.antcashmanager.android.ui.screen.settingsData

import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
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
import com.antcashmanager.android.ui.components.AppSwitch
import com.antcashmanager.android.ui.components.card.AppCard
import com.antcashmanager.android.ui.components.card.AppCardSectionHeader
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.nio.charset.StandardCharsets

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
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val registryOwner = LocalActivityResultRegistryOwner.current
    val hasActivityHost = generateSequence(context) { current ->
        (current as? ContextWrapper)?.baseContext
    }.any { it is Activity }
    val filePickerUnavailableMessage = stringResource(R.string.settings_data_file_picker_unavailable)
    val analyticsManager: AnalyticsManager = koinInject()
    val backupLauncher =
        if (isPreview || !hasActivityHost || registryOwner == null) null else rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            if (uri == null) {
                onClearPendingBackupRequest()
                return@rememberLauncherForActivityResult
            }

            val jsonData = state.pendingBackupData
            if (jsonData.isNullOrBlank()) {
                onBackupFileSaveError(SettingsDataConstants.UNKNOWN_ERROR)
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
                onBackupFileSaveError(error.message ?: SettingsDataConstants.UNKNOWN_ERROR)
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

    val restoreLauncher =
        if (isPreview || !hasActivityHost || registryOwner == null) null else rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            analyticsManager.logEvent("restore_file_selected")

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Unable to open selected backup file")

                val payload = inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                    reader.readText()
                }

                if (payload.isBlank()) {
                    throw IllegalArgumentException("Selected backup file is empty")
                }

                onRestoreBackup(payload)
            } catch (error: Exception) {
                onRestoreFileReadError(error.message ?: SettingsDataConstants.UNKNOWN_ERROR)
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
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                AppCardSectionHeader(title = stringResource(R.string.settings_data_management))
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppCard(
                        title = stringResource(R.string.settings_backup),
                        subtitle = stringResource(R.string.settings_backup_subtitle),
                        leadingIcon = Icons.Default.Backup,
                        iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = {
                            analyticsManager.logEvent("backup_create_requested")
                            onCreateBackup()
                        },
                    )
                    AppCard(
                        title = stringResource(R.string.settings_restore),
                        subtitle = stringResource(R.string.settings_restore_subtitle),
                        leadingIcon = Icons.Default.RestorePage,
                        iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = {
                            if (restoreLauncher != null) {
                                analyticsManager.logEvent("restore_open_requested")
                                restoreLauncher.launch(
                                    arrayOf("application/json", "text/json", "text/plain"),
                                )
                            } else {
                                onRestoreFileReadError(filePickerUnavailableMessage)
                            }
                        },
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

            item {
                AppCardSectionHeader(title = stringResource(R.string.settings_security))
                Spacer(modifier = Modifier.height(8.dp))
                AppCard(
                    title = stringResource(R.string.settings_security_data_encryption),
                    subtitle = stringResource(R.string.settings_security_data_encryption_subtitle),
                    leadingIcon = Icons.Default.Refresh,
                    trailingContent = {
                        AppSwitch(
                            checked = state.dataEncryptionEnabled,
                            onCheckedChange = onDataEncryptionEnabledChange,
                        )
                    },
                    onClick = { onDataEncryptionEnabledChange(!state.dataEncryptionEnabled) },
                )
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
                    AppText(stringResource(R.string.dialog_cancel))
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
                    AppText(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDataContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        SettingsDataContent()
    }
}
