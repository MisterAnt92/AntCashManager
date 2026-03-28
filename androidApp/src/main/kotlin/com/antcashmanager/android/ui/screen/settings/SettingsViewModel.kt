package com.antcashmanager.android.ui.screen.home.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import com.antcashmanager.domain.usecase.settings.SetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.SetThemeUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
        private const val SHARING_TIMEOUT = 5_000L
        private val DEFAULT_THEME = AppTheme.SYSTEM
        private val DEFAULT_LANGUAGE = AppLanguage.SYSTEM
        private const val DEFAULT_SHOW_CHARTS = true
        private const val DEFAULT_HIGH_CONTRAST = false
        private const val DEFAULT_LARGE_TEXT = false
        private const val DEFAULT_REDUCE_MOTION = false
        private const val DEFAULT_CURRENCY_SYMBOL = "\u20ac"
        private const val DEFAULT_DECIMAL_DIGITS = 2
        private const val DEFAULT_DECIMAL_SEPARATOR = ","
        private const val DEFAULT_THOUSANDS_SEPARATOR = "."
        private const val DEFAULT_SHOW_TRANSACTION_NOTES = true
    }

    private val getThemeUseCase = GetThemeUseCase(settingsRepository)
    private val setThemeUseCase = SetThemeUseCase(settingsRepository)
    private val getLanguageUseCase = GetLanguageUseCase(settingsRepository)
    private val setLanguageUseCase = SetLanguageUseCase(settingsRepository)
    private val deleteAllTransactionsUseCase = DeleteAllTransactionsUseCase(transactionRepository)

    private val backupService = BackupService(transactionRepository, categoryRepository)

    private val _backupResult = MutableStateFlow<BackupResult>(BackupResult.Idle)
    val backupResult: StateFlow<BackupResult> = _backupResult.asStateFlow()

    private val _restoreResult =
        MutableStateFlow<RestoreOperationResult>(RestoreOperationResult.Idle)
    val restoreResult: StateFlow<RestoreOperationResult> = _restoreResult.asStateFlow()

    /**
     * Import debug data from asset `debug_initial_data.json`.
     * This runs only when the app is built in DEBUG. It reads the asset and inserts
     * transactions using the provided TransactionRepository. Errors are logged and
     * ignored to keep this safe for debug usage.
     */
    fun importDebugData(context: Context) {
        if (!BuildConfig.DEBUG) return
        Logger.d("SettingsViewModel") { "Importing debug data from assets" }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val assetName = "debug_initial_data.json"
                    val json = try {
                        context.assets.open(assetName).bufferedReader().use { it.readText() }
                    } catch (ex: Exception) {
                        Logger.e("SettingsViewModel") { "Cannot open debug asset: ${ex.message}" }
                        return@withContext
                    }
                    val obj = JSONObject(json)
                    val transactions = obj.optJSONArray("transactions") ?: return@withContext
                    // Clear existing data for demo
                    transactionRepository.deleteAllTransactions()
                    for (i in 0 until transactions.length()) {
                        try {
                            val t = transactions.getJSONObject(i)
                            val transaction = Transaction(
                                id = t.optLong("id", 0L),
                                title = t.optString("title", "Senza titolo"),
                                amount = t.optDouble("amount", 0.0),
                                category = t.optString("category", "Uncategorized"),
                                type = try {
                                    TransactionType.valueOf(t.optString("type", "EXPENSE"))
                                } catch (_: Exception) {
                                    TransactionType.EXPENSE
                                },
                                timestamp = t.optLong("timestamp", System.currentTimeMillis()),
                                notes = t.optString("notes", ""),
                                payee = t.optString("payee", ""),
                                location = t.optString("location", ""),
                                isRecurring = t.optBoolean("isRecurring", false),
                                tags = if (t.has("tags")) {
                                    t.optJSONArray("tags")?.let { arr ->
                                        val list = mutableListOf<String>()
                                        for (j in 0 until arr.length()) list.add(arr.optString(j))
                                        list.joinToString(",")
                                    } ?: t.optString("tags", "")
                                } else {
                                    ""
                                },
                                recurrenceInterval = t.optString("recurrenceRule", ""),
                            )
                            try {
                                transactionRepository.insertTransaction(transaction)
                            } catch (_: Exception) {
                                // ignore individual insert failures in debug import
                            }
                        } catch (_: Exception) {
                            // ignore malformed entries
                        }
                    }
                }
            } catch (ex: Exception) {
                Logger.e("SettingsViewModel") { "Error importing debug data: ${ex.message}" }
            }
        }
    }


    // Stato aggregato delle preferenze - combinare i flussi in gruppi
    val state: StateFlow<SettingsState> = combine(
        combine(
            getThemeUseCase().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_THEME
            ),
            getLanguageUseCase().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_LANGUAGE
            ),
            settingsRepository.getShowCharts().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_SHOW_CHARTS
            ),
            settingsRepository.getHighContrast().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_HIGH_CONTRAST
            ),
            settingsRepository.getLargeText().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_LARGE_TEXT
            ),
        ) { theme, language, showCharts, highContrast, largeText ->
            SettingsPreferences1(theme, language, showCharts, highContrast, largeText)
        },
        combine(
            settingsRepository.getReduceMotion().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_REDUCE_MOTION
            ),
            settingsRepository.getCurrencySymbol().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_CURRENCY_SYMBOL
            ),
            settingsRepository.getDecimalDigits().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_DECIMAL_DIGITS
            ),
            settingsRepository.getDecimalSeparator().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_DECIMAL_SEPARATOR
            ),
            settingsRepository.getThousandsSeparator().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
                DEFAULT_THOUSANDS_SEPARATOR
            ),
        ) { reduceMotion, currencySymbol, decimalDigits, decimalSeparator, thousandsSeparator ->
            SettingsPreferences2(reduceMotion, currencySymbol, decimalDigits, decimalSeparator, thousandsSeparator)
        },
        settingsRepository.getShowTransactionNotes().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_SHOW_TRANSACTION_NOTES
        ),
    ) { prefs1, prefs2, showTransactionNotes ->
        SettingsState(
            theme = prefs1.theme,
            language = prefs1.language,
            showCharts = prefs1.showCharts,
            highContrast = prefs1.highContrast,
            largeText = prefs1.largeText,
            reduceMotion = prefs2.reduceMotion,
            currencySymbol = prefs2.currencySymbol,
            decimalDigits = prefs2.decimalDigits,
            decimalSeparator = prefs2.decimalSeparator,
            thousandsSeparator = prefs2.thousandsSeparator,
            showTransactionNotes = showTransactionNotes,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT), SettingsState())

    private val _deleteResult = MutableStateFlow<DeleteResult>(DeleteResult.Idle)
    val deleteResult: StateFlow<DeleteResult> = _deleteResult.asStateFlow()


    /**
     * Funzione di utilità per loggare e lanciare l'azione in coroutine.
     */
    private fun updatePreference(logMsg: String, action: suspend () -> Unit) {
        Logger.d(TAG) { logMsg }
        viewModelScope.launch { action() }
    }

    fun setTheme(theme: AppTheme) = updatePreference(
        logMsg = "Setting theme to: $theme",
        action = { setThemeUseCase(theme) },
    )

    fun setLanguage(language: AppLanguage) = updatePreference(
        logMsg = "Setting language to: $language",
        action = { setLanguageUseCase(language) },
    )

    fun setShowCharts(show: Boolean) = updatePreference(
        logMsg = "Setting show charts: $show",
        action = { settingsRepository.setShowCharts(show) },
    )

    fun setHighContrast(enabled: Boolean) = updatePreference(
        logMsg = "Setting high contrast: $enabled",
        action = { settingsRepository.setHighContrast(enabled) },
    )

    fun setLargeText(enabled: Boolean) = updatePreference(
        logMsg = "Setting large text: $enabled",
        action = { settingsRepository.setLargeText(enabled) },
    )

    fun setReduceMotion(enabled: Boolean) = updatePreference(
        logMsg = "Setting reduce motion: $enabled",
        action = { settingsRepository.setReduceMotion(enabled) },
    )

    fun setCurrencySymbol(symbol: String) = updatePreference(
        logMsg = "Setting currency symbol: $symbol",
        action = { settingsRepository.setCurrencySymbol(symbol) },
    )

    fun setDecimalDigits(digits: Int) = updatePreference(
        logMsg = "Setting decimal digits: $digits",
        action = { settingsRepository.setDecimalDigits(digits) },
    )

    fun setDecimalSeparator(separator: String) = updatePreference(
        logMsg = "Setting decimal separator: $separator",
        action = { settingsRepository.setDecimalSeparator(separator) },
    )

    fun setThousandsSeparator(separator: String) = updatePreference(
        logMsg = "Setting thousands separator: $separator",
        action = { settingsRepository.setThousandsSeparator(separator) },
    )

    fun setShowTransactionNotes(show: Boolean) = updatePreference(
        logMsg = "Setting show transaction notes: $show",
        action = { settingsRepository.setShowTransactionNotes(show) },
    )

    fun resetAllPreferences() = updatePreference(
        logMsg = "Resetting all preferences",
        action = { settingsRepository.resetAllPreferences() },
    )

    fun deleteAllData() {
        Logger.d("SettingsViewModel") { "Deleting all data" }
        viewModelScope.launch {
            try {
                deleteAllTransactionsUseCase()
                categoryRepository.deleteAllCategories()
                _deleteResult.value = DeleteResult.Success
            } catch (e: Exception) {
                Logger.e("SettingsViewModel") { "Error deleting data: ${e.message}" }
                _deleteResult.value = DeleteResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetDeleteResult() {
        _deleteResult.value = DeleteResult.Idle
    }

    /**
     * Creates a backup of all app data and returns the JSON string.
     * The caller is responsible for saving the string to a file.
     */
    fun createBackup(onResult: (String?) -> Unit) {
        Logger.d("SettingsViewModel") { "Creating backup..." }
        _backupResult.value = BackupResult.Loading
        viewModelScope.launch {
            val result = backupService.createBackup()
            result.fold(
                onSuccess = { jsonString ->
                    _backupResult.value = BackupResult.Success
                    onResult(jsonString)
                },
                onFailure = { error ->
                    _backupResult.value = BackupResult.Error(error.message ?: "Unknown error")
                    onResult(null)
                },
            )
        }
    }

    /**
     * Restores app data from a JSON string.
     */
    fun restoreBackup(jsonString: String) {
        Logger.d("SettingsViewModel") { "Restoring backup..." }
        _restoreResult.value = RestoreOperationResult.Loading
        viewModelScope.launch {
            val result = backupService.restoreBackup(jsonString)
            result.fold(
                onSuccess = { restoreResult ->
                    _restoreResult.value = RestoreOperationResult.Success(
                        transactions = restoreResult.transactionsRestored,
                        categories = restoreResult.categoriesRestored,
                    )
                },
                onFailure = { error ->
                    _restoreResult.value =
                        RestoreOperationResult.Error(error.message ?: "Unknown error")
                },
            )
        }
    }

    fun resetBackupResult() {
        _backupResult.value = BackupResult.Idle
    }

    fun resetRestoreResult() {
        _restoreResult.value = RestoreOperationResult.Idle
    }
}

sealed interface DeleteResult {
    data object Idle : DeleteResult
    data object Success : DeleteResult
    data class Error(val message: String) : DeleteResult
}

sealed interface BackupResult {
    data object Idle : BackupResult
    data object Loading : BackupResult
    data object Success : BackupResult
    data class Error(val message: String) : BackupResult
}

sealed interface RestoreOperationResult {
    data object Idle : RestoreOperationResult
    data object Loading : RestoreOperationResult
    data class Success(val transactions: Int, val categories: Int) : RestoreOperationResult
    data class Error(val message: String) : RestoreOperationResult
}

// Data class di supporto per il combine dei preferenze
private data class SettingsPreferences1(
    val theme: AppTheme,
    val language: AppLanguage,
    val showCharts: Boolean,
    val highContrast: Boolean,
    val largeText: Boolean,
)

private data class SettingsPreferences2(
    val reduceMotion: Boolean,
    val currencySymbol: String,
    val decimalDigits: Int,
    val decimalSeparator: String,
    val thousandsSeparator: String,
)

