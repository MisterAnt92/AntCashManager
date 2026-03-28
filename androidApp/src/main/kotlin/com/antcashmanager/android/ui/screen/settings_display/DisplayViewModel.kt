package com.antcashmanager.android.ui.screen.settings_display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
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
        private const val DEFAULT_CURRENCY_SYMBOL = "\u20ac"
        private const val DEFAULT_DECIMAL_DIGITS = 2
        private const val DEFAULT_DECIMAL_SEPARATOR = ","
        private const val DEFAULT_THOUSANDS_SEPARATOR = "."
        private const val DEFAULT_SHOW_TRANSACTION_NOTES = true
        private const val SHARING_TIMEOUT = 5_000L
    }

    // Espone il simbolo valuta attuale
    val currencySymbol = settingsRepository.getCurrencySymbol()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_CURRENCY_SYMBOL,
        )

    // Espone il numero di cifre decimali
    val decimalDigits = settingsRepository.getDecimalDigits()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_DECIMAL_DIGITS,
        )

    // Espone il separatore decimale
    val decimalSeparator = settingsRepository.getDecimalSeparator()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_DECIMAL_SEPARATOR,
        )

    // Espone il separatore delle migliaia
    val thousandsSeparator = settingsRepository.getThousandsSeparator()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_THOUSANDS_SEPARATOR,
        )

    // Espone la preferenza per la visualizzazione delle note
    val showTransactionNotes = settingsRepository.getShowTransactionNotes()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
            DEFAULT_SHOW_TRANSACTION_NOTES,
        )

    /**
     * Aggiorna il simbolo valuta.
     */
    fun setCurrencySymbol(symbol: String) = updatePreference(
        logMsg = "Setting currency symbol: $symbol",
        action = { settingsRepository.setCurrencySymbol(symbol) },
    )

    /**
     * Aggiorna il numero di cifre decimali.
     */
    fun setDecimalDigits(digits: Int) = updatePreference(
        logMsg = "Setting decimal digits: $digits",
        action = { settingsRepository.setDecimalDigits(digits) },
    )

    /**
     * Aggiorna il separatore decimale.
     */
    fun setDecimalSeparator(separator: String) = updatePreference(
        logMsg = "Setting decimal separator: $separator",
        action = { settingsRepository.setDecimalSeparator(separator) },
    )

    /**
     * Aggiorna il separatore delle migliaia.
     */
    fun setThousandsSeparator(separator: String) = updatePreference(
        logMsg = "Setting thousands separator: $separator",
        action = { settingsRepository.setThousandsSeparator(separator) },
    )

    /**
     * Aggiorna la preferenza per la visualizzazione delle note.
     */
    fun setShowTransactionNotes(show: Boolean) = updatePreference(
        logMsg = "Setting show transaction notes: $show",
        action = { settingsRepository.setShowTransactionNotes(show) },
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
}
