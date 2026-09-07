package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.testutil.FakeTransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test per [DeleteAllTransactionsUseCase] con dispatcher injection e cancellazione.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAllTransactionsUseCaseTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var useCase: DeleteAllTransactionsUseCase

    private val sampleTransaction =
        Transaction(
            id = 1L,
            title = "Spesa",
            amount = 10.0,
            category = "Cibo",
            type = TransactionType.EXPENSE,
        )

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        useCase = DeleteAllTransactionsUseCase(fakeRepo, testDispatcher)
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun invokeDeletesAllTransactions() =
        runTest(testDispatcher) {
            fakeRepo.transactions.value = listOf(sampleTransaction)

            useCase()

            assertTrue(fakeRepo.transactions.value.isEmpty())
        }

    @Test
    fun invokeIsIdempotentOnEmptyRepository() =
        runTest(testDispatcher) {
            val first = useCase()
            val second = useCase()

            assertTrue(first.isSuccess)
            assertTrue(second.isSuccess)
            assertTrue(fakeRepo.transactions.value.isEmpty())
        }

    // ── Error Handling ───────────────────────────────────────────────────────

    @Test
    fun invokeReturnsFailureResultWhenRepositoryThrows() =
        runTest(testDispatcher) {
            fakeRepo.errorToThrow = RuntimeException("DB error")

            val result = useCase()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is RuntimeException)
        }

    // ── Cancellazione ────────────────────────────────────────────────────────

    @Test
    fun invoke_shouldBeCancellable() =
        runTest(testDispatcher) {
            fakeRepo.transactions.value = listOf(sampleTransaction)
            fakeRepo.operationDelayMs = 10_000L

            val job: Job = launch { useCase() }
            job.cancel()
            advanceUntilIdle()

            assertTrue(job.isCancelled)
            assertEquals(1, fakeRepo.transactions.value.size)
            assertFalse(fakeRepo.transactions.value.isEmpty())
        }
}
