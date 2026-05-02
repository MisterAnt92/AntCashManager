package com.antcashmanager.android.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel che centralizza l'accesso alle preferenze di tema e accessibilità.
 * Espone gli state necessari per impostare il tema dell'app e fornisce
 * metodi per aggiornare la preferenza di tema.
 */
class ThemeViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "ThemeViewModel"
        private const val SHARING_TIMEOUT = 5_000L
    }

    val appTheme = settingsRepository.getTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT), AppTheme.SYSTEM)

    val highContrast = settingsRepository.getHighContrast()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT), false)

    val largeText = settingsRepository.getLargeText()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT), false)

    val reduceMotion = settingsRepository.getReduceMotion()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT), false)

    fun setTheme(theme: AppTheme) = updatePreference("Setting app theme: $theme") {
        settingsRepository.setTheme(theme)
    }

    private fun updatePreference(logMsg: String, action: suspend () -> Unit) {
        Logger.d(TAG) { logMsg }
        viewModelScope.launch { action() }
    }
}

