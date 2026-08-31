package com.antcashmanager.android.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Extension function per navigare a una rotta bottom tab con corretto back stack handling.
 *
 * Implementa il pattern standard per bottom navigation:
 * - popUpTo: ritorna al destination principale
 * - saveState: salva lo stato del back stack
 * - launchSingleTop: evita duplicati nella stack
 * - restoreState: ripristina lo stato precedente
 *
 * @param route la rotta di destinazione (es. AppRoute.BottomRoute.Home.route)
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToBottomTab(AppRoute.BottomRoute.Transactions.route)
 * ```
 */
fun NavController.navigateToBottomTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Extension function per navigare a una rotta Settings con corretto back stack handling.
 *
 * Simile a navigateToBottomTab, ma specifico per la Settings nested graph.
 *
 * @param route la rotta di destinazione (es. AppRoute.SettingsRoute.Display.route)
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToSettings(AppRoute.SettingsRoute.Display.route)
 * ```
 */
fun NavController.navigateToSettings(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

/**
 * Extension function per navigare a una rotta Transaction con gestione di parametri.
 *
 * @param route la rotta di destinazione (può includere parametri)
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToTransaction(AppRoute.TransactionRoute.Add.createRouteForEdit(123))
 * ```
 */
fun NavController.navigateToTransaction(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

/**
 * Extension function per popolare il back stack in modo sicuro.
 *
 * Utile quando l'operazione di pop potrebbe fallire (stack vuoto, etc).
 *
 * @return true se il pop è riuscito, false se il back stack era vuoto
 */
fun NavController.safePopBackStack(): Boolean {
    return if (previousBackStackEntry != null) {
        popBackStack()
        true
    } else {
        false
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TYPE-SAFE NAVIGATION FUNCTIONS (FASE 7b Enhancement)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Type-safe navigation to Home screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToHome()
 * ```
 */
fun NavController.navigateToHome() {
    navigateToBottomTab(AppRoute.BottomRoute.Home.route)
}

/**
 * Type-safe navigation to Charts screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToCharts()
 * ```
 */
fun NavController.navigateToCharts() {
    navigateToBottomTab(AppRoute.BottomRoute.Charts.route)
}

/**
 * Type-safe navigation to Transactions screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToTransactions()
 * ```
 */
fun NavController.navigateToTransactions() {
    navigateToBottomTab(AppRoute.BottomRoute.Transactions.route)
}

/**
 * Type-safe navigation to Categories screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToCategories()
 * ```
 */
fun NavController.navigateToCategories() {
    navigateToBottomTab(AppRoute.BottomRoute.Categories.route)
}

/**
 * Type-safe navigation to Settings main screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToSettings()
 * ```
 */
fun NavController.navigateToSettingsMain() {
    navigateToSettings(AppRoute.SettingsRoute.Main.route)
}

/**
 * Type-safe navigation to Display preferences screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToDisplaySettings()
 * ```
 */
fun NavController.navigateToDisplaySettings() {
    navigateToSettings(AppRoute.SettingsRoute.Display.route)
}

/**
 * Type-safe navigation to Data Management screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToDataManagement()
 * ```
 */
fun NavController.navigateToDataManagement() {
    navigateToSettings(AppRoute.SettingsRoute.DataManagement.route)
}

/**
 * Type-safe navigation to Tutorial screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToTutorial()
 * ```
 */
fun NavController.navigateToTutorial() {
    navigateToBottomTab(AppRoute.BottomRoute.Tutorial.route)
}

/**
 * Type-safe navigation to Receipt Scan screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToReceiptScan()
 * ```
 */
fun NavController.navigateToReceiptScan() {
    navigateToTransaction(AppRoute.TransactionRoute.ReceiptScan.route)
}

/**
 * Type-safe navigation to Add Transaction screen.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToAddTransaction()
 * ```
 */
fun NavController.navigateToAddTransaction() {
    navigateToTransaction(AppRoute.TransactionRoute.Add.route)
}

/**
 * Type-safe navigation to Edit Transaction screen with transaction ID.
 *
 * **Uso:**
 * ```kotlin
 * navController.navigateToEditTransaction(transactionId = 123L)
 * ```
 *
 * @param transactionId ID della transazione da editare
 */
fun NavController.navigateToEditTransaction(transactionId: Long) {
    navigateToTransaction(AppRoute.TransactionRoute.Edit.createRoute(transactionId))
}
