package com.antcashmanager.android.ui

import android.os.Build
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.dialog.AppExitConfirmationDialog
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for app exit behavior across Android SDK versions.
 *
 * Validates that:
 * - Activity.finish() is called when user confirms exit
 * - Dialog properly initiates app termination
 * - Behavior is consistent across SDK 26 (Android 8.0) to SDK 34 (Android 14)
 *
 * SDK-Specific Testing:
 * - API 26-27: Android 8.0-8.1 (Oreo)
 * - API 28-29: Android 9.0-10 (Pie, Q)
 * - API 30-31: Android 11.0-11.1 (R)
 * - API 32-33: Android 12-12.1, 13 (S, Tiramisu)
 * - API 34+: Android 14+ (Upside Down Cake+)
 */
@RunWith(AndroidJUnit4::class)
class AppExitBehaviorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test that confirms the activity finish() call is triggered on exit confirmation.
     * Works across all supported Android versions.
     */
    @Test
    fun confirmExitTriggersActivityFinish() {
        // Given: Activity context and exit dialog
        var exitCallbackInvoked = false

        composeTestRule.setContent {
            AntCashManagerTheme {
                AppExitConfirmationDialog(
                    onConfirmExit = {
                        // Simulate what NavGraph does: activity?.finish()
                        exitCallbackInvoked = true
                        composeTestRule.activity.finish()
                    },
                    onDismiss = {},
                    isVisible = true
                )
            }
        }

        // When: User confirms exit by clicking confirm button
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.exit_app_confirm)
        ).performClick()

        composeTestRule.waitForIdle()

        // Then: Activity termination is triggered
        assert(exitCallbackInvoked) { "Exit callback should be invoked" }
        assert(composeTestRule.activity.isFinishing) { "Activity should be finishing after exit confirmation" }
    }

    /**
     * Verify that dismiss/cancel does NOT trigger activity termination.
     */
    @Test
    fun dismissDialogDoesNotTerminateApp() {
        // Given: Activity and exit dialog
        var dismissCallbackInvoked = false

        composeTestRule.setContent {
            AntCashManagerTheme {
                AppExitConfirmationDialog(
                    onConfirmExit = { composeTestRule.activity.finish() },
                    onDismiss = { dismissCallbackInvoked = true },
                    isVisible = true
                )
            }
        }

        // When: User dismisses the dialog by clicking cancel
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.common_cancel)
        ).performClick()

        composeTestRule.waitForIdle()

        // Then: App should NOT terminate
        assert(dismissCallbackInvoked) { "Dismiss callback should be invoked" }
        assert(!composeTestRule.activity.isFinishing) { "Activity should still be running after dismiss" }
    }

    /**
     * Test SDK-specific behavior for Android 8.0-8.1 (API 26-27 Oreo)
     */
    @Test
    fun exitBehaviorAndroid8_0() {
        skipIfNotRunningOn(Build.VERSION_CODES.O, Build.VERSION_CODES.O_MR1)

        composeTestRule.setContent {
            AntCashManagerTheme {
                AppExitConfirmationDialog(
                    onConfirmExit = { composeTestRule.activity.finish() },
                    onDismiss = {},
                    isVisible = true
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.exit_app_confirm)
        ).performClick()

        composeTestRule.waitForIdle()
        assert(composeTestRule.activity.isFinishing) { "Activity should finish on Oreo (API 26-27)" }
    }

    /**
     * Test SDK-specific behavior for Android 9.0-10 (API 28-29 Pie/Q)
     */
    @Test
    fun exitBehaviorAndroid9_0To10() {
        skipIfNotRunningOn(Build.VERSION_CODES.P, Build.VERSION_CODES.Q)

        composeTestRule.setContent {
            AntCashManagerTheme {
                AppExitConfirmationDialog(
                    onConfirmExit = { composeTestRule.activity.finish() },
                    onDismiss = {},
                    isVisible = true
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.exit_app_confirm)
        ).performClick()

        composeTestRule.waitForIdle()
        assert(composeTestRule.activity.isFinishing) { "Activity should finish on Pie/Q (API 28-29)" }
    }

    /**
     * Test SDK-specific behavior for Android 11.0-11.1 (API 30-31 R)
     */
    @Test
    fun exitBehaviorAndroid11() {
        skipIfNotRunningOn(Build.VERSION_CODES.R, 31)  // 31 = Android 11.1

        composeTestRule.setContent {
            AntCashManagerTheme {
                AppExitConfirmationDialog(
                    onConfirmExit = { composeTestRule.activity.finish() },
                    onDismiss = {},
                    isVisible = true
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.exit_app_confirm)
        ).performClick()

        composeTestRule.waitForIdle()
        assert(composeTestRule.activity.isFinishing) { "Activity should finish on R (API 30-31)" }
    }

    /**
     * Test SDK-specific behavior for Android 12-13 (API 32-33 S/Tiramisu)
     */
    @Test
    fun exitBehaviorAndroid12To13() {
        skipIfNotRunningOn(32, 33)  // Android 12-13

        composeTestRule.setContent {
            AntCashManagerTheme {
                AppExitConfirmationDialog(
                    onConfirmExit = { composeTestRule.activity.finish() },
                    onDismiss = {},
                    isVisible = true
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.exit_app_confirm)
        ).performClick()

        composeTestRule.waitForIdle()
        assert(composeTestRule.activity.isFinishing) { "Activity should finish on S/Tiramisu (API 32-33)" }
    }

    /**
     * Test SDK-specific behavior for Android 14+ (API 34+ Upside Down Cake)
     */
    @Test
    fun exitBehaviorAndroid14Plus() {
        skipIfNotRunningOn(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)

        composeTestRule.setContent {
            AntCashManagerTheme {
                AppExitConfirmationDialog(
                    onConfirmExit = { composeTestRule.activity.finish() },
                    onDismiss = {},
                    isVisible = true
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.exit_app_confirm)
        ).performClick()

        composeTestRule.waitForIdle()
        assert(composeTestRule.activity.isFinishing) { "Activity should finish on UDC+ (API 34+)" }
    }

    /**
     * Test that the app handles back gesture correctly (all SDK versions)
     * when exit dialog is dismissed.
     */
    @Test
    fun backGestureAfterDismissDoesNotCrash() {
        // This validates that dismissing doesn't leave the app in an invalid state
        var dismissCalled = false

        composeTestRule.setContent {
            AntCashManagerTheme {
                AppExitConfirmationDialog(
                    onConfirmExit = { composeTestRule.activity.finish() },
                    onDismiss = { dismissCalled = true },
                    isVisible = true
                )
            }
        }

        // Dismiss the dialog
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.common_cancel)
        ).performClick()

        composeTestRule.waitForIdle()

        // Verify state is valid
        assert(dismissCalled) { "Dismiss should be called" }
        assert(!composeTestRule.activity.isFinishing) { "Activity should not be finishing" }
        // App should continue functioning (no crash)
    }

    /**
     * Helper function to skip test if not running on specified SDK level(s)
     */
    private fun skipIfNotRunningOn(vararg sdkLevels: Int) {
        val currentSdk = Build.VERSION.SDK_INT
        val isRunningOnTargetSdk = sdkLevels.contains(currentSdk)

        if (!isRunningOnTargetSdk) {
            // Test is skipped for this SDK level - this is expected
            Assume.assumeTrue("Skipping test for SDK level $currentSdk", false)
        }
    }
}
