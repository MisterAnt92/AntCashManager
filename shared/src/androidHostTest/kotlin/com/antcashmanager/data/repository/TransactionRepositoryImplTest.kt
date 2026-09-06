package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.SuggestionRow
import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.local.entity.TransactionEntity
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.security.LocalDataCipher
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryImplTest {

    private lateinit var fakeDao: FakeTransactionDao
    private lateinit var fakeCipher: FakeLocalDataCipher
    private lateinit var fakeWidgetUpdateNotifier: FakeWidgetUpdateNotifier
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeTransactionDao()
        fakeCipher = FakeLocalDataCipher()
        fakeWidgetUpdateNotifier = FakeWidgetUpdateNotifier()
        repository = TransactionRepositoryImpl(fakeDao, fakeCipher, fakeWidgetUpdateNotifier)
    }

    @Test
    fun insertTransaction_shouldEncryptSensitiveFields_whenSavingTransaction() = runTest {
        val transaction = sampleTransaction()

        repository.insertTransaction(transaction)

        val stored = fakeDao.lastInserted ?: error("Inserted entity not captured")
        assertTrue(stored.title.startsWith(FakeLocalDataCipher.PREFIX))
        assertTrue(stored.notes.startsWith(FakeLocalDataCipher.PREFIX))
        assertTrue(stored.payee.startsWith(FakeLocalDataCipher.PREFIX))
        assertTrue(stored.location.startsWith(FakeLocalDataCipher.PREFIX))
        assertTrue(stored.tags.startsWith(FakeLocalDataCipher.PREFIX))
    }

    @Test
    fun getAllTransactions_shouldDecryptSensitiveFields_whenLoadingTransactions() = runTest {
        val encryptedEntity = sampleEntity(
            title = fakeCipher.encryptString("Spesa supermercato"),
            notes = fakeCipher.encryptString("Promo weekend"),
            payee = fakeCipher.encryptString("Super Market"),
            location = fakeCipher.encryptString("Roma"),
            tags = fakeCipher.encryptString("food,weekend"),
        )
        fakeDao.transactionsFlow.value = listOf(encryptedEntity)

        val result = repository.getAllTransactions().first().first()

        assertEquals("Spesa supermercato", result.title)
        assertEquals("Promo weekend", result.notes)
        assertEquals("Super Market", result.payee)
        assertEquals("Roma", result.location)
        assertEquals("food,weekend", result.tags)
    }

    @Test
    fun getDistinctTitles_shouldReturnDecryptedDistinctValues_whenDatabaseHasEncryptedDuplicates() =
        runTest {
            val encrypted = fakeCipher.encryptString("Stipendio")
            fakeDao.distinctTitlesFlow.value = listOf(encrypted, encrypted)

            val values = repository.getDistinctTitles(since = 0L).first()

            assertEquals(listOf("Stipendio"), values)
        }

    @Test
    fun getDistinctTitles_shouldForwardSinceParameterToDao_whenCalledWithCutoff() = runTest {
        val values = repository.getDistinctTitles(since = 5_000L).first()

        assertEquals(5_000L, fakeDao.lastDistinctTitlesSince)
        assertEquals(emptyList<String>(), values)
    }

    @Test
    fun insertTransaction_shouldNotifyWidgetUpdate_whenTransactionSaved() = runTest {
        repository.insertTransaction(sampleTransaction())

        assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)
    }

    @Test
    fun updateTransaction_shouldNotifyWidgetUpdate_whenTransactionModified() = runTest {
        repository.updateTransaction(sampleTransaction())

        assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)
    }

    @Test
    fun deleteTransaction_shouldNotifyWidgetUpdate_whenTransactionRemoved() = runTest {
        repository.deleteTransaction(sampleTransaction())

        assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)
    }

     @Test
     fun deleteAllTransactions_shouldNotifyWidgetUpdate_whenAllTransactionsCleared() = runTest {
         repository.deleteAllTransactions()

         assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)
     }

     @Test
     fun getDistinctPayees_shouldReturnDecryptedDistinctValues_whenCalledWithSince() = runTest {
         val encrypted1 = fakeCipher.encryptString("Supermarket")
         val encrypted2 = fakeCipher.encryptString("Pharmacy")
         fakeDao.distinctPayeesFlow.value = listOf(encrypted1, encrypted2)

         val values = repository.getDistinctPayees(since = 1_000L).first()

         assertEquals(listOf("Supermarket", "Pharmacy"), values)
         assertEquals(1_000L, fakeDao.lastDistinctPayeesSince)
     }

     @Test
     fun getDistinctNotes_shouldReturnDecryptedDistinctValues_whenCalledWithSince() = runTest {
         val encrypted = fakeCipher.encryptString("Weekend promotion")
         fakeDao.distinctNotesFlow.value = listOf(encrypted)

         val values = repository.getDistinctNotes(since = 2_000L).first()

         assertEquals(listOf("Weekend promotion"), values)
         assertEquals(2_000L, fakeDao.lastDistinctNotesSince)
     }

     @Test
     fun getDistinctLocations_shouldReturnDecryptedDistinctValues_whenCalledWithSince() = runTest {
         val encrypted1 = fakeCipher.encryptString("Rome")
         val encrypted2 = fakeCipher.encryptString("Milan")
         fakeDao.distinctLocationsFlow.value = listOf(encrypted1, encrypted2)

         val values = repository.getDistinctLocations(since = 3_000L).first()

         assertEquals(listOf("Rome", "Milan"), values)
         assertEquals(3_000L, fakeDao.lastDistinctLocationsSince)
     }

     @Test
     fun getDistinctTags_shouldReturnDecryptedDistinctValues_whenCalledWithSince() = runTest {
         val encrypted = fakeCipher.encryptString("food,weekend")
         fakeDao.distinctTagsFlow.value = listOf(encrypted)

         val values = repository.getDistinctTags(since = 4_000L).first()

         assertEquals(listOf("food,weekend"), values)
         assertEquals(4_000L, fakeDao.lastDistinctTagsSince)
     }

     @Test
     fun getTransactionsByDateRange_shouldFilterByTimestampBoundaries_whenCalledWithDateRange() = runTest {
         val from = 1_000L
         val to = 5_000L
         val entity1 = sampleEntity("Title1", "", "", "", "")
             .copy(timestamp = 2_000L)
         val entity2 = sampleEntity("Title2", "", "", "", "")
             .copy(timestamp = 3_500L)
         val entity3 = sampleEntity("Title3", "", "", "", "")
             .copy(timestamp = 6_000L) // Out of range
         fakeDao.transactionsFlow.value = listOf(entity1, entity2, entity3)

         val result = repository.getTransactionsByDateRange(from, to).first()

         assertEquals(2, result.size)
         assertEquals("Title1", result[0].title)
         assertEquals("Title2", result[1].title)
     }

     @Test
     fun getRecurringTransactions_shouldReturnOnlyRecurringTransactions_whenQueried() = runTest {
         val recurring = sampleEntity("Rent", "", "", "", "")
             .copy(isRecurring = true, recurrenceInterval = "MONTHLY")
         val nonRecurring = sampleEntity("Groceries", "", "", "", "")
             .copy(isRecurring = false)
         fakeDao.transactionsFlow.value = listOf(recurring, nonRecurring)

         val result = repository.getRecurringTransactions().first()

         assertEquals(1, result.size)
         assertEquals("Rent", result.first().title)
         assertEquals(true, result.first().isRecurring)
     }

     @Test
     fun insertTransactions_shouldBatchInsertAndNotifyWidget_whenMultipleTransactionsProvided() = runTest {
         val transactions = listOf(
             sampleTransaction().copy(id = 1L, title = "Trans1"),
             sampleTransaction().copy(id = 2L, title = "Trans2"),
             sampleTransaction().copy(id = 3L, title = "Trans3"),
         )

         repository.insertTransactions(transactions)

         assertEquals(3, fakeDao.lastBatchInserted?.size)
         assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)
     }

     @Test
     fun updateTransactions_shouldBatchUpdateAndNotifyWidget_whenMultipleTransactionsProvided() = runTest {
         val transactions = listOf(
             sampleTransaction().copy(id = 1L, title = "Updated1"),
             sampleTransaction().copy(id = 2L, title = "Updated2"),
         )

         repository.updateTransactions(transactions)

         assertEquals(2, fakeDao.lastBatchUpdated?.size)
         assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)
     }

     @Test
     fun renameCategory_shouldUpdateCategoryInAllTransactions_whenCategoryRenamed() = runTest {
         val oldName = "Food"
         val newName = "Groceries"
         val oldEntity = sampleEntity("Lunch", "", "", "", "").copy(category = oldName)
         val otherEntity = sampleEntity("Rent", "", "", "", "").copy(category = "Housing")
         fakeDao.transactionsFlow.value = listOf(oldEntity, otherEntity)

         repository.renameCategory(oldName, newName, "restaurant", 0xFF90A4AE)

         val updatedOld = fakeDao.transactionsFlow.value.find { it.category == newName }
         assertEquals(newName, updatedOld?.category)
         assertEquals("restaurant", updatedOld?.categoryIcon)
     }

     @Test
     fun getSuggestions_shouldReturnDecryptedSuggestionsWithoutDuplicates_whenTransactionsHaveSensitiveFields() = runTest {
         val encrypted1 = fakeCipher.encryptString("Lunch")
         val encrypted2 = fakeCipher.encryptString("Pizza")
         val entity1 = sampleEntity(encrypted1, "", "", "", "")
             .copy(timestamp = 5_000L)
         val entity2 = sampleEntity(encrypted2, "", "", "", "")
             .copy(timestamp = 6_000L)
         fakeDao.transactionsFlow.value = listOf(entity1, entity2)

         val result = repository.getSuggestions(since = 3_000L)

         assertEquals(2, result.titles.size)
     }

     @Test
     fun getSuggestions_shouldFilterBySince_whenCalledWithTimestampCutoff() = runTest {
         val encrypted1 = fakeCipher.encryptString("OldTitle")
         val encrypted2 = fakeCipher.encryptString("NewTitle")
         val entity1 = sampleEntity(encrypted1, "", "", "", "").copy(timestamp = 1_000L)
         val entity2 = sampleEntity(encrypted2, "", "", "", "").copy(timestamp = 5_000L)
         fakeDao.transactionsFlow.value = listOf(entity1, entity2)

         val result = repository.getSuggestions(since = 3_000L)

         assertEquals(1, result.titles.size)
     }

     @Test
     fun getSuggestions_shouldReturnEmptyList_whenAllFieldsAreEmpty() = runTest {
         val entity = sampleEntity("", "", "", "", "")
         fakeDao.transactionsFlow.value = listOf(entity)

         val result = repository.getSuggestions(since = 0L)

         assertEquals(0, result.titles.size)
     }

     private fun sampleTransaction() = Transaction(
        id = 1L,
        title = "Spesa supermercato",
        amount = 100.5,
        category = "Food",
        type = TransactionType.EXPENSE,
        timestamp = 1_715_000_000_000,
        notes = "Promo weekend",
        payee = "Super Market",
        location = "Roma",
        isRecurring = false,
        tags = "food,weekend",
        recurrenceInterval = "",
        paymentType = PaymentType.ELECTRONIC,
        categoryIcon = "shopping_cart",
        categoryColor = 0xFF90A4AE,
    )

    private fun sampleEntity(
        title: String,
        notes: String,
        payee: String,
        location: String,
        tags: String,
    ) = TransactionEntity(
        id = 1L,
        title = title,
        amount = 100.5,
        category = "Food",
        type = "EXPENSE",
        timestamp = 1_715_000_000_000,
        notes = notes,
        payee = payee,
        location = location,
        isRecurring = false,
        tags = tags,
        recurrenceInterval = "",
        paymentType = PaymentType.ELECTRONIC.name,
        categoryIcon = "shopping_cart",
        categoryColor = 0xFF90A4AE,
    )
}

