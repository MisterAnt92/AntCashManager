package com.antcashmanager.android.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
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
        titleResId = R.string.nav_home,
        icon = Icons.Default.Home,
    )

    data object Charts : BottomNavItem(
        route = "charts",
        titleResId = R.string.nav_charts,
        icon = Icons.Default.BarChart,
    )

    data object Transactions : BottomNavItem(
        route = "transactions",
        titleResId = R.string.nav_transactions,
        icon = Icons.AutoMirrored.Filled.List,
    )

    data object Categories : BottomNavItem(
        route = "categories",
        titleResId = R.string.nav_categories,
        icon = Icons.Default.Category,
    )

    data object Settings : BottomNavItem(
        route = "settings",
        titleResId = R.string.nav_settings,
        icon = Icons.Default.Settings,
    )
}
