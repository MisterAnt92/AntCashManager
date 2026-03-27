package com.antcashmanager.android.ui.screen.home.settings.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DisplayViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val currencySymbol = settingsRepository.getCurrencySymbol()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "\u20ac")

    val decimalDigits = settingsRepository.getDecimalDigits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2)

    val decimalSeparator = settingsRepository.getDecimalSeparator()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ",")

    val thousandsSeparator = settingsRepository.getThousandsSeparator()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ".")

    val showTransactionNotes = settingsRepository.getShowTransactionNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setCurrencySymbol(symbol: String) {
        Logger.d("DisplayViewModel") { "Setting currency symbol: $symbol" }
        viewModelScope.launch { settingsRepository.setCurrencySymbol(symbol) }
    }

    fun setDecimalDigits(digits: Int) {
        Logger.d("DisplayViewModel") { "Setting decimal digits: $digits" }
        viewModelScope.launch { settingsRepository.setDecimalDigits(digits) }
    }

    fun setDecimalSeparator(separator: String) {
        Logger.d("DisplayViewModel") { "Setting decimal separator: $separator" }
        viewModelScope.launch { settingsRepository.setDecimalSeparator(separator) }
    }

    fun setThousandsSeparator(separator: String) {
        Logger.d("DisplayViewModel") { "Setting thousands separator: $separator" }
        viewModelScope.launch { settingsRepository.setThousandsSeparator(separator) }
    }

    fun setShowTransactionNotes(show: Boolean) {
        Logger.d("DisplayViewModel") { "Setting show transaction notes: $show" }
        viewModelScope.launch { settingsRepository.setShowTransactionNotes(show) }
    }

    fun resetAllPreferences() {
        Logger.d("DisplayViewModel") { "Resetting all preferences" }
        viewModelScope.launch { settingsRepository.resetAllPreferences() }
    }
}
