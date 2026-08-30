package com.antcashmanager.android.navigation

/**
 * Centralized route definitions for type-safe navigation.
 * All string-literal navigations should reference these sealed class routes.
 *
 * Pattern:
 * ```
 * navController?.navigate(AppRoute.TransactionRoute.Add.route)
 * navController?.navigate(AppRoute.TransactionRoute.Edit.createRoute(id))
 * ```
 */
sealed class AppRoute(val route: String) {

    // Transaction routes
    sealed class TransactionRoute(route: String) : AppRoute(route) {
        object List : TransactionRoute("transactions")
        object Add : TransactionRoute("add_transaction")
        object Edit : TransactionRoute("add_transaction") {
            fun createRoute(transactionId: Long) = "add_transaction?transactionId=$transactionId"
        }
        object ReceiptScan : TransactionRoute("receipt_scan")
    }

    // Settings routes
    sealed class SettingsRoute(route: String) : AppRoute(route) {
        object Main : SettingsRoute("settings")
        object Display : SettingsRoute("display")
        object DataManagement : SettingsRoute("settings_data")
    }

    // Chart routes
    sealed class ChartRoute(route: String) : AppRoute(route) {
        object Charts : ChartRoute("charts")
    }

    // Category routes
    sealed class CategoryRoute(route: String) : AppRoute(route) {
        object Categories : CategoryRoute("categories")
    }

    // Home routes
    sealed class HomeRoute(route: String) : AppRoute(route) {
        object Home : HomeRoute("home")
    }

    // Tutorial routes
    sealed class TutorialRoute(route: String) : AppRoute(route) {
        object Tutorial : TutorialRoute("tutorial")
    }

    // Receipt Scan routes
    sealed class ReceiptRoute(route: String) : AppRoute(route) {
        object ReceiptScan : ReceiptRoute("receipt_scan")
    }
}
