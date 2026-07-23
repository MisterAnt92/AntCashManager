package com.antcashmanager.android.ui.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.data.feedback.FeedbackEmailHelper
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.usecase.settings.GetCurrencySymbolUseCase
import com.antcashmanager.domain.usecase.settings.GetDecimalDigitsUseCase
import com.antcashmanager.domain.usecase.settings.GetDecimalSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.GetHighContrastUseCase
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetLargeTextUseCase
import com.antcashmanager.domain.usecase.settings.GetReduceMotionUseCase
import com.antcashmanager.domain.usecase.settings.GetShowChartsUseCase
import com.antcashmanager.domain.usecase.settings.GetShowTransactionNotesUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import com.antcashmanager.domain.usecase.settings.GetThousandsSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.GetTransactionDisplayTypeUseCase
import com.antcashmanager.domain.usecase.settings.ResetAllPreferencesUseCase
import com.antcashmanager.domain.usecase.settings.SetCurrencySymbolUseCase
import com.antcashmanager.domain.usecase.settings.SetDecimalDigitsUseCase
import com.antcashmanager.domain.usecase.settings.SetDecimalSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.SetHighContrastUseCase
import com.antcashmanager.domain.usecase.settings.SetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.SetLargeTextUseCase
import com.antcashmanager.domain.usecase.settings.SetReduceMotionUseCase
import com.antcashmanager.domain.usecase.settings.SetShowChartsUseCase
import com.antcashmanager.domain.usecase.settings.SetShowTransactionNotesUseCase
import com.antcashmanager.domain.usecase.settings.SetThemeUseCase
import com.antcashmanager.domain.usecase.settings.SetThousandsSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.SetTransactionDisplayTypeUseCase
import com.antcashmanager.domain.usecase.settings.SetTutorialCompletedUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class SettingsViewModel(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val getShowChartsUseCase: GetShowChartsUseCase,
    private val setShowChartsUseCase: SetShowChartsUseCase,
    private val getHighContrastUseCase: GetHighContrastUseCase,
    private val setHighContrastUseCase: SetHighContrastUseCase,
    private val getLargeTextUseCase: GetLargeTextUseCase,
    private val setLargeTextUseCase: SetLargeTextUseCase,
    private val getReduceMotionUseCase: GetReduceMotionUseCase,
    private val setReduceMotionUseCase: SetReduceMotionUseCase,
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase,
    private val setCurrencySymbolUseCase: SetCurrencySymbolUseCase,
    private val getDecimalDigitsUseCase: GetDecimalDigitsUseCase,
    private val setDecimalDigitsUseCase: SetDecimalDigitsUseCase,
    private val getDecimalSeparatorUseCase: GetDecimalSeparatorUseCase,
    private val setDecimalSeparatorUseCase: SetDecimalSeparatorUseCase,
    private val getThousandsSeparatorUseCase: GetThousandsSeparatorUseCase,
    private val setThousandsSeparatorUseCase: SetThousandsSeparatorUseCase,
    private val getShowTransactionNotesUseCase: GetShowTransactionNotesUseCase,
    private val setShowTransactionNotesUseCase: SetShowTransactionNotesUseCase,
    private val getTransactionDisplayTypeUseCase: GetTransactionDisplayTypeUseCase,
    private val setTransactionDisplayTypeUseCase: SetTransactionDisplayTypeUseCase,
    private val setTutorialCompletedUseCase: SetTutorialCompletedUseCase,
    private val resetAllPreferencesUseCase: ResetAllPreferencesUseCase,
    private val deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase,
    private val insertTransactionUseCase: InsertTransactionUseCase,
) : ViewModel() {

    private var setThemeJob: Job? = null
    private var setLanguageJob: Job? = null

    /**
     * Import debug data from asset `debug_initial_data.json`.
     * This runs only when the app is built in DEBUG. It reads the asset and inserts
     * transactions using the provided UseCase. Errors are logged and
     * ignored to keep this safe for debug usage.
     */
    fun importDebugData(context: Context) {
        if (!BuildConfig.DEBUG) return
        Logger.d(tag = SettingsConstant.TAG) { "Importing debug data from assets" }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val assetName = SettingsConstant.DEBUG_ASSET_NAME
                    val json = try {
                        context.assets.open(assetName).bufferedReader().use { it.readText() }
                    } catch (ex: Exception) {
                        Logger.e(tag = SettingsConstant.TAG) { "Cannot open debug asset: ${ex.message}" }
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
                            } catch (_: Exception) {
                                // ignore individual insert failures in debug import
                            }
                        } catch (_: Exception) {
                            // ignore malformed entries
                        }
                    }
                }
            } catch (ex: Exception) {
                Logger.e(tag = SettingsConstant.TAG) { "Error importing debug data: ${ex.message}" }
            }
        }
    }


    // Stato aggregato delle preferenze - combinare i flussi in gruppi
    val state: StateFlow<SettingsState> = combine(
        combine(
            getThemeUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_THEME } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_THEME,
            ),
            getLanguageUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_LANGUAGE } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_LANGUAGE,
            ),
            getShowChartsUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_SHOW_CHARTS } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_SHOW_CHARTS,
            ),
            getHighContrastUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_HIGH_CONTRAST } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_HIGH_CONTRAST,
            ),
            getLargeTextUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_LARGE_TEXT } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_LARGE_TEXT,
            ),
        ) { theme, language, showCharts, highContrast, largeText ->
            SettingsPreferences1(theme, language, showCharts, highContrast, largeText)
        },
        combine(
            getReduceMotionUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_REDUCE_MOTION } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_REDUCE_MOTION,
            ),
            getCurrencySymbolUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_CURRENCY_SYMBOL } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_CURRENCY_SYMBOL,
            ),
            getDecimalDigitsUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_DECIMAL_DIGITS } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_DECIMAL_DIGITS,
            ),
            getDecimalSeparatorUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_DECIMAL_SEPARATOR } }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
                SettingsConstant.DEFAULT_DECIMAL_SEPARATOR,
            ),
            getThousandsSeparatorUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_THOUSANDS_SEPARATOR } }.stateIn(
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
        getShowTransactionNotesUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_SHOW_TRANSACTION_NOTES } }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SettingsConstant.SHARING_TIMEOUT),
            SettingsConstant.DEFAULT_SHOW_TRANSACTION_NOTES,
        ),
        getTransactionDisplayTypeUseCase().map { it.getOrElse { SettingsConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE } }.stateIn(
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
        Logger.d(tag = SettingsConstant.TAG) { logMsg }
        viewModelScope.launch {
            try {
                val result = action()
                if (result is Result<*>) {
                    result.onFailure { error ->
                        if (error is CancellationException) throw error
                        Logger.e(throwable = error, tag = SettingsConstant.TAG) {
                            "Preference update failed: ${error.message}"
                        }
                    }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                Logger.e(
                    throwable = ex,
                    tag = SettingsConstant.TAG
                ) { "Preference update failed: ${ex.message}" }
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        // Debounce: cancel previous job and schedule new one with 300ms delay
        setThemeJob?.cancel()
        setThemeJob = viewModelScope.launch {
            delay(300)
            updatePreference(
                logMsg = "Setting theme to: $theme",
                action = { setThemeUseCase(theme) },
            )
        }
    }

    fun setLanguage(language: AppLanguage) {
        // Debounce: cancel previous job and schedule new one with 300ms delay
        setLanguageJob?.cancel()
        setLanguageJob = viewModelScope.launch {
            delay(300)
            updatePreference(
                logMsg = "Setting language to: $language",
                action = { setLanguageUseCase(language) },
            )
        }
    }

    fun setShowCharts(show: Boolean) = updatePreference(
        logMsg = "Setting show charts: $show",
        action = { setShowChartsUseCase(show) },
    )

    fun setHighContrast(enabled: Boolean) = updatePreference(
        logMsg = "Setting high contrast: $enabled",
        action = { setHighContrastUseCase(enabled) },
    )

    fun setLargeText(enabled: Boolean) = updatePreference(
        logMsg = "Setting large text: $enabled",
        action = { setLargeTextUseCase(enabled) },
    )

    fun setReduceMotion(enabled: Boolean) = updatePreference(
        logMsg = "Setting reduce motion: $enabled",
        action = { setReduceMotionUseCase(enabled) },
    )

    fun setCurrencySymbol(symbol: String) = updatePreference(
        logMsg = "Setting currency symbol: $symbol",
        action = { setCurrencySymbolUseCase(symbol) },
    )

    fun setDecimalDigits(digits: Int) = updatePreference(
        logMsg = "Setting decimal digits: $digits",
        action = { setDecimalDigitsUseCase(digits) },
    )

    fun setDecimalSeparator(separator: String) = updatePreference(
        logMsg = "Setting decimal separator: $separator",
        action = { setDecimalSeparatorUseCase(separator) },
    )

    fun setThousandsSeparator(separator: String) = updatePreference(
        logMsg = "Setting thousands separator: $separator",
        action = { setThousandsSeparatorUseCase(separator) },
    )

    fun setShowTransactionNotes(show: Boolean) = updatePreference(
        logMsg = "Setting show transaction notes: $show",
        action = { setShowTransactionNotesUseCase(show) },
    )

    fun setTransactionDisplayType(displayType: TransactionDisplayType) =
        updatePreference(
            logMsg = "Setting transaction display type: $displayType",
            action = { setTransactionDisplayTypeUseCase(displayType) },
        )

    fun setIsTutorialCompleted(completed: Boolean) = updatePreference(
        logMsg = "Setting tutorial completed: $completed",
        action = { setTutorialCompletedUseCase(completed) },
    )

    fun resetAllPreferences() = updatePreference(
        logMsg = "Resetting all preferences",
        action = { resetAllPreferencesUseCase() },
    )

    fun sendFeedbackEmail(emailBody: String, applicationContext: Context): Boolean {
        val success = FeedbackEmailHelper.sendFeedbackEmail(
            applicationContext,
            emailBody,
            BuildConfig.VERSION_NAME
        )
        if (success) {
            Logger.d(tag = SettingsConstant.TAG) { "Feedback email intent launched successfully" }
        } else {
            Logger.w(tag = SettingsConstant.TAG) { "No email app available to send feedback" }
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
