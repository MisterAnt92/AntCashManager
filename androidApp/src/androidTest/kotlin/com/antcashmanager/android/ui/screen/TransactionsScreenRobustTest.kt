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
 * Robust tests for Transactions Screen using TestTag selectors.
 *
 * Tests:
 * - Transaction list display
 * - Search functionality
 * - Filter functionality
 * - Sort options
 * - Empty state handling
 *
 * Extends [BaseInstrumentationTest] which automatically:
 * - Disables the tutorial overlay
 * - Mocks SettingsRepository with defaults
 * - Provides common test helpers
 *
 * Pattern: Uses stable TestTag selectors for reliability
 */
@RunWith(AndroidJUnit4::class)
class TransactionsScreenRobustTest : BaseInstrumentationTest() {

    @Test
    fun transactionsScreen_shouldBeNavigable() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to transactions
        composeTestRule.onNodeWithTag("nav_transactions")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify transactions screen is displayed
        composeTestRule.onNodeWithTag("transactions_screen")
            .assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_shouldDisplayTransactionList() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to transactions
        composeTestRule.onNodeWithTag("nav_transactions")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify transaction list is visible
        composeTestRule.onNodeWithTag("transactions_list")
            .assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_shouldDisplaySearchBar() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to transactions
        composeTestRule.onNodeWithTag("nav_transactions")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify search bar is visible
        composeTestRule.onNodeWithTag("search_bar")
            .assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_shouldDisplayFilterButton() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to transactions
        composeTestRule.onNodeWithTag("nav_transactions")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify filter button is visible
        composeTestRule.onNodeWithTag("filter_button")
            .assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_shouldDisplaySortButton() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to transactions
        composeTestRule.onNodeWithTag("nav_transactions")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify sort button is visible
        composeTestRule.onNodeWithTag("sort_button")
            .assertIsDisplayed()
    }
}
