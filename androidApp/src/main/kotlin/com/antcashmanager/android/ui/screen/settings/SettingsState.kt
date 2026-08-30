package com.antcashmanager.android.ui.screen.settings

import com.antcashmanager.android.ui.base.ErrorState
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.TransactionDisplayType

/**
 * Stato UI completo per la schermata delle impostazioni.
 *
 * Consolidates all user preferences and settings:
 * - Theme & Language: theme, language
 * - Display: showCharts, highContrast, largeText, reduceMotion
 * - Number Formatting: currencySymbol, decimalDigits, decimalSeparator, thousandsSeparator
 * - Transactions: showTransactionNotes, transactionDisplayType
 * - Error handling: errorState
 *
 * UDF Pattern:
 * - state: SettingsState (this)
 * - events: SettingEvent (sealed class)
 * - viewModel: SettingsViewModel (onEvent handler)
 */
data class SettingsState(
    // Theme & Language
    val theme: AppTheme = AppTheme.LIGHT,
    val language: AppLanguage = AppLanguage.ENGLISH,
    
    // Display Preferences
    val showCharts: Boolean = true,
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val reduceMotion: Boolean = false,
    
    // Number Formatting
    val currencySymbol: String = "€",
    val decimalDigits: Int = 2,
    val decimalSeparator: String = ".",
    val thousandsSeparator: String = ",",
    
    // Transaction Display
    val showTransactionNotes: Boolean = true,
    val transactionDisplayType: TransactionDisplayType = TransactionDisplayType.CATEGORY,
    
    // Error handling & loading
    val errorState: ErrorState = ErrorState(),
    val isLoading: Boolean = false,
)
