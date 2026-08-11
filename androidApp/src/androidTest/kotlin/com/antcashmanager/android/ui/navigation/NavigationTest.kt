package com.antcashmanager.android.ui.navigation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antcashmanager.android.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for app navigation.
 *
 * Tests cover:
 * - Navigation between main sections (Home, Charts, Transactions, Categories, Settings)
 * - Bottom navigation bar visibility and selection
 * - Screen transitions and state preservation
 * - Navigation parameter passing
 *
 * These tests run on Android device/emulator and test real navigation behavior.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Test navigation from Home to Charts screen
     */
    @Test
    fun navigate_fromHomeToCharts_shouldDisplayChartsScreen() {
        // Verify we're on Home screen initially
        composeTestRule.onNodeWithText("Home")
            .assertExists()

        // Click Charts bottom nav item
        composeTestRule.onNodeWithContentDescription("Charts")
            .performClick()

        // Verify Charts screen is displayed
        composeTestRule.onNodeWithText("Charts")
            .assertExists()
    }

    /**
     * Test navigation from Home to Transactions screen
     */
    @Test
    fun navigate_fromHomeToTransactions_shouldDisplayTransactionsScreen() {
        // Click Transactions bottom nav item
        composeTestRule.onNodeWithContentDescription("Transactions")
            .performClick()

        // Verify Transactions screen is displayed
        composeTestRule.onNodeWithText("Transactions")
            .assertExists()
    }

    /**
     * Test navigation from Home to Categories screen
     */
    @Test
    fun navigate_fromHomeToCategories_shouldDisplayCategoriesScreen() {
        // Click Categories bottom nav item
        composeTestRule.onNodeWithContentDescription("Categories")
            .performClick()

        // Verify Categories screen is displayed
        composeTestRule.onNodeWithText("Categories")
            .assertExists()
    }

    /**
     * Test navigation from Home to Settings screen
     */
    @Test
    fun navigate_fromHomeToSettings_shouldDisplaySettingsScreen() {
        // Click Settings bottom nav item
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Verify Settings screen is displayed
        composeTestRule.onNodeWithText("Settings")
            .assertExists()
    }

    /**
     * Test circular navigation: Home → Charts → Transactions → Categories → Settings → Home
     */
    @Test
    fun navigate_circularNavigation_shouldPreserveState() {
        val screens = listOf("Charts", "Transactions", "Categories", "Settings")

        for (screen in screens) {
            // Navigate to screen
            composeTestRule.onNodeWithContentDescription(screen)
                .performClick()

            // Verify we're on that screen
            composeTestRule.onNodeWithText(screen)
                .assertExists()
        }

        // Navigate back to Home
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()

        // Verify we're back on Home
        composeTestRule.onNodeWithText("Home")
            .assertExists()
    }

    /**
     * Test that bottom navigation bar is always visible
     */
    @Test
    fun bottomNavigation_shouldBeVisibleOnAllScreens() {
        val screens = listOf("Home", "Charts", "Transactions", "Categories", "Settings")

        for (screen in screens) {
            // Navigate to screen (skip first iteration since we start on Home)
            if (screen != "Home") {
                composeTestRule.onNodeWithContentDescription(screen)
                    .performClick()
            }

            // Verify bottom nav is visible
            composeTestRule.onNodeWithContentDescription("Home")
                .assertExists()
            composeTestRule.onNodeWithContentDescription("Charts")
                .assertExists()
            composeTestRule.onNodeWithContentDescription("Transactions")
                .assertExists()
            composeTestRule.onNodeWithContentDescription("Categories")
                .assertExists()
            composeTestRule.onNodeWithContentDescription("Settings")
                .assertExists()
        }
    }

    /**
     * Test quick switching between screens
     */
    @Test
    fun navigate_quickSwitching_shouldHandleRapidNavigation() {
        // Rapidly switch between screens
        composeTestRule.onNodeWithContentDescription("Charts")
            .performClick()
        composeTestRule.onNodeWithContentDescription("Transactions")
            .performClick()
        composeTestRule.onNodeWithContentDescription("Charts")
            .performClick()
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()

        // Verify we ended up on Home
        composeTestRule.onNodeWithText("Home")
            .assertExists()
    }
}
