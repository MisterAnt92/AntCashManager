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
