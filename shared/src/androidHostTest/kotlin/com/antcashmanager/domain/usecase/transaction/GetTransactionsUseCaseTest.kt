package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.testutil.FakeTransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test per [GetTransactionsUseCase] con dispatcher injection e cancellazione.
 */
@OptIn(ExperimentalCoroutinesApi::class)
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
    fun invokeReturnsAllTransactionsFromRepository() = runTest {
        val result = useCase().first().getOrThrow()

        assertEquals(2, result.size)
        assertEquals("Salary", result[0].title)
        assertEquals("Groceries", result[1].title)
    }

    @Test
    fun invokeReturnsEmptyListWhenRepositoryHasNoTransactions() = runTest {
        val emptyRepo = FakeTransactionRepository()
        val emptyUseCase =
            GetTransactionsUseCase(emptyRepo, UnconfinedTestDispatcher(testDispatcher.scheduler))

        val result = emptyUseCase().first().getOrThrow()

        assertTrue(result.isEmpty())
    }

    @Test
    fun returnedTransactionsContainCorrectTypes() = runTest {
        val result = useCase().first().getOrThrow()

        assertEquals(TransactionType.INCOME, result[0].type)
        assertEquals(TransactionType.EXPENSE, result[1].type)
    }

    // ── Reattività ───────────────────────────────────────────────────────────

    @Test
    fun invokeReflectsRepositoryUpdatesReactively() = runTest(testDispatcher) {
        val reactiveRepo = FakeTransactionRepository()
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
    fun invokeWithStandardTestDispatcherRequiresAdvanceUntilIdle() = runTest(testDispatcher) {
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
    fun flowCollection_shouldBeCancellable() = runTest(testDispatcher) {
        val repository = FakeTransactionRepository(sampleTransactions)
        val cancellableUseCase = GetTransactionsUseCase(repository, testDispatcher)

        val job = launch { cancellableUseCase().collect { } }
        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
    }
}
