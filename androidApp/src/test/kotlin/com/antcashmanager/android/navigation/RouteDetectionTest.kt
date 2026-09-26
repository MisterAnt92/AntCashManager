package com.antcashmanager.android.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for route detection logic used in NavGraph.
 * Verifies that isSubScreen() and isSettingsRoute() work correctly
 * when used in navigation conditions.
 */
class RouteDetectionTest {
    // ── isSubScreen() detection tests ──

    @Test
    fun currentRoute_shouldBeDetectedAsSubScreen_whenAddTransactionIsActive() {
        val currentRoute = "add_transaction"
        assertTrue(
            "current route add_transaction should be detected as sub-screen",
            AppRoute.isSubScreen(currentRoute),
        )
    }

    @Test
    fun currentRoute_shouldBeDetectedAsSubScreen_whenReceiptScanIsActive() {
        val currentRoute = "receipt_scan"
        assertTrue(
            "current route receipt_scan should be detected as sub-screen",
            AppRoute.isSubScreen(currentRoute),
        )
    }

    @Test
    fun currentRoute_shouldNotBeDetectedAsSubScreen_whenHomeIsActive() {
        val currentRoute = "home"
        assertFalse(
            "current route home should NOT be detected as sub-screen",
            AppRoute.isSubScreen(currentRoute),
        )
    }

    @Test
    fun currentRoute_shouldNotBeDetectedAsSubScreen_whenTransactionsIsActive() {
        val currentRoute = "transactions"
        assertFalse(
            "current route transactions should NOT be detected as sub-screen",
            AppRoute.isSubScreen(currentRoute),
        )
    }

    // ── Bottom bar visibility condition tests ──

    @Test
    fun bottomBar_shouldBeHidden_whenIsOnSubScreenIsTrue() {
        val isTutorialCompleted = true
        val preferRailNavigation = false // phone
        val isSidebarOpen = false
        val isOnCategoriesSettingsOrTutorial = false
        val isOnSubScreen = AppRoute.isSubScreen("add_transaction")

        val shouldShowBottomBar =
            isTutorialCompleted &&
                !preferRailNavigation &&
                !isSidebarOpen &&
                !isOnCategoriesSettingsOrTutorial &&
                !isOnSubScreen

        assertFalse(
            "bottom bar should be hidden when on sub-screen",
            shouldShowBottomBar,
        )
    }

    @Test
    fun bottomBar_shouldBeVisible_whenOnMainScreenAndNotInSubScreen() {
        val isTutorialCompleted = true
        val preferRailNavigation = false // phone
        val isSidebarOpen = false
        val isOnCategoriesSettingsOrTutorial = false
        val isOnSubScreen = AppRoute.isSubScreen("home")

        val shouldShowBottomBar =
            isTutorialCompleted &&
                !preferRailNavigation &&
                !isSidebarOpen &&
                !isOnCategoriesSettingsOrTutorial &&
                !isOnSubScreen

        assertTrue(
            "bottom bar should be visible when on main screen",
            shouldShowBottomBar,
        )
    }

    @Test
    fun bottomBar_shouldBeHidden_whenOnSettingsScreen() {
        val isTutorialCompleted = true
        val preferRailNavigation = false // phone
        val isSidebarOpen = false
        val isOnSettingsScreen = AppRoute.isSettingsRoute("display")
        val isOnTutorial = false
        val isOnCategoriesSettingsOrTutorial =
            "display" == AppRoute.BottomRoute.Categories.route ||
                AppRoute.isSettingsRoute("display") ||
                "display" == AppRoute.BottomRoute.Tutorial.route
        val isOnSubScreen = AppRoute.isSubScreen("display")

        val shouldShowBottomBar =
            isTutorialCompleted &&
                !preferRailNavigation &&
                !isSidebarOpen &&
                !isOnCategoriesSettingsOrTutorial &&
                !isOnSubScreen

        assertFalse(
            "bottom bar should be hidden when on settings screen",
            shouldShowBottomBar,
        )
    }

    // ── Top bar visibility condition tests ──

    @Test
    fun topBar_shouldBeHidden_whenIsOnSubScreen() {
        val isOnTutorial = false
        val isOnSubScreen = AppRoute.isSubScreen("receipt_scan")

        val shouldShowTopBar = !isOnTutorial && !isOnSubScreen

        assertFalse(
            "top bar should be hidden when on sub-screen",
            shouldShowTopBar,
        )
    }

    @Test
    fun topBar_shouldBeVisible_whenOnMainScreen() {
        val isOnTutorial = false
        val isOnSubScreen = AppRoute.isSubScreen("home")

        val shouldShowTopBar = !isOnTutorial && !isOnSubScreen

        assertTrue(
            "top bar should be visible when on main screen",
            shouldShowTopBar,
        )
    }

    @Test
    fun topBar_shouldBeHidden_whenOnTutorial() {
        val isOnTutorial = true
        val isOnSubScreen = false

        val shouldShowTopBar = !isOnTutorial && !isOnSubScreen

        assertFalse(
            "top bar should be hidden when on tutorial",
            shouldShowTopBar,
        )
    }

    // ── Integration: both functions should work together without conflict ──

    @Test
    fun routeDetection_shouldCorrectlyClassifyAllMainRoutes() {
        // Main navigation routes should NOT be sub-screens
        assertFalse(AppRoute.isSubScreen("home"))
        assertFalse(AppRoute.isSubScreen("charts"))
        assertFalse(AppRoute.isSubScreen("transactions"))
        assertFalse(AppRoute.isSubScreen("categories"))

        // Settings MAIN uses shared NavGraph header → NOT a sub-screen
        assertFalse(AppRoute.isSubScreen("settings"))
    }

    @Test
    fun routeDetection_shouldCorrectlyClassifyAllSubScreens() {
        // Transaction routes are sub-screens (no global header)
        assertTrue(AppRoute.isSubScreen("add_transaction"))
        assertTrue(AppRoute.isSubScreen("add_transaction?transactionId=123"))
        assertTrue(AppRoute.isSubScreen("receipt_scan"))

        // Display and SettingsData are sub-screens (have own TopAppBar + ArrowBack)
        assertTrue(AppRoute.isSubScreen("display"))
        assertTrue(AppRoute.isSubScreen("settings_data"))

        // Transaction sub-screens are NOT settings routes
        assertFalse(AppRoute.isSettingsRoute("add_transaction"))
        assertFalse(AppRoute.isSettingsRoute("receipt_scan"))
    }
}
