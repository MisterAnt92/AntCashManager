package com.antcashmanager.android.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antcashmanager.android.navigation.AntCashManagerNavHost
import com.antcashmanager.android.test.base.BaseInstrumentationTest
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Robust navigation tests using TestTag selectors and BaseInstrumentationTest.
 *
 * Tests core navigation functionality across app sections:
 * - Bottom navigation bar visibility
 * - Navigation between main sections
 * - Screen content display verification
 *
 * Uses TestTag selectors for stability (not affected by UI text changes).
 *
 * **Tutorial Handling**:
 * Extends [BaseInstrumentationTest] which automatically disables the tutorial overlay
 * by mocking SettingsRepository. This ensures navigation buttons are always accessible.
 *
 * **Benefits of Using BaseInstrumentationTest**:
 * - Tutorial automatically disabled
 * - SettingsRepository mocked with sensible defaults
 * - Common helper methods (getString, getActivity)
 * - Centralized test infrastructure
 * - No test setup boilerplate
 */
@RunWith(AndroidJUnit4::class)
class NavigationRobustTest : BaseInstrumentationTest() {

    @Test
    fun navigation_bottomNavBar_shouldBeVisible() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Verify bottom nav bar is visible
        composeTestRule.onNodeWithTag("bottom_nav_bar")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_shouldNavigateToHome() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Home should be visible by default
        composeTestRule.onNodeWithTag("home_screen")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_shouldNavigateToTransactions() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Click transactions nav button
        composeTestRule.onNodeWithTag("nav_transactions")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify transactions screen is displayed
        composeTestRule.onNodeWithTag("transactions_screen")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_shouldNavigateToCategories() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Click categories nav button
        composeTestRule.onNodeWithTag("nav_categories")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify categories screen is displayed
        composeTestRule.onNodeWithTag("categories_screen")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_shouldNavigateToCharts() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Click charts nav button
        composeTestRule.onNodeWithTag("nav_charts")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify charts screen is displayed
        composeTestRule.onNodeWithTag("charts_screen")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_shouldNavigateToSettings() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Click settings nav button
        composeTestRule.onNodeWithTag("nav_settings")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify settings screen is displayed
        composeTestRule.onNodeWithTag("settings_screen")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_circularNavigation_shouldWorkCorrectly() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate: Home → Transactions → Categories → Settings → Home
        val navSequence = listOf(
            "nav_transactions" to "transactions_screen",
            "nav_categories" to "categories_screen",
            "nav_settings" to "settings_screen",
            "nav_home" to "home_screen"
        )

        for ((navButton, screenTag) in navSequence) {
            composeTestRule.onNodeWithTag(navButton)
                .performClick()

            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag(screenTag)
                .assertIsDisplayed()
        }
    }

    @Test
    fun navigation_allNavButtons_shouldBeDisplayed() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Verify all navigation buttons are visible
        val navButtons = listOf(
            "nav_home",
            "nav_transactions",
            "nav_categories",
            "nav_charts",
            "nav_settings"
        )

        for (navButton in navButtons) {
            composeTestRule.onNodeWithTag(navButton)
                .assertIsDisplayed()
        }
    }
}
