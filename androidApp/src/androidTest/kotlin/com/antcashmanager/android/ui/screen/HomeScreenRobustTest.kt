package com.antcashmanager.android.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antcashmanager.android.navigation.AntCashManagerNavHost
import com.antcashmanager.android.test.base.BaseInstrumentationTest
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Robust tests for Home Screen using TestTag selectors.
 *
 * Tests core home screen functionality:
 * - Balance card display
 * - Recent transactions list
 * - Quick action buttons
 * - FAB (Floating Action Button)
 *
 * Extends [BaseInstrumentationTest] which automatically:
 * - Disables the tutorial overlay
 * - Mocks SettingsRepository with defaults
 * - Provides common test helpers
 *
 * Pattern: Uses stable TestTag selectors, not affected by text/language changes
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenRobustTest : BaseInstrumentationTest() {

    @Test
    fun homeScreen_shouldDisplayBalanceCard() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Verify balance card is visible
        composeTestRule.onNodeWithTag("balance_card")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_shouldDisplayRecentTransactions() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Verify recent transactions section exists
        composeTestRule.onNodeWithTag("recent_transactions_section")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_shouldDisplayFAB() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Verify FAB (Floating Action Button) for adding transactions
        composeTestRule.onNodeWithTag("fab_add_transaction")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_shouldDisplayTopCards() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Verify top info cards (income/expense/balance)
        composeTestRule.onNodeWithTag("income_card")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("expense_card")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_shouldHaveScrollableContent() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Verify scrollable content area exists
        composeTestRule.onNodeWithTag("home_scrollable_content")
            .assertIsDisplayed()
    }
}
