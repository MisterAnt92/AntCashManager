package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)

/**
 * Test per [DeleteAllTransactionsUseCase] con dispatcher injection e cancellazione.
 */
class DeleteAllTransactionsUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeDeleteAllRepo
    private lateinit var useCase: DeleteAllTransactionsUseCase

    @Before
    fun setup() {
        fakeRepo = FakeDeleteAllRepo()
        useCase = DeleteAllTransactionsUseCase(fakeRepo, testDispatcher)
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun `invoke deletes all transactions`() = runTest(testDispatcher) {
        fakeRepo.hasData = true

        useCase()

        assertTrue(fakeRepo.deleteAllCalled)
        assertFalse(fakeRepo.hasData)
    }

    @Test
    fun `invoke is idempotent on empty repository`() = runTest(testDispatcher) {
        useCase()
        useCase()

        assertEquals(2, fakeRepo.deleteAllCallCount)
    }

    // ── Error Handling ───────────────────────────────────────────────────────

    @Test
    fun `invoke returns failure Result when repository throws`() = runTest(testDispatcher) {
        fakeRepo.shouldThrow = true

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    // ── Cancellazione ────────────────────────────────────────────────────────

    @Test
    fun `invoke should be cancellable`() = runTest(testDispatcher) {
        val slowRepo = SlowDeleteRepo(delayMs = 10_000L)
        val cancellableUseCase = DeleteAllTransactionsUseCase(slowRepo, testDispatcher)

        val job: Job = launch { cancellableUseCase() }
        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertFalse(slowRepo.deleteAllCalled)
    }
}

// ── Fake Repositories ────────────────────────────────────────────────────────

private class FakeDeleteAllRepo : TransactionRepository {
    var hasData = false
    var shouldThrow = false
    var deleteAllCalled = false
    var deleteAllCallCount = 0

    override suspend fun deleteAllTransactions() {
        if (shouldThrow) throw RuntimeException("DB error")
        deleteAllCalled = true
        deleteAllCallCount++
        hasData = false
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun insertTransaction(transaction: Transaction): Long = 0L
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
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

private class SlowDeleteRepo(private val delayMs: Long) : TransactionRepository {
    var deleteAllCalled = false

    override suspend fun deleteAllTransactions() {
        delay(delayMs)
        deleteAllCalled = true
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun insertTransaction(transaction: Transaction): Long = 0L
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
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

