package com.antcashmanager.android.test.base

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Rule
import org.junit.runner.RunWith

/**
 * Base class for all Compose UI instrumentation tests.
 *
 * Provides centralized infrastructure for screen and navigation testing:
 * - Disables tutorial overlay automatically (via collectAsState initial value)
 * - Provides common helper methods
 * - Handles test lifecycle and cleanup
 *
 * **Tutorial Handling**:
 * NavGraph uses `collectAsState(initial = true)` for tutorial completion, which means
 * the tutorial overlay is disabled by default in tests. This ensures navigation buttons
 * and UI elements are always accessible without interference.
 *
 * **Usage**:
 * ```kotlin
 * @RunWith(AndroidJUnit4::class)
 * class MyNavigationTest : BaseInstrumentationTest() {
 *
 *     @Test
 *     fun testNavigation() {
 *         composeTestRule.setContent {
 *             AntCashManagerTheme {
 *                 AntCashManagerNavHost()
 *             }
 *         }
 *
 *         // Tutorial is automatically disabled
 *         // All navigation buttons are accessible
 *     }
 * }
 * ```
 *
 * **Why No Manual Mocking?**
 * - Simpler: Leverages existing `collectAsState(initial = true)` in NavGraph
 * - Robust: Works with Koin injection without extra setup
 * - Less Brittle: Doesn't depend on mocking infrastructure
 * - Automatic: Tutorial disabled without test setup code
 */
@RunWith(AndroidJUnit4::class)
abstract class BaseInstrumentationTest {
    /**
     * Compose test rule with v2 API (StandardTestDispatcher)
     *
     * Provides deterministic testing with proper coroutine handling.
     * Automatically manages the test activity lifecycle.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    init {
    }

    /**
     * Get string resource from application context.
     *
     * Centralizes access to string resources for tests.
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
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(stringId)

    /**
     * Get the test activity instance.
     *
     * Provides access to the activity being tested for cases where direct
     * activity interaction is needed.
     *
     * @return The ComponentActivity instance
     *
     * Example:
     * ```kotlin
     * val activity = getActivity()
     * assertEquals("MainActivity", activity::class.simpleName)
     * ```
     */
    protected fun getActivity(): ComponentActivity = composeTestRule.activity

    /**
     * Cleanup method called after each test.
     *
     * Subclasses can override to add test-specific cleanup while calling
     * super.tearDown() to ensure base cleanup runs.
     *
     * Example:
     * ```kotlin
     * @After
     * override fun tearDown() {
     *     // Custom cleanup
     *     database.clearAllData()
     *     super.tearDown()
     * }
     * ```
     */
    @After
    open fun tearDown() {
        // Base cleanup - override in subclass if specific cleanup is needed
        // Examples of what subclasses might do:
        // - Clear test database
        // - Reset shared preferences
        // - Cancel background tasks
    }
}
