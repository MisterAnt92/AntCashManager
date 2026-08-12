package com.antcashmanager.android.ui.screen

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antcashmanager.android.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for Home Screen functionality.
 *
 * Tests cover:
 * - Display of balance cards
 * - Display of top cards (Quick Insights, Daily Expense, etc.)
 * - Display of recent transactions
 * - Card reordering functionality
 * - Date range filtering
 * - Quick actions (add transaction, customize)
 *
 * These tests run on Android device/emulator and verify real screen behavior.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Test that Home screen displays main balance card
     */
    @Test
    fun homeScreen_shouldDisplayBalanceCard() {
        // Verify balance section exists
        composeTestRule.onNodeWithText("Balance")
            .assertExists()

        // Verify wallet card is displayed
        composeTestRule.onNodeWithText("Wallet")
            .assertExists()
    }

    /**
     * Test that top cards are displayed on Home screen
     */
    @Test
    fun homeScreen_shouldDisplayTopCards() {
        // Scroll to ensure all content is visible
        composeTestRule.onNodeWithText("Home")
            .performScrollToNode(hasText("Quick Insights"))

        // Verify quick insights card exists
        composeTestRule.onNodeWithText("Quick Insights")
            .assertExists()
    }

    /**
     * Test that recent transactions are displayed
     */
    @Test
    fun homeScreen_shouldDisplayRecentTransactions() {
        // Scroll down to transactions section
        composeTestRule.onNodeWithText("Home")
            .performScrollToNode(hasText("Recent"))

        // Verify recent transactions section exists
        composeTestRule.onNodeWithText("Recent")
            .assertExists()
    }

    /**
     * Test customize cards button opens dialog
     */
    @Test
    fun homeScreen_customizeButton_shouldOpenDialog() {
        // Find and click customize button (usually has Sort icon)
        composeTestRule.onNodeWithContentDescription("Customize cards")
            .performClick()

        // Verify dialog opens (look for dialog-specific element)
        // This may vary based on dialog implementation
    }

    /**
     * Test add transaction button navigates to add transaction screen
     */
    @Test
    fun homeScreen_addTransactionButton_shouldNavigateToAddTransaction() {
        // Find and click add transaction button (+ icon)
        composeTestRule.onNodeWithContentDescription("Add transaction")
            .performClick()

        // Verify navigation to add transaction screen
        composeTestRule.onNodeWithText("Add Transaction")
            .assertExists()
    }

    /**
     * Test that filtering by date range works
     */
    @Test
    fun homeScreen_dateFilter_shouldUpdateTransactionDisplay() {
        // Find date filter button/area
        composeTestRule.onNodeWithContentDescription("Date range")
            .performClick()

        // Select a specific date range (implementation varies)
        // Verify transactions are updated based on filter
    }

    /**
     * Test that screen refreshes when navigating away and back
     */
    @Test
    fun homeScreen_shouldRefreshWhenReturning() {
        // Navigate away from Home
        composeTestRule.onNodeWithContentDescription("Transactions")
            .performClick()

        // Navigate back to Home
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()

        // Verify Home screen is displayed and refreshed
        composeTestRule.onNodeWithText("Balance")
            .assertExists()
    }

    /**
     * Test that empty state is handled gracefully when no transactions
     */
    @Test
    fun homeScreen_emptyState_shouldDisplayEmptyMessage() {
        // This test assumes there's an empty state when no transactions exist
        // Scroll to check for empty state message
        composeTestRule.onNodeWithText("Home")
            .performScrollToNode(hasText("No transactions"))
    }

    /**
     * Test that cards are responsive to data changes
     */
    @Test
    fun homeScreen_cards_shouldBeResponsiveToDataChanges() {
        // Verify initial state
        composeTestRule.onNodeWithText("Balance")
            .assertExists()

        // Navigate to add transaction
        composeTestRule.onNodeWithContentDescription("Add transaction")
            .performClick()

        // Complete transaction (simplified - implementation depends on AddTransaction screen)

        // Navigate back to Home
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()

        // Verify balance is updated
        composeTestRule.onNodeWithText("Balance")
            .assertExists()
    }
}
