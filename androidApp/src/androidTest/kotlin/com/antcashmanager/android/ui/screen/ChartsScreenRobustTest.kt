package com.antcashmanager.android.ui.screen

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
 * Robust tests for Charts Screen using TestTag selectors.
 *
 * Tests:
 * - Chart display
 * - Chart types (pie, bar, etc.)
 * - Period selection
 * - Category breakdown
 *
 * Extends [BaseInstrumentationTest] which automatically:
 * - Disables the tutorial overlay
 * - Mocks SettingsRepository with defaults
 * - Provides common test helpers
 *
 * Pattern: Uses stable TestTag selectors for reliability
 */
@RunWith(AndroidJUnit4::class)
class ChartsScreenRobustTest : BaseInstrumentationTest() {
    @Test
    fun chartsScreen_shouldBeNavigable() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to charts
        composeTestRule
            .onNodeWithTag("nav_charts")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify charts screen is displayed
        composeTestRule
            .onNodeWithTag("charts_screen")
            .assertIsDisplayed()
    }

    @Test
    fun chartsScreen_shouldDisplayPieChart() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to charts
        composeTestRule
            .onNodeWithTag("nav_charts")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify pie chart is visible
        composeTestRule
            .onNodeWithTag("pie_chart")
            .assertIsDisplayed()
    }

    @Test
    fun chartsScreen_shouldDisplayPeriodSelector() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to charts
        composeTestRule
            .onNodeWithTag("nav_charts")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify period selector is visible
        composeTestRule
            .onNodeWithTag("period_selector")
            .assertIsDisplayed()
    }

    @Test
    fun chartsScreen_shouldDisplayCategoryBreakdown() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to charts
        composeTestRule
            .onNodeWithTag("nav_charts")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify category breakdown is visible
        composeTestRule
            .onNodeWithTag("category_breakdown")
            .assertIsDisplayed()
    }

    @Test
    fun chartsScreen_shouldDisplayLegend() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to charts
        composeTestRule
            .onNodeWithTag("nav_charts")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify legend is visible
        composeTestRule
            .onNodeWithTag("chart_legend")
            .assertIsDisplayed()
    }
}
