package com.antcashmanager.android.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for AppRoute utility functions.
 */
class AppRouteTest {
    // ── isSubScreen() tests ──

    @Test
    fun isSubScreen_shouldReturnTrue_forAddTransactionRoute() {
        assertTrue(
            "add_transaction route should be recognized as a sub-screen",
            AppRoute.isSubScreen("add_transaction"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnTrue_forAddTransactionRouteWithParameter() {
        assertTrue(
            "add_transaction route with parameter should be recognized as a sub-screen",
            AppRoute.isSubScreen("add_transaction?transactionId=123"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnTrue_forReceiptScanRoute() {
        assertTrue(
            "receipt_scan route should be recognized as a sub-screen",
            AppRoute.isSubScreen("receipt_scan"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnFalse_forHomeRoute() {
        assertFalse(
            "home route should NOT be a sub-screen",
            AppRoute.isSubScreen("home"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnFalse_forChartsRoute() {
        assertFalse(
            "charts route should NOT be a sub-screen",
            AppRoute.isSubScreen("charts"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnFalse_forTransactionsRoute() {
        assertFalse(
            "transactions route should NOT be a sub-screen",
            AppRoute.isSubScreen("transactions"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnFalse_forSettingsMainRoute() {
        assertFalse(
            "settings main route should NOT be a sub-screen (uses shared NavGraph header)",
            AppRoute.isSubScreen("settings"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnTrue_forDisplayRoute() {
        assertTrue(
            "display route should be a sub-screen (has own TopAppBar + ArrowBack)",
            AppRoute.isSubScreen("display"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnTrue_forSettingsDataRoute() {
        assertTrue(
            "settings_data route should be a sub-screen (has own TopAppBar + ArrowBack)",
            AppRoute.isSubScreen("settings_data"),
        )
    }

    @Test
    fun isSubScreen_shouldReturnFalse_forNull() {
        assertFalse(
            "null route should NOT be a sub-screen",
            AppRoute.isSubScreen(null),
        )
    }

    @Test
    fun isSubScreen_shouldReturnFalse_forEmptyString() {
        assertFalse(
            "empty string should NOT be a sub-screen",
            AppRoute.isSubScreen(""),
        )
    }

    // ── isSettingsRoute() tests (verify no regression) ──

    @Test
    fun isSettingsRoute_shouldReturnTrue_forSettingsMainRoute() {
        assertTrue(
            "settings main route should be recognized",
            AppRoute.isSettingsRoute("settings"),
        )
    }

    @Test
    fun isSettingsRoute_shouldReturnTrue_forDisplayRoute() {
        assertTrue(
            "display route should be recognized as settings",
            AppRoute.isSettingsRoute("display"),
        )
    }

    @Test
    fun isSettingsRoute_shouldReturnTrue_forDataManagementRoute() {
        assertTrue(
            "settings_data route should be recognized",
            AppRoute.isSettingsRoute("settings_data"),
        )
    }

    @Test
    fun isSettingsRoute_shouldReturnFalse_forAddTransactionRoute() {
        assertFalse(
            "add_transaction route should NOT be recognized as settings",
            AppRoute.isSettingsRoute("add_transaction"),
        )
    }

    @Test
    fun isSettingsRoute_shouldReturnFalse_forNull() {
        assertFalse(
            "null route should NOT be recognized as settings",
            AppRoute.isSettingsRoute(null),
        )
    }

    // ── Interaction tests: isSettingsRoute() and isSubScreen() should not conflict ──

    @Test
    fun settingsAndSubScreenDetection_canOverlap_forDisplayAndSettingsData() {
        // Display is BOTH a settings route (isSettingsRoute) AND a sub-screen (has own TopAppBar)
        assertTrue("display should be settings route", AppRoute.isSettingsRoute("display"))
        assertTrue("display should be a sub-screen (has own TopAppBar + ArrowBack)", AppRoute.isSubScreen("display"))

        // settings_data same pattern
        assertTrue("settings_data should be settings route", AppRoute.isSettingsRoute("settings_data"))
        assertTrue("settings_data should be a sub-screen", AppRoute.isSubScreen("settings_data"))

        // Settings MAIN uses shared NavGraph header — settings route but NOT sub-screen
        assertTrue("settings main should be settings route", AppRoute.isSettingsRoute("settings"))
        assertFalse("settings main should NOT be a sub-screen", AppRoute.isSubScreen("settings"))

        // Receipt scan is a sub-screen, not a settings route
        assertTrue("receipt_scan should be sub-screen", AppRoute.isSubScreen("receipt_scan"))
        assertFalse("receipt_scan should NOT be settings", AppRoute.isSettingsRoute("receipt_scan"))
    }
}
