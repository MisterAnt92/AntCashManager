package com.antcashmanager.android.ui.screen.home.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme

/**
 * Stato UI per la schermata delle impostazioni.
 */
data class SettingsState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val showCharts: Boolean = true,
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val reduceMotion: Boolean = false,
    val currencySymbol: String = "\u20ac",
    val decimalDigits: Int = 2,
    val decimalSeparator: String = ",",
    val thousandsSeparator: String = ".",
    val showTransactionNotes: Boolean = true,
)

