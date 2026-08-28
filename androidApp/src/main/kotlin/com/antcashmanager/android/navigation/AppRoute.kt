package com.antcashmanager.android.navigation

/**
 * Centralizzato, type-safe route definitions per la navigazione in Compose.
 *
 * Questa sealed class gerarchica riunisce tutte le rotte dell'app in un'unica fonte di verità,
 * evitando string literals sparse e garantendo compile-time type-safety.
 *
 * **Vantaggi**:
 * - ✅ Compile-time type-safety (no typo risk)
 * - ✅ Centralizzato in un file (easy to maintain)
 * - ✅ Hierarchical per feature (Settings, Transactions, etc.)
 * - ✅ Helper methods per rotte con parametri (es: createRoute(id))
 * - ✅ No string duplication
 *
 * **Uso**:
 * ```kotlin
 * navController.navigate(AppRoute.BottomRoute.Home.route)
 * navController.navigate(AppRoute.TransactionRoute.Edit.createRoute(123))
 * ```
 */
sealed class AppRoute {
    // ========== BOTTOM TAB ROUTES ==========
    sealed class BottomRoute(val route: String) {
        data object Home : BottomRoute("home")
        data object Charts : BottomRoute("charts")
        data object Transactions : BottomRoute("transactions")
        data object Categories : BottomRoute("categories")
        data object Settings : BottomRoute("settings")
        data object Tutorial : BottomRoute("tutorial")
    }

    // ========== SETTINGS SUB-ROUTES (Nested Navigation) ==========
    sealed class SettingsRoute(val route: String) {
        data object Main : SettingsRoute("settings")  // Alias for BottomRoute.Settings
        data object Display : SettingsRoute("settings/display")
        data object DataManagement : SettingsRoute("settings/data")
    }

    // ========== TRANSACTION FLOW ROUTES ==========
    sealed class TransactionRoute(val route: String) {
        /**
         * Rotta per aggiungere/modificare una transazione.
         * Supporta parametro opzionale transactionId.
         * - Nuova: naviga con transactionId=-1 o senza parametro
         * - Modifica: naviga con transactionId=ID
         */
        data object Add : TransactionRoute("transactions/add?transactionId={transactionId}") {
            fun createRouteForNew(): String = "transactions/add?transactionId=-1"
            fun createRouteForEdit(transactionId: Long): String = "transactions/add?transactionId=$transactionId"
        }

        data object ReceiptScan : TransactionRoute("transactions/receipt-scan")
    }

    // ========== HELPER COMPANION OBJECT ==========
    companion object {
        /**
         * Lista di tutte le rotte bottom tab per facilmente iterare/filtrare
         */
        fun bottomRoutes(): List<BottomRoute> = listOf(
            BottomRoute.Home,
            BottomRoute.Charts,
            BottomRoute.Transactions,
            BottomRoute.Categories,
            BottomRoute.Settings,
            BottomRoute.Tutorial,
        )

        /**
         * Controlla se una rotta è una rotta bottom tab
         */
        fun isBottomRoute(route: String?): Boolean {
            return route?.let { r ->
                bottomRoutes().any { it.route == r }
            } ?: false
        }

        /**
         * Controlla se una rotta è una rotta Settings (main o sub-screens)
         */
        fun isSettingsRoute(route: String?): Boolean {
            return route?.let { r ->
                r == SettingsRoute.Main.route ||
                        r == SettingsRoute.Display.route ||
                        r == SettingsRoute.DataManagement.route
            } ?: false
        }

        /**
         * Controlla se una rotta è una rotta Transaction
         */
        fun isTransactionRoute(route: String?): Boolean {
            return route?.let { r ->
                r.startsWith("transactions/")
            } ?: false
        }

    }
}
