package com.antcashmanager.android.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.antcashmanager.android.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
) {
    data object Home : BottomNavItem(
        route = "home",
        titleResId = R.string.common_home,
        icon = Icons.Default.Home,
    )

    data object Charts : BottomNavItem(
        route = "charts",
        titleResId = R.string.common_charts,
        icon = Icons.Default.BarChart,
    )

    data object Transactions : BottomNavItem(
        route = "transactions",
        titleResId = R.string.common_transactions,
        icon = Icons.AutoMirrored.Filled.List,
    )

    data object Categories : BottomNavItem(
        route = "categories",
        titleResId = R.string.common_categories,
        icon = Icons.Default.Category,
    )

    data object Settings : BottomNavItem(
        route = "settings",
        titleResId = R.string.common_settings,
        icon = Icons.Default.Settings,
    )

    data object Tutorial : BottomNavItem(
        route = "tutorial",
        titleResId = R.string.settings_tutorial,
        icon = Icons.Default.Help,
    )
}
