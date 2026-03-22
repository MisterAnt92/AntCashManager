package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    fun `invoke deletes transaction from repository`() = runTest {
        val fakeRepo = FakeDeleteTransactionRepository(
            mutableListOf(sampleTransaction),
        )
        val useCase = DeleteTransactionUseCase(fakeRepo)

        useCase(sampleTransaction)

        assertTrue(fakeRepo.transactions.isEmpty())
    }

    @Test
    fun `invoke deletes only the specified transaction`() = runTest {
        val otherTransaction = Transaction(
            id = 2L,
            title = "Salary",
            amount = 2500.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = 2000L,
        )
        val fakeRepo = FakeDeleteTransactionRepository(
            mutableListOf(sampleTransaction, otherTransaction),
        )
        val useCase = DeleteTransactionUseCase(fakeRepo)

        useCase(sampleTransaction)

        assertEquals(1, fakeRepo.transactions.size)
        assertFalse(fakeRepo.transactions.contains(sampleTransaction))
        assertTrue(fakeRepo.transactions.contains(otherTransaction))
    }
}

private class FakeDeleteTransactionRepository(
    val transactions: MutableList<Transaction>,
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        flowOf(transactions.toList())

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactions.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        transactions.add(transaction)
        return transaction.id
    }

    override suspend fun updateTransaction(transaction: Transaction) = Unit

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactions.remove(transaction)
    }

    override suspend fun deleteAllTransactions() {
        transactions.clear()
    }

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        flowOf(transactions.filter { it.timestamp in from..to })

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        flowOf(transactions.filter { it.isRecurring })
}

