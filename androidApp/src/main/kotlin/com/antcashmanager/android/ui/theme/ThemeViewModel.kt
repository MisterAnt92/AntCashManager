package com.antcashmanager.android.ui.theme

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.ui.base.BaseViewModel
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
) : BaseViewModel<ThemeEvent>() {

    val appTheme = settingsRepository.getTheme()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(ThemeConstants.SHARING_TIMEOUT),
            ThemeConstants.DEFAULT_THEME,
        )

    val highContrast = settingsRepository.getHighContrast()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(ThemeConstants.SHARING_TIMEOUT),
            ThemeConstants.DEFAULT_HIGH_CONTRAST,
        )

    val largeText = settingsRepository.getLargeText()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(ThemeConstants.SHARING_TIMEOUT),
            ThemeConstants.DEFAULT_LARGE_TEXT,
        )

    val reduceMotion = settingsRepository.getReduceMotion()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(ThemeConstants.SHARING_TIMEOUT),
            ThemeConstants.DEFAULT_REDUCE_MOTION,
        )

    override fun onEvent(event: ThemeEvent) {
        logDebug("Event: $event")
        when (event) {
            is ThemeEvent.SetTheme -> setTheme(event.theme)
            is ThemeEvent.RetryLastOperation -> logInfo("Retry requested")
        }
    }

    private fun setTheme(theme: AppTheme) = updatePreference("Setting app theme: $theme") {
        settingsRepository.setTheme(theme)
    }

    private fun updatePreference(logMsg: String, action: suspend () -> Unit) {
        logDebug(logMsg)
        viewModelScope.launch { action() }
    }
}

