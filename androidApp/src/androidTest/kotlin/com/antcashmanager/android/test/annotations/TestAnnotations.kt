package com.antcashmanager.android.test.annotations

/**
 * Custom annotations for test categorization, filtering, and documentation
 *
 * These annotations provide metadata that can be used by:
 * - CI/CD pipelines (to skip, retry, or group tests)
 * - IDEs (to highlight special test behavior)
 * - Test developers (to understand test purpose and requirements)
 *
 * Usage in CI/CD gradle:
 * ```gradle
 * androidTestImplementation 'junit:junit:4.13.2'
 *
 * test {
 *     useJUnit {
 *         excludeCategories 'com.antcashmanager.android.test.annotations.SlowTest'
 *     }
 * }
 * ```
 *
 * Usage in gradle with TestWatcher:
 * ```kotlin
 * val annotation = testMethod.getAnnotation(FlakyTest::class.java)
 * if (annotation != null) {
 *     // Retry this test up to 3 times
 * }
 * ```
 */

/**
 * Indicates test requires specific Android API level or higher
 *
 * Used to skip tests on emulators/devices with API levels below the specified level.
 * Particularly useful for testing API-specific features or behaviors.
 *
 * **Example**:
 * ```kotlin
 * @Test
 * @RequiresApi(level = 30)
 * fun testAndroid11Feature() {
 *     // This test will only run on API 30+
 *     // Will be skipped on API 26-29
 * }
 * ```
 *
 * @param level The minimum required API level
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RequiresApi(
    val level: Int,
)

/**
 * Marks test as flaky (known to fail intermittently)
 *
 * Used to identify tests that have race conditions, timing issues, or
 * unstable behavior. CI/CD can use this to:
 * - Retry failing tests automatically
 * - Report flaky tests separately
 * - Quarantine tests to prevent build failures
 * - Track flakiness trends over time
 *
 * **Example**:
 * ```kotlin
 * @Test
 * @FlakyTest(reason = "Race condition on data loading - timing dependent")
 * fun testTransactionLoadingWithAnimation() {
 *     // This test may fail intermittently due to timing
 *     // CI/CD will retry it up to 3 times
 * }
 * ```
 *
 * @param reason Description of why the test is flaky
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class FlakyTest(
    val reason: String = "",
)

/**
 * Marks test as slow (takes > 2 seconds to complete)
 *
 * Used to categorize tests by performance for:
 * - Separate test execution phases (fast tests first, slow tests later)
 * - Performance reporting and tracking
 * - Local development (developers can run quick tests only)
 * - CI/CD optimization (run slow tests in separate job)
 *
 * **Example**:
 * ```kotlin
 * @Test
 * @SlowTest(estimatedMs = 5000)
 * fun testLargeDatasetLoading() {
 *     // This test takes ~5 seconds
 *     // CI/CD may run this in a separate job pool
 * }
 * ```
 *
 * @param estimatedMs Estimated test duration in milliseconds
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SlowTest(
    val estimatedMs: Int = 5000,
)

/**
 * Indicates test requires actual device (not emulator)
 *
 * Used to skip tests on emulator-only test runs. Some features or behaviors
 * may only be testable on real devices (e.g., hardware-specific features,
 * performance characteristics, or real sensor data).
 *
 * **Example**:
 * ```kotlin
 * @Test
 * @RequiresDevice(reason = "Requires real GPS/sensor data, not available on emulator")
 * fun testLocationServices() {
 *     // This test needs actual device hardware
 *     // Will be skipped on emulator runs
 * }
 * ```
 *
 * @param reason Description of why a real device is required
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RequiresDevice(
    val reason: String = "",
)

/**
 * Indicates test requires specific manufacturer device
 *
 * Used to skip tests that only apply to specific devices (e.g., Samsung-specific
 * UI behaviors, manufacturer customizations, or manufacturer-specific bugs).
 *
 * **Example**:
 * ```kotlin
 * @Test
 * @RequiresDeviceManufacturer(manufacturer = "samsung")
 * fun testSamsungOneUiSpecificBehavior() {
 *     // This test only applies to Samsung devices
 *     // Will be skipped on other manufacturer devices
 * }
 * ```
 *
 * @param manufacturer The manufacturer name (case-insensitive, e.g., "samsung", "google")
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RequiresDeviceManufacturer(
    val manufacturer: String,
)

/**
 * Marks test as integration test (tests multiple components together)
 *
 * Used to categorize tests by scope:
 * - Unit tests: single component in isolation
 * - Integration tests: multiple components working together
 * - E2E tests: full user flows across entire app
 *
 * **Example**:
 * ```kotlin
 * @Test
 * @IntegrationTest
 * fun testAddTransactionFlow() {
 *     // Tests AddTransaction screen + ViewModel + Repository together
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class IntegrationTest

/**
 * Marks test as end-to-end test (tests full user flows)
 *
 * Used to identify comprehensive tests that exercise complete user journeys
 * from app launch through multiple screens and operations.
 *
 * **Example**:
 * ```kotlin
 * @Test
 * @E2ETest
 * fun testCreateTransactionAndViewStatistics() {
 *     // Full flow: navigate → add transaction → view charts
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class E2ETest

/**
 * Marks test to skip in CI/CD pipeline
 *
 * Used for tests that should only run locally but not in CI/CD.
 * Examples: tests requiring local setup, tests for debugging, work-in-progress tests.
 *
 * **Example**:
 * ```kotlin
 * @Test
 * @SkipInCi
 * fun testLocalSetupSpecificFeature() {
 *     // This test needs special local setup
 *     // Will only run when running tests locally
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SkipInCi
