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

/**
 * Configuration per gli item della bottom navigation bar / navigation rail.
 *
 * Questa sealed class mappa le rotte di navigazione (AppRoute.BottomRoute) con i loro
 * metadati di presentazione (icon, label, etc.) per la UI.
 *
 * **Note**: Le rotte stesse sono definite in AppRoute.kt.
 * Questo file contiene solo la configurazione visuale.
 */
sealed class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
) {
    data object Home : BottomNavItem(
        route = AppRoute.BottomRoute.Home.route,
        titleResId = R.string.common_home,
        icon = Icons.Default.Home,
    )

    data object Charts : BottomNavItem(
        route = AppRoute.BottomRoute.Charts.route,
        titleResId = R.string.common_charts,
        icon = Icons.Default.BarChart,
    )

    data object Transactions : BottomNavItem(
        route = AppRoute.BottomRoute.Transactions.route,
        titleResId = R.string.common_transactions,
        icon = Icons.AutoMirrored.Filled.List,
    )

    data object Categories : BottomNavItem(
        route = AppRoute.BottomRoute.Categories.route,
        titleResId = R.string.common_categories,
        icon = Icons.Default.Category,
    )

    data object Settings : BottomNavItem(
        route = AppRoute.BottomRoute.Settings.route,
        titleResId = R.string.common_settings,
        icon = Icons.Default.Settings,
    )

    data object Tutorial : BottomNavItem(
        route = AppRoute.BottomRoute.Tutorial.route,
        titleResId = R.string.settings_tutorial,
        icon = Icons.Default.Help,
    )
}