private class FakeLocalDataCipher : LocalDataCipher {
    companion object {
        const val PREFIX = "enc:"
    }

    override fun encryptString(value: String): String =
        if (value.startsWith(PREFIX) || value.isEmpty()) value else "$PREFIX$value"

    override fun decryptString(value: String): String =
        if (value.startsWith(PREFIX)) value.removePrefix(PREFIX) else value

    override fun clearCache() = Unit

    override fun isEncryptionEnabled(): Boolean = false
}

private class FakeWidgetUpdateNotifier : WidgetUpdateNotifier {
    var notifyCount = 0
        private set

    override suspend fun notifyTransactionsChanged() {
        notifyCount++
    }
}

private class FakeTransactionDao : TransactionDao {
     val transactionsFlow = MutableStateFlow<List<TransactionEntity>>(emptyList())
     val distinctTitlesFlow = MutableStateFlow<List<String>>(emptyList())
     val distinctPayeesFlow = MutableStateFlow<List<String>>(emptyList())
     val distinctNotesFlow = MutableStateFlow<List<String>>(emptyList())
     val distinctLocationsFlow = MutableStateFlow<List<String>>(emptyList())
     val distinctTagsFlow = MutableStateFlow<List<String>>(emptyList())

     var lastInserted: TransactionEntity? = null
     var lastDistinctTitlesSince: Long? = null
     var lastDistinctPayeesSince: Long? = null
     var lastDistinctNotesSince: Long? = null
     var lastDistinctLocationsSince: Long? = null
     var lastDistinctTagsSince: Long? = null
     var lastBatchInserted: List<TransactionEntity>? = null
     var lastBatchUpdated: List<TransactionEntity>? = null

