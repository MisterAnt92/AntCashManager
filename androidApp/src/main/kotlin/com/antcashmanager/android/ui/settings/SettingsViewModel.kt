package com.antcashmanager.android.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val getThemeUseCase = GetThemeUseCase(settingsRepository)
    private val setThemeUseCase = SetThemeUseCase(settingsRepository)
    private val getLanguageUseCase = GetLanguageUseCase(settingsRepository)
    private val setLanguageUseCase = SetLanguageUseCase(settingsRepository)
    private val deleteAllTransactionsUseCase = DeleteAllTransactionsUseCase(transactionRepository)

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
                    } catch (e: Exception) {
                        Logger.e("SettingsViewModel") { "Cannot open debug asset: ${e.message}" }
                        return@withContext
                    }
                    val obj = JSONObject(json)
                    val transactions = obj.optJSONArray("transactions") ?: return@withContext
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
                                } catch (e: Exception) {
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
                                recurrenceInterval = t.optString("recurrenceInterval", ""),
                            )
                            try {
                                transactionRepository.insertTransaction(transaction)
                            } catch (e: Exception) {
                                // ignore individual insert failures in debug import
                            }
                        } catch (e: Exception) {
                            // ignore malformed entries
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("SettingsViewModel") { "Error importing debug data: ${e.message}" }
            }
        }
    }

    val theme = getThemeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM,
        )

    val language = getLanguageUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppLanguage.SYSTEM,
        )

    val showCharts = settingsRepository.getShowCharts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val highContrast = settingsRepository.getHighContrast()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val largeText = settingsRepository.getLargeText()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val reduceMotion = settingsRepository.getReduceMotion()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val currencySymbol = settingsRepository.getCurrencySymbol()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "\u20ac")

    val decimalDigits = settingsRepository.getDecimalDigits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2)

    val decimalSeparator = settingsRepository.getDecimalSeparator()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ",")

    val thousandsSeparator = settingsRepository.getThousandsSeparator()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ".")

    private val _deleteResult = MutableStateFlow<DeleteResult>(DeleteResult.Idle)
    val deleteResult: StateFlow<DeleteResult> = _deleteResult.asStateFlow()

    fun setTheme(theme: AppTheme) {
        Logger.d("SettingsViewModel") { "Setting theme to: $theme" }
        viewModelScope.launch {
            setThemeUseCase(theme)
        }
    }

    fun setLanguage(language: AppLanguage) {
        Logger.d("SettingsViewModel") { "Setting language to: $language" }
        viewModelScope.launch {
            setLanguageUseCase(language)
        }
    }

    fun setShowCharts(show: Boolean) {
        Logger.d("SettingsViewModel") { "Setting show charts: $show" }
        viewModelScope.launch {
            settingsRepository.setShowCharts(show)
        }
    }

    fun setHighContrast(enabled: Boolean) {
        Logger.d("SettingsViewModel") { "Setting high contrast: $enabled" }
        viewModelScope.launch {
            settingsRepository.setHighContrast(enabled)
        }
    }

    fun setLargeText(enabled: Boolean) {
        Logger.d("SettingsViewModel") { "Setting large text: $enabled" }
        viewModelScope.launch {
            settingsRepository.setLargeText(enabled)
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        Logger.d("SettingsViewModel") { "Setting reduce motion: $enabled" }
        viewModelScope.launch {
            settingsRepository.setReduceMotion(enabled)
        }
    }

    fun setCurrencySymbol(symbol: String) {
        Logger.d("SettingsViewModel") { "Setting currency symbol: $symbol" }
        viewModelScope.launch { settingsRepository.setCurrencySymbol(symbol) }
    }

    fun setDecimalDigits(digits: Int) {
        Logger.d("SettingsViewModel") { "Setting decimal digits: $digits" }
        viewModelScope.launch { settingsRepository.setDecimalDigits(digits) }
    }

    fun setDecimalSeparator(separator: String) {
        Logger.d("SettingsViewModel") { "Setting decimal separator: $separator" }
        viewModelScope.launch { settingsRepository.setDecimalSeparator(separator) }
    }

    fun setThousandsSeparator(separator: String) {
        Logger.d("SettingsViewModel") { "Setting thousands separator: $separator" }
        viewModelScope.launch { settingsRepository.setThousandsSeparator(separator) }
    }

    fun resetAllPreferences() {
        Logger.d("SettingsViewModel") { "Resetting all preferences" }
        viewModelScope.launch { settingsRepository.resetAllPreferences() }
    }

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
}

sealed interface DeleteResult {
    data object Idle : DeleteResult
    data object Success : DeleteResult
    data class Error(val message: String) : DeleteResult
}
