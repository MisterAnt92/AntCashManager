package com.antcashmanager.android.ui.screen.settingsData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel per la sotto-sezione Gestione Dati.
 */
class SettingsDataViewModel(
    settingsRepository: SettingsRepository,
    transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsDataViewModel"
    }

    private val deleteAllTransactionsUseCase = DeleteAllTransactionsUseCase(transactionRepository)
    private val backupService = BackupService(transactionRepository, categoryRepository)
    private val settingsRepositoryRef = settingsRepository

    private val _state = MutableStateFlow(SettingsDataState())
    val state: StateFlow<SettingsDataState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepositoryRef.getDataEncryptionEnabled().collect { enabled ->
                _state.update { it.copy(dataEncryptionEnabled = enabled) }
            }
        }
    }

    fun setDataEncryptionEnabled(enabled: Boolean) {
        Logger.d(TAG) { "Setting data encryption enabled: $enabled" }
        viewModelScope.launch {
            settingsRepositoryRef.setDataEncryptionEnabled(enabled)
        }
    }

    fun showDeleteConfirmDialog() {
        _state.update { it.copy(showDeleteConfirmDialog = true) }
    }

    fun dismissDeleteConfirmDialog() {
        _state.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun dismissDeleteSuccessDialog() {
        _state.update {
            it.copy(
                showDeleteSuccessDialog = false,
                deleteResult = DeleteResult.Idle,
            )
        }
    }

    fun showResetPreferencesDialog() {
        _state.update { it.copy(showResetPreferencesDialog = true) }
    }

    fun dismissResetPreferencesDialog() {
        _state.update { it.copy(showResetPreferencesDialog = false) }
    }

    fun dismissBackupSuccessDialog() {
        _state.update {
            it.copy(
                showBackupSuccessDialog = false,
                backupResult = BackupResult.Idle,
            )
        }
    }

    fun dismissBackupErrorDialog() {
        _state.update {
            it.copy(
                showBackupErrorDialog = false,
                backupResult = BackupResult.Idle,
                backupErrorMessage = "",
            )
        }
    }

    fun dismissRestoreSuccessDialog() {
        _state.update {
            it.copy(
                showRestoreSuccessDialog = false,
                restoreResult = RestoreOperationResult.Idle,
                restoreSuccessInfo = null,
            )
        }
    }

    fun dismissRestoreErrorDialog() {
        _state.update {
            it.copy(
                showRestoreErrorDialog = false,
                restoreResult = RestoreOperationResult.Idle,
                restoreErrorMessage = "",
            )
        }
    }

    fun onBackupFileSaved() {
        _state.update {
            it.copy(
                pendingBackupData = null,
                pendingBackupFileName = null,
                showBackupSuccessDialog = true,
            )
        }
    }

    fun clearPendingBackupRequest() {
        _state.update {
            it.copy(
                pendingBackupData = null,
                pendingBackupFileName = null,
            )
        }
    }

    fun onBackupFileSaveError(message: String) {
        _state.update {
            it.copy(
                pendingBackupData = null,
                pendingBackupFileName = null,
                backupResult = BackupResult.Error(message),
                backupErrorMessage = message,
                showBackupErrorDialog = true,
            )
        }
    }

    fun onRestoreFileReadError(message: String) {
        _state.update {
            it.copy(
                restoreResult = RestoreOperationResult.Error(message),
                restoreErrorMessage = message,
                showRestoreErrorDialog = true,
            )
        }
    }

    fun deleteAllData() {
        Logger.d(TAG) { "Deleting all data" }
        viewModelScope.launch {
            try {
                deleteAllTransactionsUseCase()
                    .onSuccess {
                        categoryRepository.deleteAllCategories()
                        _state.update {
                            it.copy(
                                deleteResult = DeleteResult.Success,
                                showDeleteConfirmDialog = false,
                                showDeleteSuccessDialog = true,
                            )
                        }
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        Logger.e(TAG, error) { "Error deleting data: ${error.message}" }
                        _state.update {
                            it.copy(
                                deleteResult = DeleteResult.Error(error.message ?: "Unknown error"),
                                showDeleteConfirmDialog = false,
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Logger.e(TAG, error) { "Error deleting data: ${error.message}" }
                _state.update {
                    it.copy(
                        deleteResult = DeleteResult.Error(error.message ?: "Unknown error"),
                        showDeleteConfirmDialog = false,
                    )
                }
            }
        }
    }

    fun createBackup() {
        Logger.d(TAG) { "Creating backup" }
        _state.update { it.copy(backupResult = BackupResult.Loading) }
        viewModelScope.launch {
            backupService.createBackup()
                .onSuccess { jsonString ->
                    val payloadToPersist = if (_state.value.dataEncryptionEnabled) {
                        runCatching { BackupPayloadCipher.encrypt(jsonString) }
                            .getOrElse { error ->
                                val message = error.message ?: "Unknown error"
                                _state.update {
                                    it.copy(
                                        backupResult = BackupResult.Error(message),
                                        backupErrorMessage = message,
                                        showBackupErrorDialog = true,
                                    )
                                }
                                return@onSuccess
                            }
                    } else {
                        jsonString
                    }

                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(Date())
                    _state.update {
                        it.copy(
                            backupResult = BackupResult.Success,
                            pendingBackupData = payloadToPersist,
                            pendingBackupFileName = "antcashmanager_backup_$timestamp.json",
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val message = error.message ?: "Unknown error"
                    _state.update {
                        it.copy(
                            backupResult = BackupResult.Error(message),
                            backupErrorMessage = message,
                            showBackupErrorDialog = true,
                        )
                    }
                }
        }
    }

    fun restoreBackup(jsonString: String) {
        Logger.d(TAG) { "Restoring backup" }
        _state.update { it.copy(restoreResult = RestoreOperationResult.Loading) }

        val payloadToRestore = if (BackupPayloadCipher.isEncryptedPayload(jsonString)) {
            runCatching { BackupPayloadCipher.decrypt(jsonString) }
                .getOrElse { error ->
                    val message = error.message ?: "Unknown error"
                    _state.update {
                        it.copy(
                            restoreResult = RestoreOperationResult.Error(message),
                            restoreErrorMessage = message,
                            showRestoreErrorDialog = true,
                        )
                    }
                    return
                }
        } else {
            jsonString
        }

        viewModelScope.launch {
            backupService.restoreBackup(payloadToRestore)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            restoreResult = RestoreOperationResult.Success(
                                transactions = result.transactionsRestored,
                                categories = result.categoriesRestored,
                            ),
                            restoreSuccessInfo = RestoreSuccessInfo(
                                transactions = result.transactionsRestored,
                                categories = result.categoriesRestored,
                            ),
                            showRestoreSuccessDialog = true,
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val message = error.message ?: "Unknown error"
                    _state.update {
                        it.copy(
                            restoreResult = RestoreOperationResult.Error(message),
                            restoreErrorMessage = message,
                            showRestoreErrorDialog = true,
                        )
                    }
                }
        }
    }

    fun resetAllPreferences() {
        Logger.d(TAG) { "Resetting all preferences" }
        viewModelScope.launch {
            settingsRepositoryRef.resetAllPreferences()
            _state.update { it.copy(showResetPreferencesDialog = false) }
        }
    }
}

