package com.antcashmanager.android.ui.screen.settings

import android.content.Context
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.TransactionDisplayType

/**
 * UDF Pattern: Events for Settings screen.
 *
 * All preference changes and settings operations emit events
 * that the ViewModel processes to persist and update state.
 */
sealed class SettingEvent {
    // Theme & Language
    data class SetTheme(
        val theme: AppTheme,
    ) : SettingEvent()

    data class SetLanguage(
        val language: AppLanguage,
    ) : SettingEvent()

    // Display Preferences
    data class SetShowCharts(
        val show: Boolean,
    ) : SettingEvent()

    data class SetHighContrast(
        val enabled: Boolean,
    ) : SettingEvent()

    data class SetLargeText(
        val enabled: Boolean,
    ) : SettingEvent()

    data class SetReduceMotion(
        val enabled: Boolean,
    ) : SettingEvent()

    // Number Formatting
    data class SetCurrencySymbol(
        val symbol: String,
    ) : SettingEvent()

    data class SetDecimalDigits(
        val digits: Int,
    ) : SettingEvent()

    data class SetDecimalSeparator(
        val separator: String,
    ) : SettingEvent()

    data class SetThousandsSeparator(
        val separator: String,
    ) : SettingEvent()

    // Transaction Display
    data class SetTransactionDisplayType(
        val displayType: TransactionDisplayType,
    ) : SettingEvent()

    data class SetTutorialCompleted(
        val completed: Boolean,
    ) : SettingEvent()

    // Utilities
    data object ResetAllPreferences : SettingEvent()

    data class ImportDebugData(
        val context: Context,
    ) : SettingEvent()

    data class SendFeedbackEmail(
        val emailBody: String,
        val context: Context,
    ) : SettingEvent()

    data object RetryLastOperation : SettingEvent()
}
