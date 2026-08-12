package com.antcashmanager.data.mapper

import com.antcashmanager.data.local.entity.TransactionEntity
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionMapperTest {

    @Test
    fun entityToDomainMapsCorrectlyForINCOME() {
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
    fun entityToDomainMapsCorrectlyForEXPENSE() {
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
    fun domainToEntityMapsCorrectlyForINCOME() {
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
    fun domainToEntityMapsCorrectlyForEXPENSE() {
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
    fun roundTripEntityToDomainToEntityPreservesData() {
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
    fun roundTripDomainToEntityToDomainPreservesData() {
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
    fun entityWithZeroIdMapsCorrectly() {
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

    @Test
    fun toDomain_shouldPreserveMealVoucherCount_whenEntityHasNonZeroValue() {
        val entity = TransactionEntity(
            id = 6L,
            title = "Lunch",
            amount = 12.0,
            category = "Food",
            type = "EXPENSE",
            timestamp = 6000L,
            mealVoucherCount = 3,
        )

        val domain = entity.toDomain()

        assertEquals(3, domain.mealVoucherCount)
    }

    @Test
    fun toEntity_shouldPreserveMealVoucherCount_whenDomainHasNonZeroValue() {
        val domain = Transaction(
            id = 6L,
            title = "Lunch",
            amount = 12.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 6000L,
            mealVoucherCount = 3,
        )

        val entity = domain.toEntity()

        assertEquals(3, entity.mealVoucherCount)
    }

    @Test
    fun roundTrip_shouldPreserveMealVoucherCount_whenValueIsNonZero() {
        val original = TransactionEntity(
            id = 7L,
            title = "Dinner",
            amount = 20.0,
            category = "Food",
            type = "EXPENSE",
            timestamp = 7000L,
            mealVoucherCount = 2,
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }

     @Test
     fun toDomain_shouldFallbackToElectronicPaymentType_whenStoredValueIsInvalid() {
         val entity = TransactionEntity(
             id = 8L,
             title = "Corrupted",
             amount = 10.0,
             category = "Other",
             type = "EXPENSE",
             timestamp = 8000L,
             paymentType = "BOGUS",
         )

         val domain = entity.toDomain()

         assertEquals(PaymentType.ELECTRONIC, domain.paymentType)
     }

     @Test
     fun entityToDomain_shouldMapNotes_whenNotesFieldIsPresent() {
         val entity = TransactionEntity(
             id = 9L,
             title = "Pizza",
             amount = 15.0,
             category = "Food",
             type = "EXPENSE",
             timestamp = 9000L,
             notes = "Dinner with colleagues",
         )

         val domain = entity.toDomain()

         assertEquals("Dinner with colleagues", domain.notes)
     }

     @Test
     fun entityToDomain_shouldMapPayee_whenPayeeFieldIsPresent() {
         val entity = TransactionEntity(
             id = 10L,
             title = "Gas",
             amount = 60.0,
             category = "Transport",
             type = "EXPENSE",
             timestamp = 10000L,
             payee = "Shell Station",
         )

         val domain = entity.toDomain()

         assertEquals("Shell Station", domain.payee)
     }

     @Test
     fun entityToDomain_shouldMapLocation_whenLocationFieldIsPresent() {
         val entity = TransactionEntity(
             id = 11L,
             title = "Shopping",
             amount = 85.0,
             category = "Shopping",
             type = "EXPENSE",
             timestamp = 11000L,
             location = "Rome, Italy",
         )

         val domain = entity.toDomain()

         assertEquals("Rome, Italy", domain.location)
     }

     @Test
     fun entityToDomain_shouldMapTags_whenTagsFieldIsPresent() {
         val entity = TransactionEntity(
             id = 12L,
             title = "Lunch",
             amount = 12.0,
             category = "Food",
             type = "EXPENSE",
             timestamp = 12000L,
             tags = "work,business,lunch",
         )

         val domain = entity.toDomain()

         assertEquals("work,business,lunch", domain.tags)
     }

     @Test
     fun entityToDomain_shouldMapIsRecurring_whenRecurringFlagIsTrue() {
         val entity = TransactionEntity(
             id = 13L,
             title = "Netflix",
             amount = 12.99,
             category = "Entertainment",
             type = "EXPENSE",
             timestamp = 13000L,
             isRecurring = true,
             recurrenceInterval = "MONTHLY",
         )

         val domain = entity.toDomain()

         assertEquals(true, domain.isRecurring)
         assertEquals("MONTHLY", domain.recurrenceInterval)
     }

     @Test
     fun entityToDomain_shouldMapCategoryIcon_whenIconFieldIsPresent() {
         val entity = TransactionEntity(
             id = 14L,
             title = "Coffee",
             amount = 4.5,
             category = "Food",
             type = "EXPENSE",
             timestamp = 14000L,
             categoryIcon = "local_cafe",
         )

         val domain = entity.toDomain()

         assertEquals("local_cafe", domain.categoryIcon)
     }

     @Test
     fun entityToDomain_shouldMapCategoryColor_whenColorFieldIsPresent() {
         val entity = TransactionEntity(
             id = 15L,
             title = "Books",
             amount = 25.0,
             category = "Education",
             type = "EXPENSE",
             timestamp = 15000L,
             categoryColor = 0xFF5E35B1,
         )

         val domain = entity.toDomain()

         assertEquals(0xFF5E35B1, domain.categoryColor)
     }

     @Test
     fun domainToEntity_shouldMapNotesField_whenNotesAreProvided() {
         val domain = Transaction(
             id = 16L,
             title = "Rent",
             amount = 800.0,
             category = "Housing",
             type = TransactionType.EXPENSE,
             timestamp = 16000L,
             notes = "Monthly rent payment",
         )

         val entity = domain.toEntity()

         assertEquals("Monthly rent payment", entity.notes)
     }

     @Test
     fun domainToEntity_shouldMapPayeeField_whenPayeeIsProvided() {
         val domain = Transaction(
             id = 17L,
             title = "Groceries",
             amount = 50.0,
             category = "Food",
             type = TransactionType.EXPENSE,
             timestamp = 17000L,
             payee = "Tesco",
         )

         val entity = domain.toEntity()

         assertEquals("Tesco", entity.payee)
     }

     @Test
     fun domainToEntity_shouldMapAllExtendedFields_whenCompleteTransactionProvided() {
         val domain = Transaction(
             id = 18L,
             title = "Business Lunch",
             amount = 35.50,
             category = "Food",
             type = TransactionType.EXPENSE,
             timestamp = 18000L,
             notes = "Team lunch meeting",
             payee = "Restaurant ABC",
             location = "Downtown",
             tags = "business,expense,lunch",
             isRecurring = false,
             recurrenceInterval = "",
             paymentType = PaymentType.CASH,
             categoryIcon = "restaurant",
             categoryColor = 0xFFE91E63,
         )

         val entity = domain.toEntity()

         assertEquals(18L, entity.id)
         assertEquals("Business Lunch", entity.title)
         assertEquals(35.50, entity.amount, 0.001)
         assertEquals("Team lunch meeting", entity.notes)
         assertEquals("Restaurant ABC", entity.payee)
         assertEquals("Downtown", entity.location)
         assertEquals("business,expense,lunch", entity.tags)
         assertEquals(false, entity.isRecurring)
         assertEquals(PaymentType.CASH.name, entity.paymentType)
         assertEquals("restaurant", entity.categoryIcon)
         assertEquals(0xFFE91E63, entity.categoryColor)
     }

     @Test
     fun roundTrip_shouldPreserveAllExtendedFields_whenComplexTransactionMapped() {
         val original = Transaction(
             id = 19L,
             title = "Complex",
             amount = 99.99,
             category = "Other",
             type = TransactionType.EXPENSE,
             timestamp = 19000L,
             notes = "Complex transaction",
             payee = "Vendor XYZ",
             location = "Location ABC",
             tags = "tag1,tag2,tag3",
             isRecurring = true,
             recurrenceInterval = "WEEKLY",
             paymentType = PaymentType.ELECTRONIC,
             categoryIcon = "shopping_cart",
             categoryColor = 0xFF4CAF50,
         )

         val roundTripped = original.toEntity().toDomain()

         assertEquals(original, roundTripped)
     }

     @Test
     fun toDomain_shouldHandleEmptyOptionalFields_whenNotSet() {
         val entity = TransactionEntity(
             id = 20L,
             title = "Simple",
             amount = 10.0,
             category = "Other",
             type = "EXPENSE",
             timestamp = 20000L,
             notes = "",
             payee = "",
             location = "",
             tags = "",
             isRecurring = false,
             recurrenceInterval = "",
         )

         val domain = entity.toDomain()

         assertEquals("", domain.notes)
         assertEquals("", domain.payee)
         assertEquals("", domain.location)
         assertEquals("", domain.tags)
         assertEquals(false, domain.isRecurring)
     }
}

