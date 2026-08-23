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
 * - Meal vouchers support (mealVoucherCount, mealVoucherDifference)
 * - ValidationResult DSL (onSuccess, onFailure chaining)
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
                timestamp = 999000,  // < 1000000
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
            timestamp = 999000,  // < 1000000
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
            timestamp = 999000,  // < 1000000
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

    @Test
    fun validate_shouldPass_withMealVouchersPaymentType() {
        // Arrange
        val transaction = Transaction(
            title = "Meal Voucher Payment",
            amount = 15.87,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
            paymentType = PaymentType.MEAL_VOUCHERS,
            mealVoucherCount = 3,
            mealVoucherDifference = 0.0,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
        assertEquals(0, result.errors.size)
    }

    @Test
    fun validate_shouldPass_withMealVoucherDifference() {
        // Arrange
        val transaction = Transaction(
            title = "Lunch with Difference",
            amount = 21.16,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
            paymentType = PaymentType.MEAL_VOUCHERS,
            mealVoucherCount = 3,
            mealVoucherDifference = 5.29,  // Default meal voucher value
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validate_shouldPass_withNegativeMealVoucherDifference() {
        // Arrange - negative difference when vouchers cover more than amount
        val transaction = Transaction(
            title = "Lunch Discount",
            amount = 10.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
            paymentType = PaymentType.MEAL_VOUCHERS,
            mealVoucherCount = 2,
            mealVoucherDifference = -0.58,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validate_shouldPass_withZeroMealVoucherCount() {
        // Arrange
        val transaction = Transaction(
            title = "No Vouchers",
            amount = 50.0,
            category = "Restaurant",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
            mealVoucherCount = 0,
            mealVoucherDifference = 0.0,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validate_shouldPass_withCashPaymentType() {
        // Arrange
        val transaction = Transaction(
            title = "Cash Payment",
            amount = 25.50,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
            paymentType = PaymentType.CASH,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validate_shouldPass_withElectronicPaymentType() {
        // Arrange
        val transaction = Transaction(
            title = "Card Payment",
            amount = 75.00,
            category = "Restaurant",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
            paymentType = PaymentType.ELECTRONIC,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validationResult_isInvalid_shouldReturnTrue_whenHasErrors() {
        // Arrange
        val transaction = Transaction(
            title = "",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
        )
        val result = validator.validate(transaction)

        // Act & Assert
        assertTrue(result.isInvalid())
        assertFalse(result.isValid())
    }

    @Test
    fun validationResult_isInvalid_shouldReturnFalse_whenNoErrors() {
        // Arrange
        val transaction = Transaction(
            title = "Valid",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
        )
        val result = validator.validate(transaction)

        // Act & Assert
        assertFalse(result.isInvalid())
        assertTrue(result.isValid())
    }

    @Test
    fun validationResult_onSuccess_shouldReturnResult_forChaining() {
        // Arrange
        val transaction = Transaction(
            title = "Valid",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
        )
        val result = validator.validate(transaction)
        var successCalled = false

        // Act - test method chaining
        val chainResult = result.onSuccess { successCalled = true }

        // Assert
        assertTrue(successCalled)
        assertEquals(result, chainResult)  // Verify it returns itself for chaining
    }

    @Test
    fun validationResult_onFailure_shouldReturnResult_forChaining() {
        // Arrange
        val transaction = Transaction(
            title = "",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
        )
        val result = validator.validate(transaction)
        var failureCalled = false

        // Act - test method chaining
        val chainResult = result.onFailure { failureCalled = true }

        // Assert
        assertTrue(failureCalled)
        assertEquals(result, chainResult)  // Verify it returns itself for chaining
    }

    @Test
    fun validationResult_shouldChain_onSuccessAndOnFailure() {
        // Arrange
        val transaction = Transaction(
            title = "Valid",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
        )
        val result = validator.validate(transaction)
        var successCalled = false
        var failureCalled = false

        // Act - chain both handlers
        result
            .onSuccess { successCalled = true }
            .onFailure { failureCalled = true }

        // Assert
        assertTrue(successCalled)
        assertFalse(failureCalled)  // Only success should be called for valid result
    }

    @Test
    fun validationResult_shouldNotCallOnSuccess_whenInvalid() {
        // Arrange
        val transaction = Transaction(
            title = "",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
        )
        val result = validator.validate(transaction)
        var successCalled = false
        var failureCalled = false

        // Act
        result
            .onSuccess { successCalled = true }
            .onFailure { failureCalled = true }

        // Assert
        assertFalse(successCalled)  // Should not be called for invalid result
        assertTrue(failureCalled)
    }

    @Test
    fun validationException_shouldContainAllErrors() {
        // Arrange
        val transaction = Transaction(
            title = "",  // Invalid
            amount = -10.0,  // Invalid
            category = "",  // Invalid
            type = TransactionType.EXPENSE,
            timestamp = 1001000,  // Invalid (future)
        )
        val result = validator.validate(transaction)

        // Act
        val exception = ValidationException(result.errors)

        // Assert
        assertEquals(4, exception.errors.size)
        assertTrue(exception.message?.contains("TRANSACTION_TITLE_EMPTY") ?: false)
        assertTrue(exception.message?.contains("TRANSACTION_AMOUNT_NOT_POSITIVE") ?: false)
        assertTrue(exception.message?.contains("TRANSACTION_CATEGORY_EMPTY") ?: false)
        assertTrue(exception.message?.contains("TRANSACTION_TIMESTAMP_FUTURE") ?: false)
    }

    @Test
    fun validate_shouldPass_withIncomeTransaction() {
        // Arrange
        val transaction = Transaction(
            title = "Salary",
            amount = 2500.00,
            category = "Income",
            type = TransactionType.INCOME,
            timestamp = 999000,  // < 1000000
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validate_shouldFail_whenIncomeWithInvalidTitle() {
        // Arrange
        val transaction = Transaction(
            title = "   ",
            amount = 2500.00,
            category = "Income",
            type = TransactionType.INCOME,
            timestamp = 999000,  // < 1000000
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertFalse(result.isValid())
        assertTrue(result.errors.any { it.code == "TRANSACTION_TITLE_EMPTY" })
    }

    @Test
    fun validate_shouldPass_withVerySmallAmount() {
        // Arrange
        val transaction = Transaction(
            title = "Tip",
            amount = 0.001,  // Very small but positive
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validate_shouldPass_withVeryLargeAmount() {
        // Arrange
        val transaction = Transaction(
            title = "Large Purchase",
            amount = 999999.99,  // Very large amount
            category = "Miscellaneous",
            type = TransactionType.EXPENSE,
            timestamp = 999000,  // < 1000000
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validate_shouldPass_withPastTimestamp() {
        // Arrange
        val transaction = Transaction(
            title = "Old Transaction",
            amount = 100.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 1,  // Very old timestamp
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun validationError_shouldIncludeFieldInfo() {
        // Arrange
        val transaction = Transaction(
            title = "",
            amount = -50.0,
            category = "",
            type = TransactionType.EXPENSE,
            timestamp = 1001000,
        )

        // Act
        val result = validator.validate(transaction)

        // Assert
        result.errors.forEach { error ->
            assertFalse(error.message.isEmpty())
            assertFalse(error.code.isEmpty())
            when (error.code) {
                "TRANSACTION_TITLE_EMPTY" -> assertEquals("title", error.field)
                "TRANSACTION_AMOUNT_NOT_POSITIVE" -> assertEquals("amount", error.field)
                "TRANSACTION_CATEGORY_EMPTY" -> assertEquals("category", error.field)
                "TRANSACTION_TIMESTAMP_FUTURE" -> assertEquals("timestamp", error.field)
            }
        }
    }
}
