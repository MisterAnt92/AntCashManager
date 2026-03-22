package com.antcashmanager.android.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Composition-local that signals whether animations should be
 * reduced / disabled for accessibility purposes.
 *
 * Provided by [AntCashManagerTheme] based on the user's
 * "Reduce Motion" setting.
 */
val LocalReduceMotion = staticCompositionLocalOf { false }

