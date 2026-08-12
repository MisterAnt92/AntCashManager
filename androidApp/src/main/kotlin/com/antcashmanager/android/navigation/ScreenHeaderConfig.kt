package com.antcashmanager.android.navigation

import androidx.compose.runtime.Composable

/**
 * Configuration for the unified screen header (top bar with hamburger menu).
 *
 * @param title The title to display in the header
 * @param actions Composable lambda for action buttons (Search, Filter, Help, etc.)
 * @param filterCount Number of active filters (for badge display)
 * @param hasOrderOption If true, shows sort/order icon in header
 * @param showSearchIcon If true, shows search icon in header
 * @param onFilterClick Callback when filter icon is clicked
 * @param onOrderClick Callback when order icon is clicked
 * @param onSearchClick Callback when search icon is clicked
 */
data class ScreenHeaderConfig(
    val title: String = "",
    val actions: (@Composable () -> Unit)? = null,
    val filterCount: Int = 0,
    val hasOrderOption: Boolean = false,
    val showSearchIcon: Boolean = false,
    val onFilterClick: (() -> Unit)? = null,
    val onOrderClick: (() -> Unit)? = null,
    val onSearchClick: (() -> Unit)? = null,
)
