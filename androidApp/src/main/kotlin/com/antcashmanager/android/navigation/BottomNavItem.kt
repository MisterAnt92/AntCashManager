package com.antcashmanager.android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    data object Home : BottomNavItem(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home,
    )

    data object Charts : BottomNavItem(
        route = "charts",
        title = "Charts",
        icon = Icons.Default.BarChart,
    )

    data object Transactions : BottomNavItem(
        route = "transactions",
        title = "Transactions",
        icon = Icons.AutoMirrored.Filled.List,
    )

    data object Categories : BottomNavItem(
        route = "categories",
        title = "Categories",
        icon = Icons.Default.Category,
    )

    data object Settings : BottomNavItem(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings,
    )
}
