package com.antcashmanager

import com.antcashmanager.data.local.entity.TransactionEntity
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionMapperTest {

    @Test
    fun `entity to domain and back preserves important fields`() {
        val entity = TransactionEntity(
            id = 42,
            title = "Test Tx",
            amount = 12.34,
            category = "Cibo",
            type = "EXPENSE",
            timestamp = 1620000000000,
            notes = "note",
            payee = "Mario",
            location = "Supermarket",
            isRecurring = true,
            tags = "food,groceries",
            recurrenceInterval = "MONTHLY",
            paymentType = "CASH",
        )

        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.amount, domain.amount, 0.001)
        assertEquals(entity.category, domain.category)
        assertEquals(TransactionType.EXPENSE, domain.type)
        assertEquals(entity.timestamp, domain.timestamp)
        assertEquals(entity.notes, domain.notes)
        assertEquals(entity.payee, domain.payee)
        assertEquals(entity.location, domain.location)
        assertEquals(entity.isRecurring, domain.isRecurring)
        // tags in domain model are stored as a comma-separated String
        assertEquals(entity.tags, domain.tags)
        assertEquals(entity.recurrenceInterval, domain.recurrenceInterval)
        assertEquals(PaymentType.CASH, domain.paymentType)

        val back = domain.toEntity()
        assertEquals(entity.title, back.title)
        assertEquals(entity.amount, back.amount, 0.001)
        assertEquals(entity.type, back.type)
        assertEquals(entity.notes, back.notes)
        assertEquals(entity.tags, back.tags)
        assertEquals(entity.paymentType, back.paymentType)
    }

    @Test
    fun `entity with invalid paymentType defaults to ELECTRONIC`() {
        val entity = TransactionEntity(
            id = 1,
            title = "Test",
            amount = 100.0,
            category = "Test",
            type = "INCOME",
            timestamp = System.currentTimeMillis(),
            paymentType = "INVALID_TYPE",
        )

        val domain = entity.toDomain()
        assertEquals(PaymentType.ELECTRONIC, domain.paymentType)
    }

    @Test
    fun `all payment types map correctly`() {
        val paymentTypes = listOf("ELECTRONIC", "CASH", "MEAL_VOUCHERS")
        val expectedTypes = listOf(PaymentType.ELECTRONIC, PaymentType.CASH, PaymentType.MEAL_VOUCHERS)

        paymentTypes.zip(expectedTypes).forEach { (stringType, expectedType) ->
            val entity = TransactionEntity(
                id = 1,
                title = "Test",
                amount = 100.0,
                category = "Test",
                type = "EXPENSE",
                timestamp = System.currentTimeMillis(),
                paymentType = stringType,
            )

            val domain = entity.toDomain()
            assertEquals(expectedType, domain.paymentType)

            val backToEntity = domain.toEntity()
            assertEquals(stringType, backToEntity.paymentType)
        }
    }
}

