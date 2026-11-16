package com.antcashmanager.android.ui.theme

import com.antcashmanager.architecture.UIEvent

import com.antcashmanager.domain.model.AppTheme

/**
 * UDF Pattern: Events for Theme management.
 */
sealed class ThemeEvent : UIEvent() {
    data class SetThemeEvent(
        val theme: AppTheme,
    ) : ThemeEvent()

    data object RetryLastOperationEvent : ThemeEvent()
}
