package com.antcashmanager.data.mapper

import com.antcashmanager.data.local.entity.TransactionEntity
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionMapperTest {

    @Test
    fun `entity to domain maps correctly for INCOME`() {
        val entity = TransactionEntity(
            id = 1L,
            title = "Salary",
            amount = 2500.0,
            category = "Work",
            type = "INCOME",
            timestamp = 1000L,
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("Salary", domain.title)
        assertEquals(2500.0, domain.amount, 0.001)
        assertEquals("Work", domain.category)
        assertEquals(TransactionType.INCOME, domain.type)
        assertEquals(1000L, domain.timestamp)
    }

    @Test
    fun `entity to domain maps correctly for EXPENSE`() {
        val entity = TransactionEntity(
            id = 2L,
            title = "Groceries",
            amount = 85.50,
            category = "Food",
            type = "EXPENSE",
            timestamp = 2000L,
        )

        val domain = entity.toDomain()

        assertEquals(2L, domain.id)
        assertEquals("Groceries", domain.title)
        assertEquals(85.50, domain.amount, 0.001)
        assertEquals("Food", domain.category)
        assertEquals(TransactionType.EXPENSE, domain.type)
        assertEquals(2000L, domain.timestamp)
    }

    @Test
    fun `domain to entity maps correctly for INCOME`() {
        val domain = Transaction(
            id = 1L,
            title = "Salary",
            amount = 2500.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = 1000L,
        )

        val entity = domain.toEntity()

        assertEquals(1L, entity.id)
        assertEquals("Salary", entity.title)
        assertEquals(2500.0, entity.amount, 0.001)
        assertEquals("Work", entity.category)
        assertEquals("INCOME", entity.type)
        assertEquals(1000L, entity.timestamp)
    }

    @Test
    fun `domain to entity maps correctly for EXPENSE`() {
        val domain = Transaction(
            id = 2L,
            title = "Rent",
            amount = 800.0,
            category = "Housing",
            type = TransactionType.EXPENSE,
            timestamp = 2000L,
        )

        val entity = domain.toEntity()

        assertEquals(2L, entity.id)
        assertEquals("Rent", entity.title)
        assertEquals(800.0, entity.amount, 0.001)
        assertEquals("Housing", entity.category)
        assertEquals("EXPENSE", entity.type)
        assertEquals(2000L, entity.timestamp)
    }

    @Test
    fun `round-trip entity to domain to entity preserves data`() {
        val original = TransactionEntity(
            id = 5L,
            title = "Test",
            amount = 100.0,
            category = "Misc",
            type = "INCOME",
            timestamp = 5000L,
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `round-trip domain to entity to domain preserves data`() {
        val original = Transaction(
            id = 5L,
            title = "Test",
            amount = 100.0,
            category = "Misc",
            type = TransactionType.EXPENSE,
            timestamp = 5000L,
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `entity with zero id maps correctly`() {
        val entity = TransactionEntity(
            id = 0L,
            title = "New Transaction",
            amount = 50.0,
            category = "Other",
            type = "EXPENSE",
            timestamp = 3000L,
        )

        val domain = entity.toDomain()

        assertEquals(0L, domain.id)
    }
}

