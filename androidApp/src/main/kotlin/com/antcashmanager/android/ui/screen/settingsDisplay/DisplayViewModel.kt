package com.antcashmanager.android.ui.screen.settingsDisplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * ViewModel per la gestione delle preferenze di visualizzazione.
 * Espone lo stato tramite StateFlow e fornisce metodi per aggiornare le preferenze.
 * Tutti i valori di default sono centralizzati in costanti private.
 */
class DisplayViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "DisplayViewModel"
        private val DEFAULT_CURRENCY_SYMBOL = CurrencyFormat.DEFAULT.currencySymbol
        private val DEFAULT_DECIMAL_DIGITS = CurrencyFormat.DEFAULT.decimalDigits
        private val DEFAULT_DECIMAL_SEPARATOR = CurrencyFormat.DEFAULT.decimalSeparator
        private val DEFAULT_THOUSANDS_SEPARATOR = CurrencyFormat.DEFAULT.thousandsSeparator
        private const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy"
        private const val DEFAULT_SHOW_TRANSACTION_NOTES = true
        private const val SHARING_TIMEOUT = 5_000L

        private val SUPPORTED_CURRENCY_SYMBOLS =
            CurrencyFormat.SUPPORTED_CURRENCIES.map { it.first }.toSet()
        private val SUPPORTED_DECIMAL_SEPARATORS =
            CurrencyFormat.DECIMAL_SEPARATORS.map { it.first }.toSet()
        private val SUPPORTED_THOUSANDS_SEPARATORS =
            CurrencyFormat.THOUSANDS_SEPARATORS.map { it.first }.toSet()
    }

    // Espone il simbolo valuta attuale
    val currencySymbol = settingsRepository.getCurrencySymbol()
        .map(::sanitizeCurrencySymbol)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_CURRENCY_SYMBOL,
        )

    // Espone il numero di cifre decimali
    val decimalDigits = settingsRepository.getDecimalDigits()
        .map(::sanitizeDecimalDigits)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_DECIMAL_DIGITS,
        )

    // Espone il separatore decimale
    val decimalSeparator = settingsRepository.getDecimalSeparator()
        .map(::sanitizeDecimalSeparator)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_DECIMAL_SEPARATOR,
        )

    // Espone il separatore delle migliaia
    val thousandsSeparator = combine(
        settingsRepository.getThousandsSeparator(),
        settingsRepository.getDecimalSeparator().map(::sanitizeDecimalSeparator),
    ) { thousands, decimal ->
        sanitizeThousandsSeparator(thousands, decimal)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_THOUSANDS_SEPARATOR,
        )


    // Espone la preferenza per la visualizzazione della sezione grafici
    val showChartsSection = settingsRepository.getShowCharts()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            true,
        )

    // Espone il formato data
    val dateFormat = settingsRepository.getDateFormat()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_DATE_FORMAT,
        )

    // Espone la preferenza per mostrare le note delle transazioni
    val showTransactionNotes = settingsRepository.getShowTransactionNotes()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_SHOW_TRANSACTION_NOTES,
        )

    // Espone la preferenza per mostrare il breakdown dei pagamenti
    val showPaymentTypeBreakdown = settingsRepository.getShowPaymentTypeBreakdown()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            false,
        )

    // Espone il tipo di visualizzazione delle transazioni (Home)
    val transactionDisplayType = settingsRepository.getTransactionDisplayType()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            TransactionDisplayType.TREND,
        )

    // Espone il tipo di visualizzazione delle transazioni (Transazioni)
    val transactionsTransactionDisplayType = settingsRepository.getTransactionsTransactionDisplayType()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            TransactionDisplayType.TREND,
        )

    /**
     * Aggiorna il simbolo valuta.
     */
    fun setCurrencySymbol(symbol: String) = updatePreference(
        logMsg = "Setting currency symbol: $symbol",
        action = {
            settingsRepository.setCurrencySymbol(sanitizeCurrencySymbol(symbol))
        },
    )

    /**
     * Aggiorna il numero di cifre decimali.
     */
    fun setDecimalDigits(digits: Int) = updatePreference(
        logMsg = "Setting decimal digits: $digits",
        action = {
            settingsRepository.setDecimalDigits(sanitizeDecimalDigits(digits))
        },
    )

    /**
     * Aggiorna il separatore decimale.
     */
    fun setDecimalSeparator(separator: String) = updatePreference(
        logMsg = "Setting decimal separator: $separator",
        action = {
            val safeDecimal = sanitizeDecimalSeparator(separator)
            settingsRepository.setDecimalSeparator(safeDecimal)
            if (safeDecimal == thousandsSeparator.value) {
                settingsRepository.setThousandsSeparator(DEFAULT_THOUSANDS_SEPARATOR)
            }
        },
    )

    /**
     * Aggiorna il separatore delle migliaia.
     */
    fun setThousandsSeparator(separator: String) = updatePreference(
        logMsg = "Setting thousands separator: $separator",
        action = {
            val safeThousands = if (separator in SUPPORTED_THOUSANDS_SEPARATORS) {
                separator
            } else {
                DEFAULT_THOUSANDS_SEPARATOR
            }
            settingsRepository.setThousandsSeparator(safeThousands)
        },
    )


    /**
     * Aggiorna la preferenza per la visualizzazione della sezione grafici.
     */
    fun setShowChartsSection(show: Boolean) = updatePreference(
        logMsg = "Setting show charts section: $show",
        action = { settingsRepository.setShowCharts(show) },
    )

    /**
     * Aggiorna il formato data.
     */
    fun setDateFormat(pattern: String) = updatePreference(
        logMsg = "Setting date format: $pattern",
        action = { settingsRepository.setDateFormat(pattern) },
    )

    /**
     * Aggiorna la preferenza per mostrare le note delle transazioni.
     */
    fun setShowTransactionNotes(show: Boolean) = updatePreference(
        logMsg = "Setting show transaction notes: $show",
        action = { settingsRepository.setShowTransactionNotes(show) },
    )

    /**
     * Aggiorna la preferenza per mostrare il breakdown dei pagamenti.
     */
    fun setShowPaymentTypeBreakdown(show: Boolean) = updatePreference(
        logMsg = "Setting show payment type breakdown: $show",
        action = { settingsRepository.setShowPaymentTypeBreakdown(show) },
    )

    /**
     * Aggiorna il tipo di visualizzazione delle transazioni (Home).
     */
    fun setTransactionDisplayType(displayType: TransactionDisplayType) = updatePreference(
        logMsg = "Setting home transaction display type: $displayType",
        action = { settingsRepository.setTransactionDisplayType(displayType) },
    )

    /**
     * Aggiorna il tipo di visualizzazione delle transazioni (Transazioni).
     */
    fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) = updatePreference(
        logMsg = "Setting transactions transaction display type: $displayType",
        action = { settingsRepository.setTransactionsTransactionDisplayType(displayType) },
    )

    /**
     * Ripristina tutte le preferenze ai valori di default.
     */
    fun resetAllPreferences() = updatePreference(
        logMsg = "Resetting all preferences",
        action = { settingsRepository.resetAllPreferences() },
    )

    /**
     * Funzione di utilità per loggare e lanciare l'azione in coroutine.
     */
    private fun updatePreference(logMsg: String, action: suspend () -> Unit) {
        Logger.d(TAG) { logMsg }
        viewModelScope.launch { action() }
    }

    private fun sanitizeCurrencySymbol(symbol: String): String =
        if (symbol in SUPPORTED_CURRENCY_SYMBOLS) symbol else DEFAULT_CURRENCY_SYMBOL

    private fun sanitizeDecimalDigits(digits: Int): Int = digits.coerceIn(0, 4)

    private fun sanitizeDecimalSeparator(separator: String): String =
        if (separator in SUPPORTED_DECIMAL_SEPARATORS) separator else DEFAULT_DECIMAL_SEPARATOR

    private fun sanitizeThousandsSeparator(thousands: String, decimal: String): String {
        val normalized = if (thousands in SUPPORTED_THOUSANDS_SEPARATORS) {
            thousands
        } else {
            DEFAULT_THOUSANDS_SEPARATOR
        }

        return if (normalized == decimal) DEFAULT_THOUSANDS_SEPARATOR else normalized
    }
}
