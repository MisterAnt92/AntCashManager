package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InsertTransactionUseCaseMockkTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var useCase: InsertTransactionUseCase

    private val sampleTransaction =
        Transaction(
            id = 0L,
            title = "Groceries",
            amount = 42.25,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 1_712_200_000_000L,
        )

    @Before
    fun setup() {
        transactionRepository = mockk()
        useCase =
            InsertTransactionUseCase(
                transactionRepository = transactionRepository,
                dispatcher = testDispatcher,
            )
    }

    @Test
    fun invoke_shouldReturnSuccessId_whenRepositoryInsertSucceeds() =
        runTest(testDispatcher) {
            coEvery { transactionRepository.insertTransaction(sampleTransaction) } returns 77L

            val result = useCase(sampleTransaction)

            assertTrue(result.isSuccess)
            assertEquals(77L, result.getOrThrow())
            coVerify(exactly = 1) { transactionRepository.insertTransaction(sampleTransaction) }
        }

    @Test
    fun invoke_shouldReturnFailure_whenRepositoryInsertThrows() =
        runTest(testDispatcher) {
            val expectedError = IllegalStateException("db-error")
            coEvery { transactionRepository.insertTransaction(sampleTransaction) } throws expectedError

            val result = useCase(sampleTransaction)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalStateException)
            assertEquals("db-error", result.exceptionOrNull()?.message)
            coVerify(exactly = 1) { transactionRepository.insertTransaction(sampleTransaction) }
        }

    @Test
    fun invoke_shouldForwardTransactionToRepository_whenCalled() =
        runTest(testDispatcher) {
            coEvery { transactionRepository.insertTransaction(any()) } returns 10L

            useCase(sampleTransaction)

            coVerify(exactly = 1) {
                transactionRepository.insertTransaction(
                    match {
                        it.title == sampleTransaction.title &&
                            it.amount == sampleTransaction.amount &&
                            it.category == sampleTransaction.category &&
                            it.type == sampleTransaction.type
                    },
                )
            }
        }

    @Test
    fun invokeWithStandardTestDispatcherNeedsAdvanceUntilIdle() =
        runTest(testDispatcher) {
            coEvery { transactionRepository.insertTransaction(any()) } returns 1L
            var resultId = 0L

            val job = launch { resultId = useCase(sampleTransaction).getOrThrow() }
            advanceUntilIdle()

            assertEquals(1L, resultId)
            job.join()
        }
}
