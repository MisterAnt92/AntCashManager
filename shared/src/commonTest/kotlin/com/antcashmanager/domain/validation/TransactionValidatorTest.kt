package com.antcashmanager.domain.validation

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for [TransactionValidator].
 *
 * Validates:
 * - Title validation (empty, blank)
 * - Amount validation (zero, negative)
 * - Timestamp validation (future dates)
 * - Category validation (empty)
 * - Multiple errors handling
 */
class TransactionValidatorTest {

    private val mockClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1000000)
    }

    private val validator = TransactionValidatorImpl(clock = mockClock)

    @Test
    fun validate_shouldPass_whenTransactionIsValid() {
        // Arrange
        val transaction = Transaction(
            title = "Valid Transaction",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000, // < 1000000
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
        assertEquals(0, result.errors.size)
    }

    @Test
    fun validate_shouldFail_whenTitleIsEmpty() {
        // Arrange
        val transaction = Transaction(
            title = "",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertTrue(result.errors.any { it.code == "TRANSACTION_TITLE_EMPTY" })
        assertEquals("title", result.errors.find { it.code == "TRANSACTION_TITLE_EMPTY" }?.field)
    }

    @Test
    fun validate_shouldFail_whenTitleIsBlank() {
        // Arrange
        val transaction = Transaction(
            title = "   ",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertTrue(result.errors.any { it.code == "TRANSACTION_TITLE_EMPTY" })
    }

    @Test
    fun validate_shouldFail_whenAmountIsZero() {
        // Arrange
        val transaction = Transaction(
            title = "Valid Title",
            amount = 0.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertTrue(result.errors.any { it.code == "TRANSACTION_AMOUNT_NOT_POSITIVE" })
    }

    @Test
    fun validate_shouldFail_whenAmountIsNegative() {
        // Arrange
        val transaction = Transaction(
            title = "Valid Title",
            amount = -50.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertTrue(result.errors.any { it.code == "TRANSACTION_AMOUNT_NOT_POSITIVE" })
        assertEquals("amount", result.errors.find { it.code == "TRANSACTION_AMOUNT_NOT_POSITIVE" }?.field)
    }

    @Test
    fun validate_shouldFail_whenTimestampIsInFuture() {
        // Arrange
        val transaction = Transaction(
            title = "Valid Title",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 1001000, // > 1000000
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertTrue(result.errors.any { it.code == "TRANSACTION_TIMESTAMP_FUTURE" })
        assertEquals("timestamp", result.errors.find { it.code == "TRANSACTION_TIMESTAMP_FUTURE" }?.field)
    }

    @Test
    fun validate_shouldPass_whenTimestampIsExactlyNow() {
        // Arrange
        val transaction = Transaction(
            title = "Valid Title",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 1000000, // == 1000000
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validate_shouldFail_whenCategoryIsEmpty() {
        // Arrange
        val transaction = Transaction(
            title = "Valid Title",
            amount = 100.0,
            category = "",
            type = TransactionType.EXPENSE,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertTrue(result.errors.any { it.code == "TRANSACTION_CATEGORY_EMPTY" })
        assertEquals("category", result.errors.find { it.code == "TRANSACTION_CATEGORY_EMPTY" }?.field)
    }

    @Test
    fun validate_shouldFail_whenCategoryIsBlank() {
        // Arrange
        val transaction = Transaction(
            title = "Valid Title",
            amount = 100.0,
            category = "  \t  ",
            type = TransactionType.EXPENSE,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertTrue(result.errors.any { it.code == "TRANSACTION_CATEGORY_EMPTY" })
    }

    @Test
    fun validate_shouldCollectMultipleErrors_whenMultipleFieldsInvalid() {
        // Arrange
        val transaction = Transaction(
            title = "",  // Invalid
            amount = -10.0,  // Invalid
            category = "",  // Invalid
            type = TransactionType.EXPENSE,
            timestamp = 1001000,  // Invalid (future)
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertEquals(4, result.errors.size)
        assertTrue(result.errors.any { it.code == "TRANSACTION_TITLE_EMPTY" })
        assertTrue(result.errors.any { it.code == "TRANSACTION_AMOUNT_NOT_POSITIVE" })
        assertTrue(result.errors.any { it.code == "TRANSACTION_CATEGORY_EMPTY" })
        assertTrue(result.errors.any { it.code == "TRANSACTION_TIMESTAMP_FUTURE" })
    }

    @Test
    fun validate_shouldReturnFieldInformation_forErrorMessages() {
        // Arrange
        val transaction = Transaction(
            title = "",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        val error = result.errors.find { it.code == "TRANSACTION_TITLE_EMPTY" }
        assertEquals("title", error?.field)
        assertTrue(error?.message?.isNotEmpty() ?: false)
    }

    @Test
    fun validate_shouldAcceptPositiveAmountsOfAnySize() {
        // Arrange
        val testCases = listOf(
            0.01,
            1.0,
            100.0,
            1000.99,
            999999.99,
        )

        // Act & Assert
        testCases.forEach { amount ->
            val transaction = Transaction(
                title = "Valid Title",
                amount = amount,
                category = "Food",
                type = TransactionType.EXPENSE,
            )
            val result = validator.validate(transaction)
            assertTrue(result.isValid(), "Amount $amount should be valid")
        }
    }

    @Test
    fun validationResult_onSuccess_shouldExecuteCallback_whenValid() {
        // Arrange
        val transaction = Transaction(
            title = "Valid",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )
        val result = validator.validate(transaction)
        var callbackExecuted = false

        // Act
        result.onSuccess { callbackExecuted = true }

        // Assert
        assertTrue(callbackExecuted)
    }

    @Test
    fun validationResult_onFailure_shouldExecuteCallback_whenInvalid() {
        // Arrange
        val transaction = Transaction(
            title = "",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )
        val result = validator.validate(transaction)
        var callbackExecuted = false

        // Act
        result.onFailure { callbackExecuted = true }

        // Assert
        assertTrue(callbackExecuted)
    }

    @Test
    fun validationResult_toResult_shouldReturnSuccess_whenValid() {
        // Arrange
        val transaction = Transaction(
            title = "Valid",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )
        val result = validator.validate(transaction)

        // Act
        val kotlinResult = result.toResult()

        // Assert
        assertTrue(kotlinResult.isSuccess)
    }

    @Test
    fun validationResult_toResult_shouldReturnFailure_whenInvalid() {
        // Arrange
        val transaction = Transaction(
            title = "",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )
        val result = validator.validate(transaction)

        // Act
        val kotlinResult = result.toResult()

        // Assert
        assertTrue(kotlinResult.isFailure)
        val exception = kotlinResult.exceptionOrNull()
        assertTrue(exception is ValidationException)
    }
}
