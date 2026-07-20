package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.testutil.FakeTransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
 * Test per [InsertTransactionUseCase] con dispatcher injection e cancellazione.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InsertTransactionUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTransactionRepository
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
        fakeRepo = FakeTransactionRepository()
        useCase =
            InsertTransactionUseCase(fakeRepo, UnconfinedTestDispatcher(testDispatcher.scheduler))
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun invokeInsertsTransactionAndReturnsId() = runTest {
        val resultId = useCase(testTransaction).getOrThrow()

        assertEquals(1L, resultId)
        assertTrue(fakeRepo.transactions.value.any { it.title == testTransaction.title })
    }

    @Test
    fun invokeInsertsMultipleTransactionsWithIncrementalIds() = runTest {
        val t1 = testTransaction.copy(title = "T1")
        val t2 = testTransaction.copy(title = "T2")

        val id1 = useCase(t1).getOrThrow()
        val id2 = useCase(t2).getOrThrow()

        assertEquals(1L, id1)
        assertEquals(2L, id2)
        assertEquals(2, fakeRepo.transactions.value.size)
    }

    // ── Dispatcher Injection ─────────────────────────────────────────────────

    @Test
    fun invokeWithStandardTestDispatcherNeedsAdvanceUntilIdle() = runTest(testDispatcher) {
        val standardUseCase = InsertTransactionUseCase(fakeRepo, testDispatcher)
        var resultId = 0L

        val job = launch { resultId = standardUseCase(testTransaction).getOrThrow() }
        advanceUntilIdle()

        assertEquals(1L, resultId)
        job.join()
    }

    // ── Error Handling ───────────────────────────────────────────────────────

    @Test
    fun invokeReturnsFailureResultWhenRepositoryThrows() = runTest {
        fakeRepo.errorToThrow = RuntimeException("DB error")

        val result = useCase(testTransaction)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    // ── Cancellazione ────────────────────────────────────────────────────────

    @Test
    fun invoke_shouldBeCancellableBeforeCompletion() = runTest(testDispatcher) {
        fakeRepo.operationDelayMs = 10_000L
        val cancellableUseCase = InsertTransactionUseCase(fakeRepo, testDispatcher)

        val job: Job = launch { cancellableUseCase(testTransaction) }
        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertTrue(fakeRepo.transactions.value.isEmpty())
    }
}
