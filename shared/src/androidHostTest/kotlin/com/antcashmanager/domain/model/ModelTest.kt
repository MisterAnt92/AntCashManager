package com.antcashmanager.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TransactionTest {
    @Test
    fun transactionWithDefaultIdIsZero() {
        val transaction =
            Transaction(
                title = "Test",
                amount = 100.0,
                category = "Misc",
                type = TransactionType.INCOME,
            )
        assertEquals(0L, transaction.id)
    }

    @Test
    fun transactionsWithSameDataAreEqual() {
        val t1 =
            Transaction(
                id = 1L,
                title = "Test",
                amount = 100.0,
                category = "Misc",
                type = TransactionType.INCOME,
                timestamp = 1000L,
            )
        val t2 =
            Transaction(
                id = 1L,
                title = "Test",
                amount = 100.0,
                category = "Misc",
                type = TransactionType.INCOME,
                timestamp = 1000L,
            )
        assertEquals(t1, t2)
    }

    @Test
    fun transactionsWithDifferentIdsAreNotEqual() {
        val t1 =
            Transaction(
                id = 1L,
                title = "Test",
                amount = 100.0,
                category = "Misc",
                type = TransactionType.INCOME,
                timestamp = 1000L,
            )
        val t2 = t1.copy(id = 2L)
        assertNotEquals(t1, t2)
    }

    @Test
    fun copyPreservesAllFields() {
        val original =
            Transaction(
                id = 1L,
                title = "Original",
                amount = 100.0,
                category = "Misc",
                type = TransactionType.INCOME,
                timestamp = 1000L,
            )
        val copy = original.copy(title = "Updated")

        assertEquals("Updated", copy.title)
        assertEquals(original.id, copy.id)
        assertEquals(original.amount, copy.amount, 0.001)
        assertEquals(original.category, copy.category)
        assertEquals(original.type, copy.type)
        assertEquals(original.timestamp, copy.timestamp)
    }
}

class TransactionTypeTest {
    @Test
    fun transactionTypeHasExactlyTwoValues() {
        assertEquals(2, TransactionType.entries.size)
    }

    @Test
    fun transactionTypeValuesAreINCOMEAndEXPENSE() {
        val values = TransactionType.entries.map { it.name }
        assertEquals(listOf("INCOME", "EXPENSE"), values)
    }

    @Test
    fun valueOfReturnsCorrectEnum() {
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"))
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"))
    }
}

class AppThemeTest {
    @Test
    fun appThemeHasExactlyThreeValues() {
        assertEquals(3, AppTheme.entries.size)
    }

    @Test
    fun appThemeValuesAreLIGHTDARKSYSTEM() {
        val values = AppTheme.entries.map { it.name }
        assertEquals(listOf("LIGHT", "DARK", "SYSTEM"), values)
    }
}
