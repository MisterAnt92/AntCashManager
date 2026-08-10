package com.antcashmanager.android.navigation

import androidx.compose.runtime.Composable

/**
 * Configuration for the unified screen header (top bar with hamburger menu).
 *
 * @param title The title to display in the header
 * @param actions Composable lambda for action buttons (Search, Filter, Help, etc.)
 */
data class ScreenHeaderConfig(
    val title: String = "",
    val actions: (@Composable () -> Unit)? = null,
)