    override fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionsFlow

    override suspend fun getCount(): Int = transactionsFlow.value.size

    override suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionsFlow.value.firstOrNull { it.id == id }

    override suspend fun insertTransaction(transaction: TransactionEntity): Long {
        lastInserted = transaction
        transactionsFlow.value = transactionsFlow.value + transaction
        return transaction.id
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionsFlow.value = transactionsFlow.value.map { current ->
            if (current.id == transaction.id) transaction else current
        }
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionsFlow.value = transactionsFlow.value.filterNot { it.id == transaction.id }
    }

    override suspend fun deleteAllTransactions() {
        transactionsFlow.value = emptyList()
    }

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<TransactionEntity>> =
        flowOf(transactionsFlow.value.filter { it.timestamp in from..to })

    override fun getRecurringTransactions(): Flow<List<TransactionEntity>> =
        flowOf(transactionsFlow.value.filter { it.isRecurring })

    override fun getTransactionsPaginated(limit: Int, offset: Int): Flow<List<TransactionEntity>> =
        flowOf(transactionsFlow.value.sortedByDescending { it.timestamp }.drop(offset).take(limit))

    override fun getTransactionsByCategory(category: String, limit: Int, offset: Int): Flow<List<TransactionEntity>> =
        flowOf(transactionsFlow.value.filter { it.category == category }.sortedByDescending { it.timestamp }.drop(offset).take(limit))

