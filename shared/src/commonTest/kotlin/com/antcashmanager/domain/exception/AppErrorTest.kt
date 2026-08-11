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
    fun categoryException_shouldStoreMessage() {
        val message = "Category not found"
        val exception = CategoryException(message)

        assertEquals(message, exception.message)
        assertNotNull(exception)
    }

    @Test
    fun categoryException_shouldHaveCause() {
        val cause = RuntimeException("Original error")
        val exception = CategoryException("Category error", cause)

        assertEquals(cause, exception.cause)
    }

    @Test
    fun receiptScanException_shouldStoreMessage() {
        val message = "Failed to scan receipt"
        val exception = ReceiptScanException(message)

        assertEquals(message, exception.message)
        assertNotNull(exception)
    }

    @Test
    fun receiptScanException_shouldHaveCause() {
        val cause = IllegalStateException("Camera unavailable")
        val exception = ReceiptScanException("Scan failed", cause)

        assertEquals(cause, exception.cause)
    }

    @Test
    fun categoryException_isThrowable() {
        val exception = CategoryException("Test error")
        assertTrue(exception is Throwable)
    }

    @Test
    fun receiptScanException_isThrowable() {
        val exception = ReceiptScanException("Test error")
        assertTrue(exception is Throwable)
    }

    @Test
    fun exception_canBeCaught() {
        try {
            throw CategoryException("Test exception")
        } catch (e: CategoryException) {
            assertEquals("Test exception", e.message)
        }
    }

    @Test
    fun exception_stackTraceIsAvailable() {
        val exception = CategoryException("Test")
        val stackTrace = exception.stackTrace
        assertNotNull(stackTrace)
    }

    @Test
    fun exception_toStringIncludesClassName() {
        val exception = CategoryException("Test message")
        val exceptionString = exception.toString()
        assertTrue(exceptionString.contains("CategoryException"))
    }
}
