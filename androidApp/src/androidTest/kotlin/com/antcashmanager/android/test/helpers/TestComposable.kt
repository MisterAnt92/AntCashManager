package com.antcashmanager.android.test.helpers

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Extension functions for common Compose test patterns
 * Reduces boilerplate and standardizes test interactions across all instrumentation tests
 *
 * These functions encapsulate common patterns and handle proper synchronization
 * with the Compose test framework's StandardTestDispatcher
 *
 * Usage:
 * ```kotlin
 * waitForNode(composeTestRule, "Save").clickAndWait(composeTestRule)
 * waitForNode(composeTestRule, "Title").typeAndWait("Test", composeTestRule)
 * ```
 */

/**
 * Click a node and wait for state to settle
 *
 * Combines performClick() with waitForIdle() to ensure the compose state
 * has settled before proceeding.
 *
 * @param composeTestRule The test rule instance
 * @param afterClickWaitMs Additional delay after click (for animations, etc.)
 */
fun SemanticsNodeInteraction.clickAndWait(
    composeTestRule: ComposeContentTestRule,
    afterClickWaitMs: Long = 500
) {
    this.performClick()
    composeTestRule.waitForIdle()
    // Additional wait if specified (for animations or transitions)
    if (afterClickWaitMs > 0) {
        try {
            Thread.sleep(afterClickWaitMs)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

/**
 * Type text into a field and wait for state to settle
 *
 * Combines performTextInput() with waitForIdle() to ensure any
 * state changes triggered by the text input have completed.
 *
 * @param text The text to type
 * @param composeTestRule The test rule instance
 */
fun SemanticsNodeInteraction.typeAndWait(
    text: String,
    composeTestRule: ComposeContentTestRule
) {
    this.performTextInput(text)
    composeTestRule.waitForIdle()
}

/**
 * Clear text in a field and type new text
 *
 * Useful for replacing existing text in fields. Handles both
 * clearing and input with proper synchronization.
 *
 * @param newText The text to type after clearing
 * @param composeTestRule The test rule instance
 */
fun SemanticsNodeInteraction.replaceText(
    newText: String,
    composeTestRule: ComposeContentTestRule
) {
    this.performTextClearance()
    this.typeAndWait(newText, composeTestRule)
}
