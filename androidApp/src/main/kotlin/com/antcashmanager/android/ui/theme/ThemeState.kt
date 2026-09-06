package com.antcashmanager.android.ui.theme

import com.antcashmanager.domain.model.AppTheme

/**
 * Consolidated UI state for Theme screen.
 *
 * Manages all theme-related preferences:
 * - App theme (light/dark/system)
 * - Accessibility settings (high contrast, large text, reduce motion)
 */
data class ThemeState(
    val appTheme: AppTheme = ThemeConstants.DEFAULT_THEME,
    val highContrast: Boolean = ThemeConstants.DEFAULT_HIGH_CONTRAST,
    val largeText: Boolean = ThemeConstants.DEFAULT_LARGE_TEXT,
    val reduceMotion: Boolean = ThemeConstants.DEFAULT_REDUCE_MOTION,
)
