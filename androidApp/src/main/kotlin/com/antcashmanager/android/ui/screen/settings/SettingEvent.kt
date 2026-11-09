package com.antcashmanager.android.ui.screen.settings

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
    data class SetThemeEvent(
        val theme: AppTheme,
    ) : SettingEvent()

    data class SetLanguageEvent(
        val language: AppLanguage,
    ) : SettingEvent()

    // Display Preferences
    data class SetShowChartsEvent(
        val show: Boolean,
    ) : SettingEvent()

    data class SetHighContrastEvent(
        val enabled: Boolean,
    ) : SettingEvent()

    data class SetLargeTextEvent(
        val enabled: Boolean,
    ) : SettingEvent()

    data class SetReduceMotionEvent(
        val enabled: Boolean,
    ) : SettingEvent()

    // Number Formatting
    data class SetCurrencySymbolEvent(
        val symbol: String,
    ) : SettingEvent()

    data class SetDecimalDigitsEvent(
        val digits: Int,
    ) : SettingEvent()

    data class SetDecimalSeparatorEvent(
        val separator: String,
    ) : SettingEvent()

    data class SetThousandsSeparatorEvent(
        val separator: String,
    ) : SettingEvent()

    // Transaction Display
    data class SetTransactionDisplayTypeEvent(
        val displayType: TransactionDisplayType,
    ) : SettingEvent()

    data class SetTutorialCompletedEvent(
        val completed: Boolean,
    ) : SettingEvent()

    // Utilities
    data object ResetAllPreferencesEvent : SettingEvent()

    data object ImportDebugDataEvent : SettingEvent()

    data class SendFeedbackEmailEvent(
        val emailBody: String,
    ) : SettingEvent()

    data object RetryLastOperationEvent : SettingEvent()

    // Analytics / Privacy
    data class SetAnalyticsConsentEvent(
        val granted: Boolean,
    ) : SettingEvent()
}
