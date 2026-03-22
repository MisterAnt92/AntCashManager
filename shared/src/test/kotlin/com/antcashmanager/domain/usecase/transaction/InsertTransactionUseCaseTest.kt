package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsertTransactionUseCaseTest {

    @Test
    fun `invoke inserts transaction and returns id`() = runTest {
        val fakeRepo = FakeInsertTransactionRepository()
        val useCase = InsertTransactionUseCase(fakeRepo)

        val transaction = Transaction(
            id = 0L,
            title = "New Income",
            amount = 1500.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = 1000L,
        )

        val resultId = useCase(transaction)

        assertEquals(1L, resultId)
        assertTrue(fakeRepo.insertedTransactions.contains(transaction))
    }

    @Test
    fun `invoke inserts multiple transactions with incremental ids`() = runTest {
        val fakeRepo = FakeInsertTransactionRepository()
        val useCase = InsertTransactionUseCase(fakeRepo)

        val t1 = Transaction(
            title = "T1",
            amount = 100.0,
            category = "A",
            type = TransactionType.INCOME,
            timestamp = 1000L,
        )
        val t2 = Transaction(
            title = "T2",
            amount = 200.0,
            category = "B",
            type = TransactionType.EXPENSE,
            timestamp = 2000L,
        )

        val id1 = useCase(t1)
        val id2 = useCase(t2)

        assertEquals(1L, id1)
        assertEquals(2L, id2)
        assertEquals(2, fakeRepo.insertedTransactions.size)
    }
}

private class FakeInsertTransactionRepository : TransactionRepository {
    val insertedTransactions = mutableListOf<Transaction>()
    private var nextId = 1L

    override fun getAllTransactions(): Flow<List<Transaction>> =
        flowOf(insertedTransactions.toList())

    override suspend fun getTransactionById(id: Long): Transaction? =
        insertedTransactions.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        insertedTransactions.add(transaction)
        return nextId++
    }

    override suspend fun updateTransaction(transaction: Transaction) = Unit

    override suspend fun deleteTransaction(transaction: Transaction) = Unit

    override suspend fun deleteAllTransactions() = Unit

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        flowOf(insertedTransactions.filter { it.timestamp in from..to })

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        flowOf(insertedTransactions.filter { it.isRecurring })
}

