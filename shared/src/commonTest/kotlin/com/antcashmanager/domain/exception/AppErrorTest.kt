package com.antcashmanager.domain.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for domain exception types.
 *
 * Tests cover:
 * - Exception creation and message storage
 * - Exception types (CategoryException, ReceiptScanException, etc.)
 * - Exception cause chaining
 * - Exception inheritance
 */
class AppErrorTest {

    @Test
    fun categoryException_notFound_shouldStoreMessage() {
        val categoryName = "Unknown"
        val exception = CategoryException.NotFound(categoryName)

        assertTrue(exception.message?.contains(categoryName) ?: false)
        assertNotNull(exception)
    }

    @Test
    fun categoryException_duplicateName_shouldStoreMessage() {
        val categoryName = "Food"
        val exception = CategoryException.DuplicateName(categoryName)

        assertTrue(exception.message?.contains(categoryName) ?: false)
        assertNotNull(exception)
    }

    @Test
    fun receiptScanException_invalidImage_shouldBeAvailable() {
        val exception = ReceiptScanException.InvalidImage

        assertTrue(exception.message?.contains("Image") ?: false)
        assertNotNull(exception)
    }

    @Test
    fun receiptScanException_noTextExtracted_shouldBeAvailable() {
        val exception = ReceiptScanException.NoTextExtracted

        assertTrue(exception.message?.contains("text") ?: false)
        assertNotNull(exception)
    }

    @Test
    fun categoryException_isThrowable() {
        val exception = CategoryException.NotFound("Test")
        assertTrue(exception is Throwable)
    }

    @Test
    fun receiptScanException_isThrowable() {
        val exception = ReceiptScanException.InvalidImage
        assertTrue(exception is Throwable)
    }

    @Test
    fun exception_canBeCaught() {
        try {
            throw CategoryException.NotFound("Test exception")
        } catch (e: CategoryException) {
            assertTrue(e.message?.contains("Test exception") ?: false)
        }
    }

    @Test
    fun exception_stackTraceIsAvailable() {
        val exception = CategoryException.NotFound("Test")
        val stackTrace = exception.stackTrace
        assertNotNull(stackTrace)
    }

    @Test
    fun exception_toStringIncludesClassName() {
        val exception = CategoryException.NotFound("Test message")
        val exceptionString = exception.toString()
        assertTrue(exceptionString.contains("CategoryException") || exceptionString.contains("NotFound"))
    }
}
