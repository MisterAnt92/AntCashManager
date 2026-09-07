package com.antcashmanager.android.test.helpers

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume

/**
 * Centralized test helper functions to reduce duplication across test files
 * Provides common utilities for accessing resources, activities, and controlling test execution
 *
 * Usage:
 * ```kotlin
 * class MyScreenTest : BaseScreenTest<MainActivity>() {
 *     @Test
 *     fun testSomething() {
 *         val title = TestHelpers.getResourceString(R.string.app_name)
 *         TestHelpers.skipIfNotRunningOn(30, 31)
 *     }
 * }
 * ```
 */
object TestHelpers {
    /**
     * Get string resource from application context
     *
     * This replaces the verbose pattern:
     * ```kotlin
     * InstrumentationRegistry.getInstrumentation()
     *     .targetContext.getString(stringId)
     * ```
     *
     * @param stringId The string resource ID
     * @return The string value of the resource
     */
    fun getResourceString(stringId: Int): String =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(stringId)

    /**
     * Access ComposeTestRule activity via reflection
     *
     * Works with Compose Test v2 API where activity is not directly exposed as a public property.
     * Uses reflection to access the internal activity field with fallback strategies.
     *
     * @param composeTestRule The ComposeTestRule instance
     * @return The ComponentActivity instance
     * @throws IllegalStateException if activity field cannot be accessed
     */
    fun getTestActivity(composeTestRule: Any): ComponentActivity {
        return try {
            // Try different field names used in different versions of Compose Test v2
            val fieldNames = listOf("activity", "activityRule", "mActivity", "_activity", "mActivityScenario")
            var field: java.lang.reflect.Field? = null

            for (fieldName in fieldNames) {
                try {
                    field = composeTestRule.javaClass.getDeclaredField(fieldName)
                    field.isAccessible = true
                    val value = field.get(composeTestRule)

                    // If it's already an activity, return it
                    if (value is ComponentActivity) {
                        return value
                    }

                    // If it's an ActivityScenario, get the activity from it
                    if (value != null && value.javaClass.simpleName == "ActivityScenario") {
                        val getActivityMethod = value.javaClass.getMethod("getActivityResult")
                        val result = getActivityMethod.invoke(value)
                        if (result != null && result is ComponentActivity) {
                            return result
                        }
                    }
                } catch (e: NoSuchFieldException) {
                    // Try next field name
                } catch (e: Exception) {
                    // Continue to next field if this one fails
                }
            }

            throw IllegalStateException(
                "Cannot access activity from ComposeTestRule: No compatible field found",
            )
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(
                "Cannot access activity from ComposeTestRule: ${e.message}",
                e,
            )
        }
    }

    /**
     * Skip test if not running on specified SDK level(s)
     *
     * This allows tests to be skipped on irrelevant API levels.
     * Useful for API-specific behavior testing.
     *
     * @param sdkLevels The required API levels (e.g., 30, 31 for Android 11-12)
     *
     * Example:
     * ```kotlin
     * @Test
     * fun testAndroid11Feature() {
     *     TestHelpers.skipIfNotRunningOn(30, 31)  // Only run on API 30-31
     *     // test code
     * }
     * ```
     */
    fun skipIfNotRunningOn(vararg sdkLevels: Int) {
        val currentSdk = Build.VERSION.SDK_INT
        val isRunningOnTargetSdk = sdkLevels.contains(currentSdk)

        if (!isRunningOnTargetSdk) {
            Assume.assumeTrue(
                "Test skipped for SDK level $currentSdk (required: ${sdkLevels.joinToString(",")})",
                false,
            )
        }
    }

    /**
     * Check if running on specific manufacturer device
     *
     * @param manufacturer The device manufacturer name (e.g., "samsung", "google")
     * @return true if running on specified manufacturer device
     *
     * Example:
     * ```kotlin
     * if (TestHelpers.isDeviceManufacturer("samsung")) {
     *     // Samsung-specific test logic
     * }
     * ```
     */
    fun isDeviceManufacturer(manufacturer: String): Boolean =
        Build.MANUFACTURER?.equals(manufacturer, ignoreCase = true) == true
}
