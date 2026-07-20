package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.testutil.FakeTransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteTransactionUseCaseTest {

    private val sampleTransaction = Transaction(
        id = 1L,
        title = "Groceries",
        amount = 85.50,
        category = "Food",
        type = TransactionType.EXPENSE,
        timestamp = 1000L,
    )

    @Test
    fun invokeDeletesTransactionFromRepository() = runTest {
        val fakeRepo = FakeTransactionRepository(listOf(sampleTransaction))
        val useCase = DeleteTransactionUseCase(fakeRepo)

        useCase(sampleTransaction)

        assertTrue(fakeRepo.transactions.value.isEmpty())
    }

    @Test
    fun invokeDeletesOnlyTheSpecifiedTransaction() = runTest {
        val otherTransaction = Transaction(
            id = 2L,
            title = "Salary",
            amount = 2500.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = 2000L,
        )
        val fakeRepo = FakeTransactionRepository(listOf(sampleTransaction, otherTransaction))
        val useCase = DeleteTransactionUseCase(fakeRepo)

        useCase(sampleTransaction)

        assertEquals(1, fakeRepo.transactions.value.size)
        assertFalse(fakeRepo.transactions.value.contains(sampleTransaction))
        assertTrue(fakeRepo.transactions.value.contains(otherTransaction))
    }
}
