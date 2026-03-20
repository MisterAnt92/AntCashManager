package com.antcashmanager.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import com.antcashmanager.domain.usecase.settings.SetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.SetThemeUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val getThemeUseCase = GetThemeUseCase(settingsRepository)
    private val setThemeUseCase = SetThemeUseCase(settingsRepository)
    private val getLanguageUseCase = GetLanguageUseCase(settingsRepository)
    private val setLanguageUseCase = SetLanguageUseCase(settingsRepository)
    private val deleteAllTransactionsUseCase = DeleteAllTransactionsUseCase(transactionRepository)

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
