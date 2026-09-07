package com.antcashmanager.android.ui.theme

import com.antcashmanager.domain.model.AppTheme

/**
 * UDF Pattern: Events for Theme management.
 */
sealed class ThemeEvent {
    data class SetTheme(
        val theme: AppTheme,
    ) : ThemeEvent()

    data object RetryLastOperation : ThemeEvent()
}
