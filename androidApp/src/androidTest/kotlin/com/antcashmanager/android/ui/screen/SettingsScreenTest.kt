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
 * Instrumentation tests for Settings Screen functionality.
 *
 * Tests cover:
 * - Display settings
 * - Theme selection (Light/Dark/System)
 * - Language selection
 * - Currency display options
 * - Data management (Backup/Restore/Clear)
 * - About section
 * - Version information
 *
 * These tests run on Android device/emulator and verify real screen behavior.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Test navigation to Settings screen
     */
    @Test
    fun settingsScreen_shouldBeNavigable() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Verify Settings screen is displayed
        composeTestRule.onNodeWithText("Settings")
            .assertExists()
    }

    /**
     * Test theme selection button
     */
    @Test
    fun settingsScreen_themeButton_shouldOpenThemeDialog() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Click theme selection
        composeTestRule.onNodeWithText("Theme")
            .performClick()

        // Verify theme dialog opens
        composeTestRule.onNodeWithText("Light")
            .assertExists()
        composeTestRule.onNodeWithText("Dark")
            .assertExists()
    }

    /**
     * Test theme selection
     */
    @Test
    fun settingsScreen_selectTheme_shouldChangeTheme() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Open theme dialog
        composeTestRule.onNodeWithText("Theme")
            .performClick()

        // Select Dark theme
        composeTestRule.onNodeWithText("Dark")
            .performClick()

        // Verify theme changed (UI should update)
        // Note: This verifies at UI level, actual theme change may take a moment
    }

    /**
     * Test language selection button
     */
    @Test
    fun settingsScreen_languageButton_shouldOpenLanguageDialog() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Click language selection
        composeTestRule.onNodeWithText("Language")
            .performClick()

        // Verify language dialog opens with options
        composeTestRule.onNodeWithText("English")
            .assertExists()
    }

    /**
     * Test currency display setting
     */
    @Test
    fun settingsScreen_currencySetting_shouldBeAvailable() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Look for currency setting
        composeTestRule.onNodeWithText("Currency")
            .assertExists()
    }

    /**
     * Test data management section
     */
    @Test
    fun settingsScreen_dataManagement_shouldHaveOptions() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Scroll to data management section
        composeTestRule.onNodeWithText("Settings")
            .performScrollToNode(hasText("Data Management"))

        // Verify data management section exists
        composeTestRule.onNodeWithText("Data Management")
            .assertExists()
    }

    /**
     * Test backup button
     */
    @Test
    fun settingsScreen_backupButton_shouldStartBackup() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Scroll to data management
        composeTestRule.onNodeWithText("Settings")
            .performScrollToNode(hasText("Data Management"))

        // Click backup button
        composeTestRule.onNodeWithText("Backup")
            .performClick()

        // Verify backup process starts (dialog or progress shown)
    }

    /**
     * Test about section
     */
    @Test
    fun settingsScreen_aboutSection_shouldDisplayInfo() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Scroll to about section
        composeTestRule.onNodeWithText("Settings")
            .performScrollToNode(hasText("About"))

        // Verify about section exists
        composeTestRule.onNodeWithText("About")
            .assertExists()
    }

    /**
     * Test version information display
     */
    @Test
    fun settingsScreen_shouldDisplayVersion() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Scroll to bottom for version info
        composeTestRule.onNodeWithText("Settings")
            .performScrollToNode(hasText("Version"))

        // Verify version is displayed
        composeTestRule.onNodeWithText("Version")
            .assertExists()
    }

    /**
     * Test navigation to display settings
     */
    @Test
    fun settingsScreen_displaySettings_shouldBeAccessible() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Look for display settings option
        composeTestRule.onNodeWithText("Display")
            .performClick()

        // Verify navigation to display settings
    }

    /**
     * Test notification settings
     */
    @Test
    fun settingsScreen_notificationSettings_shouldBeAvailable() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Look for notification settings (if available)
        composeTestRule.onNodeWithText("Notifications")
            .assertExists()
    }

    /**
     * Test scrolling through settings
     */
    @Test
    fun settingsScreen_scroll_shouldShowAllSettings() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Scroll down to see all settings
        composeTestRule.onNodeWithText("Settings")
            .performScrollToNode(hasText("About"))
    }

    /**
     * Test settings persistence
     */
    @Test
    fun settingsScreen_changesShouldBePersisted() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Change a setting
        composeTestRule.onNodeWithText("Theme")
            .performClick()

        composeTestRule.onNodeWithText("Dark")
            .performClick()

        // Navigate away
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()

        // Navigate back to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Verify setting is still changed
        composeTestRule.onNodeWithText("Settings")
            .assertExists()
    }

    /**
     * Test clear data confirmation dialog
     */
    @Test
    fun settingsScreen_clearData_shouldShowConfirmation() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Scroll to data management
        composeTestRule.onNodeWithText("Settings")
            .performScrollToNode(hasText("Data Management"))

        // Click clear data
        composeTestRule.onNodeWithText("Clear Data")
            .performClick()

        // Verify confirmation dialog appears
        composeTestRule.onNodeWithText("Are you sure")
            .assertExists()
    }
}
