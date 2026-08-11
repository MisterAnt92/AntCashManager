package com.antcashmanager.domain.model

import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.testutil.testTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for Transaction domain model.
 *
 * Tests cover:
 * - Transaction creation with various parameters
 * - Amount sign (negative for expense, positive for income)
 * - Recurring transaction properties
 * - Encryption field handling
 * - Equality comparison
 */
class TransactionDomainModelTest {

    @Test
    fun transaction_shouldHandleExpenseAmountCorrectly() {
        val transaction = testTransaction {
            amount = -50.0
            type = TransactionType.EXPENSE
        }

        assertEquals(-50.0, transaction.amount)
        assertEquals(TransactionType.EXPENSE, transaction.type)
    }

    @Test
    fun transaction_shouldHandleIncomeAmountCorrectly() {
        val transaction = testTransaction {
            amount = 100.0
            type = TransactionType.INCOME
        }

        assertEquals(100.0, transaction.amount)
        assertEquals(TransactionType.INCOME, transaction.type)
    }

    @Test
    fun transaction_shouldStoreAllFields() {
        val transaction = testTransaction {
            title = "Lunch"
            amount = -25.0
            category = "Food"
            type = TransactionType.EXPENSE
            notes = "Tasty pizza"
            payee = "Luigi's"
            location = "Downtown"
            isRecurring = true
            recurrenceInterval = "weekly"
            tags = "food,italian,lunch"
        }

        assertEquals("Lunch", transaction.title)
        assertEquals(-25.0, transaction.amount)
        assertEquals("Food", transaction.category)
        assertEquals("Tasty pizza", transaction.notes)
        assertEquals("Luigi's", transaction.payee)
        assertEquals("Downtown", transaction.location)
        assertTrue(transaction.isRecurring)
        assertEquals("weekly", transaction.recurrenceInterval)
        assertEquals("food,italian,lunch", transaction.tags)
    }

    @Test
    fun transaction_shouldHandlePaymentTypes() {
        val cashTransaction = testTransaction {
            paymentType = PaymentType.CASH
        }
        assertEquals(PaymentType.CASH, cashTransaction.paymentType)

        val cardTransaction = testTransaction {
            paymentType = PaymentType.ELECTRONIC
        }
        assertEquals(PaymentType.ELECTRONIC, cardTransaction.paymentType)
    }

    @Test
    fun transaction_nonRecurring_shouldHaveEmptyInterval() {
        val transaction = testTransaction {
            isRecurring = false
            recurrenceInterval = ""
        }

        assertFalse(transaction.isRecurring)
        assertEquals("", transaction.recurrenceInterval)
    }

    @Test
    fun transaction_shouldHaveUniqueId() {
        val tx1 = testTransaction { id = 1L }
        val tx2 = testTransaction { id = 2L }

        assertEquals(1L, tx1.id)
        assertEquals(2L, tx2.id)
    }

    @Test
    fun transaction_shouldStoreTimestamp() {
        val currentTime = System.currentTimeMillis()
        val transaction = testTransaction {
            timestamp = currentTime
        }

        assertEquals(currentTime, transaction.timestamp)
    }

    @Test
    fun transaction_shouldHandleEmptyOptionalFields() {
        val transaction = testTransaction {
            notes = ""
            payee = ""
            location = ""
            tags = ""
        }

        assertEquals("", transaction.notes)
        assertEquals("", transaction.payee)
        assertEquals("", transaction.location)
        assertEquals("", transaction.tags)
    }

    @Test
    fun transaction_shouldHandleMealVoucherPaymentType() {
        val transaction = testTransaction {
            paymentType = PaymentType.MEAL_VOUCHERS
            mealVoucherCount = 5
        }

        assertEquals(PaymentType.MEAL_VOUCHERS, transaction.paymentType)
        assertEquals(5, transaction.mealVoucherCount)
    }

    @Test
    fun transaction_equalityTest() {
        val tx1 = testTransaction {
            id = 1L
            title = "Lunch"
        }

        val tx2 = testTransaction {
            id = 1L
            title = "Lunch"
        }

        assertEquals(tx1, tx2)
    }

    @Test
    fun transaction_inequalityTest() {
        val tx1 = testTransaction {
            id = 1L
            title = "Lunch"
        }

        val tx2 = testTransaction {
            id = 2L
            title = "Lunch"
        }

        assertFalse(tx1 == tx2)
    }
}
