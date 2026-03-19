package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTransactionsUseCaseTest {

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
        useCase = GetTransactionsUseCase(repository)
    }

    @Test
    fun `invoke returns all transactions from repository`() = runTest {
        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals("Salary", result[0].title)
        assertEquals("Groceries", result[1].title)
    }

    @Test
    fun `invoke returns empty list when repository has no transactions`() = runTest {
        val emptyRepo = FakeTransactionRepository(emptyList())
        val emptyUseCase = GetTransactionsUseCase(emptyRepo)

        val result = emptyUseCase().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returned transactions contain correct types`() = runTest {
        val result = useCase().first()

        assertEquals(TransactionType.INCOME, result[0].type)
        assertEquals(TransactionType.EXPENSE, result[1].type)
    }
}

/**
 * Fake repository per i test, evita dipendenze da Room.
 */
private class FakeTransactionRepository(
    private val transactions: List<Transaction>,
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        flowOf(transactions)

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactions.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transaction.id

    override suspend fun updateTransaction(transaction: Transaction) = Unit

    override suspend fun deleteTransaction(transaction: Transaction) = Unit

    override suspend fun deleteAllTransactions() = Unit

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        flowOf(transactions.filter { it.timestamp in from..to })
}

