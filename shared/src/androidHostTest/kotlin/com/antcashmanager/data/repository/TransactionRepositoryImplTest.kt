package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.local.entity.TransactionEntity
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.security.LocalDataCipher
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
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeTransactionDao()
        fakeCipher = FakeLocalDataCipher()
        repository = TransactionRepositoryImpl(fakeDao, fakeCipher)
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
    fun getDistinctTitles_shouldReturnDecryptedDistinctValues_whenDatabaseHasEncryptedDuplicates() = runTest {
        val encrypted = fakeCipher.encryptString("Stipendio")
        fakeDao.distinctTitlesFlow.value = listOf(encrypted, encrypted)

        val values = repository.getDistinctTitles().first()

        assertEquals(listOf("Stipendio"), values)
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
}

private class FakeTransactionDao : TransactionDao {
    val transactionsFlow = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val distinctTitlesFlow = MutableStateFlow<List<String>>(emptyList())

    var lastInserted: TransactionEntity? = null

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

    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) {
        transactionsFlow.value = transactionsFlow.value.map { current ->
            if (current.category == oldCategoryName) {
                current.copy(category = newCategoryName, categoryIcon = icon, categoryColor = color)
            } else {
                current
            }
        }
    }

    override fun getDistinctTitles(): Flow<List<String>> = distinctTitlesFlow

    override fun getDistinctPayees(): Flow<List<String>> = flowOf(emptyList())

    override fun getDistinctNotes(): Flow<List<String>> = flowOf(emptyList())

    override fun getDistinctLocations(): Flow<List<String>> = flowOf(emptyList())

    override fun getDistinctTags(): Flow<List<String>> = flowOf(emptyList())
}