    override fun getTransactionsByCategoryAndDateRange(
        category: String,
        from: Long,
        to: Long,
        limit: Int,
        offset: Int
    ): Flow<List<TransactionEntity>> =
        flowOf(transactionsFlow.value.filter { it.category == category && it.timestamp in from..to }.sortedByDescending { it.timestamp }.drop(offset).take(limit))

    override fun searchTransactions(query: String, limit: Int, offset: Int): Flow<List<TransactionEntity>> =
        flowOf(transactionsFlow.value.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.payee.contains(query, ignoreCase = true) ||
            it.notes.contains(query, ignoreCase = true)
        }.sortedByDescending { it.timestamp }.drop(offset).take(limit))

    override suspend fun renameCategory(
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long
    ) {
        transactionsFlow.value = transactionsFlow.value.map { current ->
            if (current.category == oldCategoryName) {
                current.copy(category = newCategoryName, categoryIcon = icon, categoryColor = color)
            } else {
                current
            }
        }
    }

     override fun getDistinctTitles(since: Long): Flow<List<String>> {
         lastDistinctTitlesSince = since
         return distinctTitlesFlow
     }

     override fun getDistinctPayees(since: Long): Flow<List<String>> {
         lastDistinctPayeesSince = since
         return distinctPayeesFlow
     }

     override fun getDistinctNotes(since: Long): Flow<List<String>> {
         lastDistinctNotesSince = since
         return distinctNotesFlow
     }

     override fun getDistinctLocations(since: Long): Flow<List<String>> {
         lastDistinctLocationsSince = since
         return distinctLocationsFlow
     }

     override fun getDistinctTags(since: Long): Flow<List<String>> {
         lastDistinctTagsSince = since
         return distinctTagsFlow
     }

     override suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long> {
         lastBatchInserted = transactions
         transactionsFlow.value = transactionsFlow.value + transactions
         return transactions.map { it.id }
     }

     override suspend fun updateTransactions(transactions: List<TransactionEntity>) {
         lastBatchUpdated = transactions
         transactionsFlow.value = transactionsFlow.value.map { current ->
             transactions.firstOrNull { it.id == current.id } ?: current
         }
     }

    override suspend fun getSuggestions(since: Long): List<SuggestionRow> {
        return transactionsFlow.value
            .filter { it.timestamp >= since }
            .filter { it.title.isNotEmpty() || it.payee.isNotEmpty() || it.notes.isNotEmpty() || it.location.isNotEmpty() || it.tags.isNotEmpty() }
            .distinctBy { it.title + it.payee + it.notes + it.location + it.tags }
            .map { SuggestionRow(it.title, it.payee, it.notes, it.location, it.tags) }
    }
}

