package com.antcashmanager.android.navigation

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal for providing the screen header configuration callback to all screens.
 */
val LocalScreenHeaderConfigCallback =
    compositionLocalOf<((ScreenHeaderConfig) -> Unit)?> {
        null
    }
