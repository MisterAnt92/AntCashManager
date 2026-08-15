package com.antcashmanager.android.ui.screen.settings.dataManagement

import android.os.Build
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.domain.model.None
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
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
    private val categoryRepository: CategoryRepository,
    private val deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase,
    private val backupService: BackupService,
) : BaseViewModel<None>() {
    private val settingsRepositoryRef = settingsRepository

    private val _state = MutableStateFlow(SettingsDataState())
    val state: StateFlow<SettingsDataState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepositoryRef.getDataEncryptionEnabled().collect { enabled ->
                _state.update { it.copy(dataEncryptionEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getLastBackupTimestamp().collect { timestamp ->
                _state.update { it.copy(lastBackupTimestamp = timestamp) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getLastRestoreTimestamp().collect { timestamp ->
                _state.update { it.copy(lastRestoreTimestamp = timestamp) }
            }
        }
        viewModelScope.launch {
            settingsRepositoryRef.getSuggestionsEnabled().collect { enabled ->
                _state.update { it.copy(suggestionsEnabled = enabled) }
            }
        }
    }

    fun setDataEncryptionEnabled(enabled: Boolean) {
        logDebug("Setting data encryption enabled: $enabled")
        viewModelScope.launch {
            settingsRepositoryRef.setDataEncryptionEnabled(enabled)
        }
    }

    fun setSuggestionsEnabled(enabled: Boolean) {
        logDebug("Setting suggestions enabled: $enabled")
        viewModelScope.launch {
            settingsRepositoryRef.setSuggestionsEnabled(enabled)
        }
    }

    fun showDeleteSuggestionsDialog() {
        _state.update { it.copy(showDeleteSuggestionsDialog = true) }
    }

    fun dismissDeleteSuggestionsDialog() {
        _state.update { it.copy(showDeleteSuggestionsDialog = false) }
    }

    fun deleteAllSuggestions() {
        logDebug("Deleting all suggestions")
        viewModelScope.launch {
            settingsRepositoryRef.setSuggestionsClearedAt(System.currentTimeMillis())
            _state.update { it.copy(showDeleteSuggestionsDialog = false) }
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
        viewModelScope.launch {
            settingsRepositoryRef.setLastBackupTimestamp(System.currentTimeMillis())
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
        logDebug("Deleting all data")
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
                        logError("Error deleting data: ${error.message}", error)
                        _state.update {
                            it.copy(
                                deleteResult = DeleteResult.Error(
                                    error.message ?: SettingsDataConstant.UNKNOWN_ERROR,
                                ),
                                showDeleteConfirmDialog = false,
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logError("Error deleting data: ${error.message}", error)
                _state.update {
                    it.copy(
                        deleteResult = DeleteResult.Error(
                            error.message ?: SettingsDataConstant.UNKNOWN_ERROR,
                        ),
                        showDeleteConfirmDialog = false,
                    )
                }
            }
        }
    }

    /**
     * Garantisce che [block] resti "percepibile" almeno per [SettingsDataConstant.MIN_LOADING_DURATION_MS]:
     * se l'operazione reale è più veloce, attende la differenza prima di restituire il risultato,
     * cosicché la dialog di caricamento non lampeggi troppo velocemente per essere notata.
     */
    private suspend fun <T> withMinimumLoadingDuration(block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val remaining =
            SettingsDataConstant.MIN_LOADING_DURATION_MS - (System.currentTimeMillis() - startTime)
        if (remaining > 0) delay(remaining)
        return result
    }

    fun createBackup() {
        logDebug("Creating backup")
        _state.update { it.copy(backupResult = BackupResult.Loading) }
        viewModelScope.launch {
            withMinimumLoadingDuration { backupService.createBackup() }
                .onSuccess { jsonString ->
                    val payloadToPersist = if (_state.value.dataEncryptionEnabled) {
                        runCatching { BackupPayloadCipher.encrypt(jsonString) }
                            .getOrElse { error ->
                                val message = error.message ?: SettingsDataConstant.UNKNOWN_ERROR
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

                    val timestamp = SimpleDateFormat(
                        SettingsDataConstant.BACKUP_TIMESTAMP_PATTERN,
                        Locale.getDefault(),
                    )
                        .format(Date())
                    _state.update {
                        it.copy(
                            backupResult = BackupResult.Success,
                            pendingBackupData = payloadToPersist,
                            pendingBackupFileName =
                                "${SettingsDataConstant.BACKUP_FILE_PREFIX}$timestamp${SettingsDataConstant.BACKUP_FILE_SUFFIX}",
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val message = error.message ?: SettingsDataConstant.UNKNOWN_ERROR
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
        logDebug("Restoring backup")
        _state.update { it.copy(restoreResult = RestoreOperationResult.Loading) }

        val payloadToRestore = if (BackupPayloadCipher.isEncryptedPayload(jsonString)) {
            runCatching { BackupPayloadCipher.decrypt(jsonString) }
                .onFailure { error ->
                    // ✅ Log API level info for diagnostics
                    Logger.e(tag = "SettingsDataViewModel") {
                        "Decryption failed on API ${Build.VERSION.SDK_INT}: ${error.message}"
                    }
                }
                .getOrElse { error ->
                    // ✅ Enhanced error message mapping
                    val message = when (error) {
                        is IllegalArgumentException -> {
                            "Payload di backup invalido o danneggiato"
                        }
                        is javax.crypto.BadPaddingException -> {
                            "Chiave di crittografia non corrisponde - password errata?"
                        }
                        is java.security.InvalidKeyException -> {
                            "Chiave di crittografia non valida"
                        }
                        else -> {
                            error.message ?: SettingsDataConstant.UNKNOWN_ERROR
                        }
                    }
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
            withMinimumLoadingDuration { backupService.restoreBackup(payloadToRestore) }
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
                    settingsRepositoryRef.setLastRestoreTimestamp(System.currentTimeMillis())
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val message = error.message ?: SettingsDataConstant.UNKNOWN_ERROR
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
        logDebug("Resetting all preferences")
        viewModelScope.launch {
            settingsRepositoryRef.resetAllPreferences()
            _state.update { it.copy(showResetPreferencesDialog = false) }
        }
    }
}

