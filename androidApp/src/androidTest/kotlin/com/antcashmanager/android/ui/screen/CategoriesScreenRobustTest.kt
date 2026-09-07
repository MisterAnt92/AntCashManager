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
 * Robust tests for Categories Screen using TestTag selectors.
 *
 * Tests:
 * - Category list display
 * - Category search
 * - Reorder functionality
 * - Add category button
 *
 * Extends [BaseInstrumentationTest] which automatically:
 * - Disables the tutorial overlay
 * - Mocks SettingsRepository with defaults
 * - Provides common test helpers
 *
 * Pattern: Uses stable TestTag selectors for reliability
 */
@RunWith(AndroidJUnit4::class)
class CategoriesScreenRobustTest : BaseInstrumentationTest() {
    @Test
    fun categoriesScreen_shouldBeNavigable() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to categories
        composeTestRule
            .onNodeWithTag("nav_categories")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify categories screen is displayed
        composeTestRule
            .onNodeWithTag("categories_screen")
            .assertIsDisplayed()
    }

    @Test
    fun categoriesScreen_shouldDisplayCategoriesList() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to categories
        composeTestRule
            .onNodeWithTag("nav_categories")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify categories list is visible
        composeTestRule
            .onNodeWithTag("categories_list")
            .assertIsDisplayed()
    }

    @Test
    fun categoriesScreen_shouldDisplaySearchBar() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to categories
        composeTestRule
            .onNodeWithTag("nav_categories")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify search bar is visible
        composeTestRule
            .onNodeWithTag("categories_search_bar")
            .assertIsDisplayed()
    }

    @Test
    fun categoriesScreen_shouldDisplayAddButton() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to categories
        composeTestRule
            .onNodeWithTag("nav_categories")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify add button is visible
        composeTestRule
            .onNodeWithTag("add_category_button")
            .assertIsDisplayed()
    }

    @Test
    fun categoriesScreen_shouldDisplayReorderButton() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to categories
        composeTestRule
            .onNodeWithTag("nav_categories")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify reorder button is visible
        composeTestRule
            .onNodeWithTag("reorder_categories_button")
            .assertIsDisplayed()
    }
}
