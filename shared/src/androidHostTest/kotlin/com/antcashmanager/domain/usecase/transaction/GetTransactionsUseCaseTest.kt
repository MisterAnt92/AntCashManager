package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)

/**
 * Test per [GetTransactionsUseCase] con dispatcher injection e cancellazione.
 */
class GetTransactionsUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: GetTransactionsUseCase

    private val sampleTransactions = listOf(
        Transaction(
            id = 1L,
            title = "Salary",
            amount = 2500.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = 1000L,
        ),
        Transaction(
            id = 2L,
            title = "Groceries",
            amount = 85.50,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 2000L,
        ),
    )

    @Before
    fun setup() {
        val repository = FakeTransactionRepository(sampleTransactions)
        // UnconfinedTestDispatcher: emissione immediata senza advanceUntilIdle
        useCase =
            GetTransactionsUseCase(repository, UnconfinedTestDispatcher(testDispatcher.scheduler))
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun `invoke returns all transactions from repository`() = runTest {
        val result = useCase().first().getOrThrow()

        assertEquals(2, result.size)
        assertEquals("Salary", result[0].title)
        assertEquals("Groceries", result[1].title)
    }

    @Test
    fun `invoke returns empty list when repository has no transactions`() = runTest {
        val emptyRepo = FakeTransactionRepository(emptyList())
        val emptyUseCase =
            GetTransactionsUseCase(emptyRepo, UnconfinedTestDispatcher(testDispatcher.scheduler))

        val result = emptyUseCase().first().getOrThrow()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returned transactions contain correct types`() = runTest {
        val result = useCase().first().getOrThrow()

        assertEquals(TransactionType.INCOME, result[0].type)
        assertEquals(TransactionType.EXPENSE, result[1].type)
    }

    // ── Reattività ───────────────────────────────────────────────────────────

    @Test
    fun `invoke reflects repository updates reactively`() = runTest(testDispatcher) {
        val reactiveRepo = FakeReactiveTransactionRepository()
        val reactiveUseCase = GetTransactionsUseCase(
            reactiveRepo,
            UnconfinedTestDispatcher(testDispatcher.scheduler),
        )
        val collected = mutableListOf<List<Transaction>>()

        val job = launch { reactiveUseCase().collect { collected.add(it.getOrThrow()) } }
        advanceUntilIdle()

        // Aggiorna il repository
        reactiveRepo.transactions.value = sampleTransactions
        advanceUntilIdle()

        assertEquals(2, collected.size)
        assertTrue(collected[0].isEmpty())
        assertEquals(2, collected[1].size)
        job.cancel()
    }

    // ── Dispatcher Injection ─────────────────────────────────────────────────

    @Test
    fun `invoke with StandardTestDispatcher requires advanceUntilIdle`() = runTest(testDispatcher) {
        val repository = FakeTransactionRepository(sampleTransactions)
        val standardUseCase = GetTransactionsUseCase(repository, testDispatcher)
        var result: List<Transaction> = emptyList()

        val job = launch { standardUseCase().collect { result = it.getOrThrow() } }
        advanceUntilIdle()

        assertEquals(2, result.size)
        job.cancel()
    }

    // ── Cancellazione ────────────────────────────────────────────────────────

    @Test
    fun `flow collection should be cancellable`() = runTest(testDispatcher) {
        val repository = FakeTransactionRepository(sampleTransactions)
        val cancellableUseCase = GetTransactionsUseCase(repository, testDispatcher)

        val job = launch { cancellableUseCase().collect { } }
        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
    }
}

// ── Fake Repositories ────────────────────────────────────────────────────────

private class FakeTransactionRepository(
    private val transactions: List<Transaction>,
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(transactions)
    override suspend fun getTransactionById(id: Long): Transaction? =
        transactions.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long = transaction.id
    override suspend fun updateTransaction(transaction: Transaction) = Unit
    override suspend fun deleteTransaction(transaction: Transaction) = Unit
    override suspend fun deleteAllTransactions() = Unit
    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        flowOf(transactions.filter { it.timestamp in from..to })

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        flowOf(transactions.filter { it.isRecurring })

    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) {}
    override fun getDistinctTitles() = flowOf(emptyList<String>())
    override fun getDistinctPayees() = flowOf(emptyList<String>())
    override fun getDistinctNotes() = flowOf(emptyList<String>())
    override fun getDistinctLocations() = flowOf(emptyList<String>())
    override fun getDistinctTags() = flowOf(emptyList<String>())
}

private class FakeReactiveTransactionRepository : TransactionRepository {
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    override fun getAllTransactions(): Flow<List<Transaction>> = transactions
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun insertTransaction(transaction: Transaction): Long = 0L
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override suspend fun deleteAllTransactions() {
        transactions.value = emptyList()
    }

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
