package com.antcashmanager.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import com.antcashmanager.domain.usecase.settings.SetThemeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(settingsRepository: SettingsRepository) : ViewModel() {

    private val getThemeUseCase = GetThemeUseCase(settingsRepository)
    private val setThemeUseCase = SetThemeUseCase(settingsRepository)

    val theme = getThemeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM
        )

    fun setTheme(theme: AppTheme) {
        Logger.d("SettingsViewModel") { "Setting theme to: $theme" }
        viewModelScope.launch {
            setThemeUseCase(theme)
        }
    }
}
