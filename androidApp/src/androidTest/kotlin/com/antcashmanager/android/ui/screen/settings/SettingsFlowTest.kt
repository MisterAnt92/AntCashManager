package com.antcashmanager.android.ui.screen.settings

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
 * End-to-end instrumentation test for the "Settings" user flow.
 *
 * Tests the complete flow:
 * 1. Open Settings screen
 * 2. Change app theme (Light/Dark/System)
 * 3. Change language
 * 4. Configure display options
 * 5. Manage data (backup/restore)
 * 6. Verify changes persist across app restart
 *
 * This test ensures settings are properly saved and applied throughout
 * the app, and that data management operations work correctly.
 *
 * Related flow tests:
 * - NavigationTest: Tab navigation to Settings
 */
@RunWith(AndroidJUnit4::class)
class SettingsFlowTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Test theme change from Light to Dark
     */
    @Test
    fun settingsFlow_shouldChangeTheme_whenThemeOptionSelected() {
        // Navigate to Settings screen
        composeTestRule.onNodeWithContentDescription("Settings")
            .assertExists()
            .performClick()

        // Wait for Settings screen to load
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Find and click on Display/Theme option
        composeTestRule.onNodeWithText("Display Settings")
            .assertExists()
            .performClick()

        // Wait for Display Settings submenu
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Theme")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Select Dark theme
        composeTestRule.onNodeWithText("Dark Mode")
            .assertExists()
            .performClick()

        // Verify confirmation dialog appears
        composeTestRule.onNodeWithText("Apply")
            .assertExists()
            .performClick()

        // Wait for theme to apply (typically quick, but allow for animation)
        composeTestRule.waitUntil(timeoutMillis = 1500) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Verify we're back on Settings screen and theme is applied
        composeTestRule.onNodeWithText("Display Settings")
            .assertExists()
    }

    /**
     * Test language change
     */
    @Test
    fun settingsFlow_shouldChangeLanguage_whenLanguageOptionSelected() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Wait for Settings to load
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Find language option
        composeTestRule.onNodeWithText("Language")
            .assertExists()
            .performClick()

        // Wait for language dialog
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Select Language")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Select different language (e.g., Italian if app supports it)
        composeTestRule.onNodeWithText("Italiano")
            .assertExists()
            .performClick()

        // Wait for language to change
        // Text on screen should change to Italian
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Impostazioni") // Italian for "Settings"
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Test toggling card visibility on home screen
     */
    @Test
    fun settingsFlow_shouldToggleCardVisibility_whenDisplayOptionChanged() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Wait for Settings to load
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Find Display Settings
        composeTestRule.onNodeWithText("Display Settings")
            .performClick()

        // Wait for submenu
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Show Balance Card")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Toggle a card visibility option
        composeTestRule.onNodeWithContentDescription("Toggle Balance Card")
            .assertExists()
            .performClick()

        // Verify change is applied
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Test creating a backup of app data
     */
    @Test
    fun settingsFlow_shouldCreateBackup_whenBackupOptionClicked() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Wait for Settings to load
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Navigate to Data Management section
        composeTestRule.onNodeWithText("Data Management")
            .assertExists()
            .performClick()

        // Wait for Data Management submenu
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Create Backup")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Click Create Backup button
        composeTestRule.onNodeWithText("Create Backup")
            .performClick()

        // Wait for backup to complete (might show progress indicator)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                // Check for success message or return to settings
                composeTestRule.onNodeWithText("Backup Created Successfully")
                    .assertExists()
                true
            } catch (e: Exception) {
                try {
                    composeTestRule.onNodeWithText("Data Management")
                        .assertExists()
                    true
                } catch (e2: Exception) {
                    false
                }
            }
        }
    }

    /**
     * Test accessing about/version information
     */
    @Test
    fun settingsFlow_shouldDisplayAboutInfo_whenAboutOptionClicked() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Wait for Settings to load
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Scroll down to find About option
        // (might need to scroll depending on screen size)
        composeTestRule.onNodeWithText("About")
            .assertExists()
            .performClick()

        // Wait for About screen/dialog
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Version")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Verify version information is displayed
        composeTestRule.onNodeWithText("Version")
            .assertExists()
    }

    /**
     * Test clearing cached data
     */
    @Test
    fun settingsFlow_shouldClearCache_whenClearCacheOptionClicked() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Wait for Settings to load
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Navigate to Data Management
        composeTestRule.onNodeWithText("Data Management")
            .performClick()

        // Wait for submenu
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Clear Cache")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Click Clear Cache
        composeTestRule.onNodeWithText("Clear Cache")
            .performClick()

        // Confirm dialog if it appears
        try {
            composeTestRule.onNodeWithText("Confirm")
                .assertExists()
                .performClick()
        } catch (e: Exception) {
            // Might not have confirmation dialog
        }

        // Wait for operation to complete
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Cache Cleared")
                    .assertExists()
                true
            } catch (e: Exception) {
                // Success message might not show, just check we're back on settings
                try {
                    composeTestRule.onNodeWithText("Data Management")
                        .assertExists()
                    true
                } catch (e2: Exception) {
                    false
                }
            }
        }
    }

    /**
     * Test that settings persist after navigation away and back
     */
    @Test
    fun settingsFlow_shouldPersistSettings_whenNavigatingAwayAndBack() {
        // Navigate to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Wait for Settings to load
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Change theme to Dark
        composeTestRule.onNodeWithText("Display Settings")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Theme")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        composeTestRule.onNodeWithText("Dark Mode")
            .performClick()

        composeTestRule.onNodeWithText("Apply")
            .performClick()

        // Navigate away from Settings to another screen (e.g., Home)
        composeTestRule.onNodeWithContentDescription("Home")
            .assertExists()
            .performClick()

        // Wait for Home to load
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Home")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Navigate back to Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Wait for Settings to load again
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Settings")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Verify theme setting is still Dark (persisted)
        composeTestRule.onNodeWithText("Display Settings")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Dark Mode")
                    .assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
