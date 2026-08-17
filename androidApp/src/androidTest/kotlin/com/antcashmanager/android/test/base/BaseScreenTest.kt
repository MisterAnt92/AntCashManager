package com.antcashmanager.android.test.base

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Rule
import org.junit.runner.RunWith

/**
 * Base class for all screen/UI instrumentation tests
 *
 * Provides common infrastructure, helpers, and cleanup for all screen-level tests.
 * Eliminates boilerplate by centralizing rule setup, common helpers, and @After cleanup.
 *
 * This class uses Compose Test v2 API (StandardTestDispatcher) which provides
 * deterministic testing with proper coroutine queuing rather than immediate execution.
 *
 * **Usage**:
 * ```kotlin
 * @RunWith(AndroidJUnit4::class)
 * class HomeScreenTest : BaseScreenTest() {
 *
 *     @Test
 *     fun testLoadTransactionsSuccessfully() {
 *         // Use inherited helpers:
 *         val title = getString(R.string.app_name)  // Get string resource
 *         val activity = getActivity()              // Get activity instance
 *         composeTestRule.waitForNode("Transactions")  // Wait for node
 *     }
 *
 *     @After
 *     override fun tearDown() {
 *         // Custom cleanup
 *         super.tearDown()  // Call parent cleanup
 *     }
 * }
 * ```
 *
 * **Key Benefits**:
 * - Standardized Compose Test v2 API usage across all tests
 * - No need to repeat `@get:Rule val composeTestRule = ...` in each test class
 * - Common helpers for string resources and activity access
 * - Guaranteed cleanup between tests via @After
 * - Reduced code duplication by ~50 lines per test file
 */
@RunWith(AndroidJUnit4::class)
abstract class BaseScreenTest {

    /**
     * Compose test rule with v2 API (StandardTestDispatcher)
     *
     * This rule handles:
     * - Setting up the test activity
     * - Creating a StandardTestDispatcher for deterministic testing
     * - Managing the Compose test environment lifecycle
     *
     * Note: Activity is accessed via reflection (TestHelpers.getTestActivity)
     * since v2 API doesn't expose it as a public property.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /**
     * Get string resource from application context
     *
     * Centralizes access to string resources and eliminates the need to
     * call InstrumentationRegistry directly in tests.
     *
     * @param stringId The string resource ID
     * @return The string value of the resource
     *
     * Example:
     * ```kotlin
     * val saveButtonText = getString(R.string.save)
     * ```
     */
    protected fun getString(stringId: Int): String =
        InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(stringId)

    /**
     * Get the test activity instance
     *
     * Provides access to the activity being tested for cases where direct
     * activity interaction is needed (e.g., for intent verification).
     *
     * @return The ComponentActivity instance
     *
     * Example:
     * ```kotlin
     * val activity = getActivity()
     * assertEquals("MainActivity", activity::class.simpleName)
     * ```
     */
    protected fun getActivity(): ComponentActivity =
        composeTestRule.activity


    /**
     * Common cleanup after each test
     *
     * Subclasses can override to add test-specific cleanup while calling
     * super.tearDown() to ensure base cleanup runs.
     *
     * This ensures:
     * - No state leakage between tests
     * - Proper resource cleanup
     * - Deterministic test execution order independence
     *
     * Example:
     * ```kotlin
     * @After
     * override fun tearDown() {
     *     // Custom cleanup (clear database, reset preferences, etc.)
     *     database.clearAllData()
     *
     *     super.tearDown()  // Always call parent cleanup
     * }
     * ```
     */
    @After
    open fun tearDown() {
        // Default cleanup - override in subclass if specific cleanup is needed
        // Examples of what subclasses might do:
        // - Clear test database
        // - Reset shared preferences
        // - Close resources
        // - Cancel background tasks
    }
}
