package com.antcashmanager.android.ui.screen.settings

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.data.feedback.FeedbackEmailHelper
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import com.antcashmanager.domain.usecase.settings.SettingsUseCasesProvider
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SettingsViewModel(
    private val settingsUseCases: SettingsUseCasesProvider,
    private val deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase,
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val widgetUpdateNotifier: WidgetUpdateNotifier,
) : BaseViewModel<SettingEvent>() {

    // Convenience properties for readability (delegate to provider)
    private val getThemeUseCase get() = settingsUseCases.getTheme
    private val setThemeUseCase get() = settingsUseCases.setTheme
    private val getLanguageUseCase get() = settingsUseCases.getLanguage
    private val setLanguageUseCase get() = settingsUseCases.setLanguage
    private val getShowChartsUseCase get() = settingsUseCases.getShowCharts
    private val setShowChartsUseCase get() = settingsUseCases.setShowCharts
    private val getHighContrastUseCase get() = settingsUseCases.getHighContrast
    private val setHighContrastUseCase get() = settingsUseCases.setHighContrast
    private val getLargeTextUseCase get() = settingsUseCases.getLargeText
    private val setLargeTextUseCase get() = settingsUseCases.setLargeText
    private val getReduceMotionUseCase get() = settingsUseCases.getReduceMotion
    private val setReduceMotionUseCase get() = settingsUseCases.setReduceMotion
    private val getCurrencySymbolUseCase get() = settingsUseCases.getCurrencySymbol
    private val setCurrencySymbolUseCase get() = settingsUseCases.setCurrencySymbol
    private val getDecimalDigitsUseCase get() = settingsUseCases.getDecimalDigits
    private val setDecimalDigitsUseCase get() = settingsUseCases.setDecimalDigits
    private val getDecimalSeparatorUseCase get() = settingsUseCases.getDecimalSeparator
    private val setDecimalSeparatorUseCase get() = settingsUseCases.setDecimalSeparator
    private val getThousandsSeparatorUseCase get() = settingsUseCases.getThousandsSeparator
    private val setThousandsSeparatorUseCase get() = settingsUseCases.setThousandsSeparator
    private val getShowTransactionNotesUseCase get() = settingsUseCases.getShowTransactionNotes
    private val getTransactionDisplayTypeUseCase get() = settingsUseCases.getTransactionDisplayType
    private val setTransactionDisplayTypeUseCase get() = settingsUseCases.setTransactionDisplayType
    private val setTutorialCompletedUseCase get() = settingsUseCases.setTutorialCompleted
    private val resetAllPreferencesUseCase get() = settingsUseCases.resetAllPreferences

    private var setThemeJob: Job? = null
    private var setLanguageJob: Job? = null

    // FIX 2: Track when language is changing to coordinate with exit dialog timing
    // When language changes, the WithAppLocale recomposition creates a new LocalContext
    // This flag allows AppExitConfirmationDialog to increase its delay from 300ms to 500ms
    // to ensure the new context is fully established before Activity.finish() is called
    private val _isLanguageChanging = MutableStateFlow(false)
    val isLanguageChanging: StateFlow<Boolean> = _isLanguageChanging.asStateFlow()

    override fun onEvent(event: SettingEvent) {
        logDebug("Event: $event")
        when (event) {
            is SettingEvent.SetTheme -> setTheme(event.theme)
            is SettingEvent.SetLanguage -> setLanguage(event.language)
            is SettingEvent.SetShowCharts -> setShowCharts(event.show)
            is SettingEvent.SetHighContrast -> setHighContrast(event.enabled)
            is SettingEvent.SetLargeText -> setLargeText(event.enabled)
            is SettingEvent.SetReduceMotion -> setReduceMotion(event.enabled)
            is SettingEvent.SetCurrencySymbol -> setCurrencySymbol(event.symbol)
            is SettingEvent.SetDecimalDigits -> setDecimalDigits(event.digits)
            is SettingEvent.SetDecimalSeparator -> setDecimalSeparator(event.separator)
            is SettingEvent.SetThousandsSeparator -> setThousandsSeparator(event.separator)
            is SettingEvent.SetTransactionDisplayType -> setTransactionDisplayType(event.displayType)
            is SettingEvent.SetTutorialCompleted -> setIsTutorialCompleted(event.completed)
            is SettingEvent.ResetAllPreferences -> resetAllPreferences()
            is SettingEvent.ImportDebugData -> importDebugData(event.context)
            is SettingEvent.SendFeedbackEmail -> sendFeedbackEmail(event.emailBody, event.context)
            is SettingEvent.RetryLastOperation -> logInfo("Retry requested")
        }
    }

    /**
     * Import debug data from asset `debug_initial_data.json`.
     * This runs only when the app is built in DEBUG. It reads the asset and inserts
     * transactions using the provided UseCase. Errors are logged and
     * ignored to keep this safe for debug usage.
     */
    private fun importDebugData(context: Context) {
        if (!BuildConfig.DEBUG) return
        logDebug("Importing debug data from assets")
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val assetName = SettingsConstant.DEBUG_ASSET_NAME
                    val json = try {
                        context.assets.open(assetName).bufferedReader().use { it.readText() }
                    } catch (ex: Exception) {
                        logError("Cannot open debug asset: ${ex.message}")
                        return@withContext
                    }
                    val obj = JSONObject(json)
                    val transactions = obj.optJSONArray(SettingsConstant.JSON_KEY_TRANSACTIONS)
                        ?: return@withContext
                    // Clear existing data for demo
                    deleteAllTransactionsUseCase()
                    for (i in 0 until transactions.length()) {
                        try {
                            val t = transactions.getJSONObject(i)
                            val transaction = Transaction(
                                id = t.optLong(SettingsConstant.JSON_KEY_ID, 0L),
                                title = t.optString(
                                    SettingsConstant.JSON_KEY_TITLE,
                                    SettingsConstant.DEFAULT_TRANSACTION_TITLE,
                                ),
                                amount = t.optDouble(SettingsConstant.JSON_KEY_AMOUNT, 0.0),
                                category = t.optString(
                                    SettingsConstant.JSON_KEY_CATEGORY,
                                    SettingsConstant.DEFAULT_TRANSACTION_CATEGORY,
                                ),
                                type = try {
                                    TransactionType.valueOf(
                                        t.optString(
                                            SettingsConstant.JSON_KEY_TYPE,
                                            SettingsConstant.DEFAULT_TRANSACTION_TYPE,
                                        ),
                                    )
                                } catch (_: Exception) {
                                    TransactionType.EXPENSE
                                },
                                timestamp = t.optLong(
                                    SettingsConstant.JSON_KEY_TIMESTAMP,
                                    System.currentTimeMillis(),
                                ),
                                notes = t.optString(SettingsConstant.JSON_KEY_NOTES, ""),
                                payee = t.optString(SettingsConstant.JSON_KEY_PAYEE, ""),
                                location = t.optString(SettingsConstant.JSON_KEY_LOCATION, ""),
                                isRecurring = t.optBoolean(
                                    SettingsConstant.JSON_KEY_IS_RECURRING,
                                    false
                                ),
                                tags = if (t.has(SettingsConstant.JSON_KEY_TAGS)) {
                                    t.optJSONArray(SettingsConstant.JSON_KEY_TAGS)?.let { arr ->
                                        val list = mutableListOf<String>()
                                        for (j in 0 until arr.length()) list.add(arr.optString(j))
                                        list.joinToString(",")
                                    } ?: t.optString(SettingsConstant.JSON_KEY_TAGS, "")
                                } else {
                                    ""
                                },
                                recurrenceInterval = t.optString(
                                    SettingsConstant.JSON_KEY_RECURRENCE_RULE,
                                    ""
                                ),
                                paymentType = try {
                                    PaymentType.valueOf(
                                        t.optString(
                                            SettingsConstant.JSON_KEY_PAYMENT_TYPE,
                                            SettingsConstant.DEFAULT_PAYMENT_TYPE,
                                        ),
                                    )
                                } catch (_: Exception) {
                                    PaymentType.ELECTRONIC
                                },
                            )
                            try {
                                insertTransactionUseCase(transaction)
                            } catch (insertError: Exception) {
                                // FASE 5: Log individual insert failures in debug import
                                logWarn("Failed to insert transaction: ${insertError.message}")
                            }
                        } catch (entryError: Exception) {
                            // FASE 5: Log malformed entries
                            logWarn("Skipped malformed entry: ${entryError.message}")
                        }
                    }
                }
            } catch (ex: Exception) {
                logError("Error importing debug data: ${ex.message}")
            }
        }
    }


    // Stato aggregato delle preferenze - combinare i flussi in gruppi
    val state: StateFlow<SettingsState> = combine(
        combine(
            getThemeUseCase().map {
                val themeName = it.getOrNull() ?: SettingsConstant.DEFAULT_THEME.name
                try { AppTheme.valueOf(themeName) } catch (_: Exception) { SettingsConstant.DEFAULT_THEME }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_THEME,
            ),
            getLanguageUseCase().map {
                val langName = it.getOrNull() ?: SettingsConstant.DEFAULT_LANGUAGE.name
                try { AppLanguage.valueOf(langName) } catch (_: Exception) { SettingsConstant.DEFAULT_LANGUAGE }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_LANGUAGE,
            ),
            getShowChartsUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_SHOW_CHARTS) }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_SHOW_CHARTS,
            ),
            getHighContrastUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_HIGH_CONTRAST) }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_HIGH_CONTRAST,
            ),
            getLargeTextUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_LARGE_TEXT) }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_LARGE_TEXT,
            ),
        ) { theme, language, showCharts, highContrast, largeText ->
            SettingsPreferences1(theme, language, showCharts, highContrast, largeText)
        },
        combine(
            getReduceMotionUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_REDUCE_MOTION) }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_REDUCE_MOTION,
            ),
            getCurrencySymbolUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_CURRENCY_SYMBOL) }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_CURRENCY_SYMBOL,
            ),
            getDecimalDigitsUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_DECIMAL_DIGITS) }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_DECIMAL_DIGITS,
            ),
            getDecimalSeparatorUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_DECIMAL_SEPARATOR) }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_DECIMAL_SEPARATOR,
            ),
            getThousandsSeparatorUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_THOUSANDS_SEPARATOR) }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_THOUSANDS_SEPARATOR,
            ),
        ) { reduceMotion, currencySymbol, decimalDigits, decimalSeparator, thousandsSeparator ->
            SettingsPreferences2(
                reduceMotion,
                currencySymbol,
                decimalDigits,
                decimalSeparator,
                thousandsSeparator
            )
        },
        getShowTransactionNotesUseCase().map { it.getOrDefault(SettingsConstant.DEFAULT_SHOW_TRANSACTION_NOTES) }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
            SettingsConstant.DEFAULT_SHOW_TRANSACTION_NOTES,
        ),
        getTransactionDisplayTypeUseCase().map {
            val typeName = it.getOrNull() ?: SettingsConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE.name
            try { TransactionDisplayType.valueOf(typeName) } catch (_: Exception) { SettingsConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
            SettingsConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE,
        ),
    ) { prefs1, prefs2, showTransactionNotes, transactionDisplayType ->
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
            transactionDisplayType = transactionDisplayType,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
        SettingsState(),
    )

    /**
     * Funzione di utilità per loggare e lanciare l'azione in coroutine.
     */
    private fun updatePreference(logMsg: String, action: suspend () -> Any?) {
        logDebug(logMsg)
        viewModelScope.launch {
            try {
                val result = action()
                if (result is Result<*>) {
                    result.onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Preference update failed: ${error.message}", error)
                    }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                logError("Preference update failed: ${ex.message}", ex)
            }
        }
    }

    private fun setTheme(theme: AppTheme) {
        // Debounce: cancel previous job and schedule new one with 300ms delay
        setThemeJob?.cancel()
        setThemeJob = viewModelScope.launch {
            delay(300)
            updatePreference(
                logMsg = "Setting theme to: $theme",
                action = { setThemeUseCase(theme.name) },
            )
        }
    }

    private fun setLanguage(language: AppLanguage) {
        // Debounce: cancel previous job and schedule new one with 300ms delay
        setLanguageJob?.cancel()
        // FIX 2: Set flag to indicate language is changing
        _isLanguageChanging.value = true
        setLanguageJob = viewModelScope.launch {
            delay(300)
            updatePreference(
                logMsg = "Setting language to: $language",
                action = {
                    val result = setLanguageUseCase(language.name)
                    widgetUpdateNotifier.notifyTransactionsChanged()
                    result
                },
            )
            // Wait a bit more for WithAppLocale recomposition to complete
            delay(100)
            // FIX 2: Clear flag after language change and recomposition complete
            _isLanguageChanging.value = false
        }
    }

    private fun setShowCharts(show: Boolean) = updatePreference(
        logMsg = "Setting show charts: $show",
        action = { setShowChartsUseCase(show) },
    )

    private fun setHighContrast(enabled: Boolean) = updatePreference(
        logMsg = "Setting high contrast: $enabled",
        action = { setHighContrastUseCase(enabled) },
    )

    private fun setLargeText(enabled: Boolean) = updatePreference(
        logMsg = "Setting large text: $enabled",
        action = { setLargeTextUseCase(enabled) },
    )

    private fun setReduceMotion(enabled: Boolean) = updatePreference(
        logMsg = "Setting reduce motion: $enabled",
        action = { setReduceMotionUseCase(enabled) },
    )

    private fun setCurrencySymbol(symbol: String) = updatePreference(
        logMsg = "Setting currency symbol: $symbol",
        action = { setCurrencySymbolUseCase(symbol) },
    )

    private fun setDecimalDigits(digits: Int) = updatePreference(
        logMsg = "Setting decimal digits: $digits",
        action = { setDecimalDigitsUseCase(digits) },
    )

    private fun setDecimalSeparator(separator: String) = updatePreference(
        logMsg = "Setting decimal separator: $separator",
        action = { setDecimalSeparatorUseCase(separator) },
    )

    private fun setThousandsSeparator(separator: String) = updatePreference(
        logMsg = "Setting thousands separator: $separator",
        action = { setThousandsSeparatorUseCase(separator) },
    )

    private fun setTransactionDisplayType(displayType: TransactionDisplayType) = updatePreference(
        logMsg = "Setting transaction display type: $displayType",
        action = { setTransactionDisplayTypeUseCase(displayType.name) },
    )

    private fun setIsTutorialCompleted(completed: Boolean) = updatePreference(
        logMsg = "Setting tutorial completed: $completed",
        action = { setTutorialCompletedUseCase(completed) },
    )

    private fun resetAllPreferences() = updatePreference(
        logMsg = "Resetting all preferences",
        action = {
            val result = resetAllPreferencesUseCase()
            widgetUpdateNotifier.notifyTransactionsChanged()
            result
        },
    )

    private fun sendFeedbackEmail(emailBody: String, applicationContext: Context): Boolean {
        val success = FeedbackEmailHelper.sendFeedbackEmail(
            applicationContext,
            emailBody,
            BuildConfig.VERSION_NAME
        )
        if (success) {
            logDebug("Feedback email intent launched successfully")
        } else {
            logWarn("No email app available to send feedback")
        }
        return success
    }
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
