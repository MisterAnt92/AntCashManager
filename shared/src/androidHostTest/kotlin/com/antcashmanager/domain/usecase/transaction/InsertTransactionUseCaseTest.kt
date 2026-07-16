package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)

/**
 * Test per [InsertTransactionUseCase] con dispatcher injection e cancellazione.
 */
class InsertTransactionUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeInsertTransactionRepository
    private lateinit var useCase: InsertTransactionUseCase

    private val testTransaction = Transaction(
        id = 0L,
        title = "New Income",
        amount = 1500.0,
        category = "Work",
        type = TransactionType.INCOME,
        timestamp = 1000L,
    )

    @Before
    fun setup() {
        fakeRepo = FakeInsertTransactionRepository()
        useCase =
            InsertTransactionUseCase(fakeRepo, UnconfinedTestDispatcher(testDispatcher.scheduler))
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun `invoke inserts transaction and returns id`() = runTest {
        val resultId = useCase(testTransaction).getOrThrow()

        assertEquals(1L, resultId)
        assertTrue(fakeRepo.insertedTransactions.contains(testTransaction))
    }

    @Test
    fun `invoke inserts multiple transactions with incremental ids`() = runTest {
        val t1 = testTransaction.copy(title = "T1")
        val t2 = testTransaction.copy(title = "T2")

        val id1 = useCase(t1).getOrThrow()
        val id2 = useCase(t2).getOrThrow()

        assertEquals(1L, id1)
        assertEquals(2L, id2)
        assertEquals(2, fakeRepo.insertedTransactions.size)
    }

    // ── Dispatcher Injection ─────────────────────────────────────────────────

    @Test
    fun `invoke with StandardTestDispatcher needs advanceUntilIdle`() = runTest(testDispatcher) {
        val standardUseCase = InsertTransactionUseCase(fakeRepo, testDispatcher)
        var resultId = 0L

        val job = launch { resultId = standardUseCase(testTransaction).getOrThrow() }
        advanceUntilIdle()

        assertEquals(1L, resultId)
        job.join()
    }

    // ── Error Handling ───────────────────────────────────────────────────────

    @Test
    fun `invoke returns failure Result when repository throws`() = runTest {
        fakeRepo.shouldThrow = true

        val result = useCase(testTransaction)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    // ── Cancellazione ────────────────────────────────────────────────────────

    @Test
    fun `invoke should be cancellable before completion`() = runTest(testDispatcher) {
        val slowRepo = SlowFakeInsertRepository(delayMs = 10_000L)
        val cancellableUseCase = InsertTransactionUseCase(slowRepo, testDispatcher)

        val job: Job = launch { cancellableUseCase(testTransaction) }
        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertFalse(slowRepo.insertCalled)
    }
}

// ── Fake Repositories ────────────────────────────────────────────────────────

private class FakeInsertTransactionRepository : TransactionRepository {
    val insertedTransactions = mutableListOf<Transaction>()
    private var nextId = 1L
    var shouldThrow = false

    override suspend fun insertTransaction(transaction: Transaction): Long {
        if (shouldThrow) throw RuntimeException("DB error")
        insertedTransactions.add(transaction)
        return nextId++
    }

    override fun getAllTransactions(): Flow<List<Transaction>> =
        flowOf(insertedTransactions.toList())

    override suspend fun getTransactionById(id: Long): Transaction? =
        insertedTransactions.find { it.id == id }

    override suspend fun updateTransaction(transaction: Transaction) = Unit
    override suspend fun deleteTransaction(transaction: Transaction) = Unit
    override suspend fun deleteAllTransactions() = Unit
    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        flowOf(insertedTransactions.filter { it.timestamp in from..to })

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        flowOf(insertedTransactions.filter { it.isRecurring })

    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) {}
    override fun getDistinctTitles() = flowOf(emptyList<String>())
    override fun getDistinctPayees() = flowOf(emptyList<String>())
    override fun getDistinctNotes() = flowOf(emptyList<String>())
    override fun getDistinctLocations() = flowOf(emptyList<String>())
    override fun getDistinctTags() = flowOf(emptyList<String>())
}

private class SlowFakeInsertRepository(private val delayMs: Long) : TransactionRepository {
    var insertCalled = false

    override suspend fun insertTransaction(transaction: Transaction): Long {
        delay(delayMs)
        insertCalled = true
        return 99L
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override suspend fun deleteAllTransactions() {}
    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        flowOf(emptyList())

    override fun getRecurringTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) {}
    override fun getDistinctTitles() = flowOf(emptyList<String>())
    override fun getDistinctPayees() = flowOf(emptyList<String>())
    override fun getDistinctNotes() = flowOf(emptyList<String>())
    override fun getDistinctLocations() = flowOf(emptyList<String>())
    override fun getDistinctTags() = flowOf(emptyList<String>())
}
