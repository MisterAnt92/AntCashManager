package com.antcashmanager.android.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antcashmanager.android.navigation.AntCashManagerNavHost
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Robust tests for Settings Screen using TestTag selectors.
 *
 * Tests:
 * - Settings menu display
 * - Theme selection
 * - Language selection
 * - Data management options
 * - Display settings
 * - About section
 *
 * Pattern: Uses stable TestTag selectors for reliability
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenRobustTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsScreen_shouldBeNavigable() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to settings
        composeTestRule.onNodeWithTag("nav_settings")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify settings screen is displayed
        composeTestRule.onNodeWithTag("settings_screen")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplaySettingsList() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to settings
        composeTestRule.onNodeWithTag("nav_settings")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify settings list is visible
        composeTestRule.onNodeWithTag("settings_list")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplayThemeOption() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to settings
        composeTestRule.onNodeWithTag("nav_settings")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify theme option is visible
        composeTestRule.onNodeWithTag("theme_setting")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplayLanguageOption() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to settings
        composeTestRule.onNodeWithTag("nav_settings")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify language option is visible
        composeTestRule.onNodeWithTag("language_setting")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplayDataManagementOption() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to settings
        composeTestRule.onNodeWithTag("nav_settings")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify data management option is visible
        composeTestRule.onNodeWithTag("data_management_option")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplayDisplaySettingsOption() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to settings
        composeTestRule.onNodeWithTag("nav_settings")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify display settings option is visible
        composeTestRule.onNodeWithTag("display_settings_option")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplayAboutOption() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                AntCashManagerNavHost()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to settings
        composeTestRule.onNodeWithTag("nav_settings")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify about option is visible
        composeTestRule.onNodeWithTag("about_option")
            .assertIsDisplayed()
    }
}
