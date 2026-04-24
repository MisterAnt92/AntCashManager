package com.antcashmanager.domain.usecase.transaction

// Test concreti per GetTransactionsUseCase con dispatcher injection e cancellazione
// si trovano in shared/src/test/ per JVM (JUnit).
// Questo file è riservato a futuri test KMP-only (es. iOS, Desktop).
// NOTA: la classe GetTransactionsUseCaseTest è definita in src/test/ (JVM).

/*
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test concreti per [GetTransactionsUseCase].
 *
 * Verifica:
 * - Happy path: emissione corretta dei dati dal repository
 * - Reattività: aggiornamenti del Flow riflettono modifiche al repository
 * - Dispatcher injection: flowOn con TestDispatcher
 * - Cancellazione cooperativa: la raccolta del Flow può essere annullata
 */
class GetTransactionsUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeGetTransactionsRepository
    private lateinit var useCase: GetTransactionsUseCase

    private val sampleTransactions = listOf(
        Transaction(
            id = 1L,
            title = "Stipendio Marzo",
            amount = 1800.0,
            category = "Income",
            type = TransactionType.INCOME,
            timestamp = 1_700_000_000_000L,
        ),
        Transaction(
            id = 2L,
            title = "Bolletta luce",
            amount = 78.50,
            category = "Utilities",
            type = TransactionType.EXPENSE,
            timestamp = 1_700_100_000_000L,
        ),
    )

    @BeforeTest
    fun setup() {
        fakeRepo = FakeGetTransactionsRepository()
        // UnconfinedTestDispatcher: il Flow emette immediatamente senza bisogno di advanceUntilIdle
        useCase = GetTransactionsUseCase(fakeRepo, UnconfinedTestDispatcher(testDispatcher.scheduler))
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun `invoke should emit empty list when repository has no transactions`() = runTest(testDispatcher) {
        // Given - repo vuoto (default)

        // When
        val result = useCase().first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should emit all transactions from repository`() = runTest(testDispatcher) {
        // Given
        fakeRepo.transactions.value = sampleTransactions

        // When
        val result = useCase().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Stipendio Marzo", result[0].title)
        assertEquals("Bolletta luce", result[1].title)
    }

    @Test
    fun `invoke should emit correct transaction amounts`() = runTest(testDispatcher) {
        // Given
        fakeRepo.transactions.value = sampleTransactions

        // When
        val result = useCase().first()

        // Then
        assertEquals(1800.0, result[0].amount)
        assertEquals(78.50, result[1].amount)
    }

    // ── Reattività (Flow Updates) ────────────────────────────────────────────

    @Test
    fun `invoke should reflect repository updates reactively`() = runTest(testDispatcher) {
        // Given - prima emissione vuota
        val collected = mutableListOf<List<Transaction>>()
        val job = launch { useCase().collect { collected.add(it) } }
        advanceUntilIdle()

        // When - aggiorna il repository
        fakeRepo.transactions.value = sampleTransactions
        advanceUntilIdle()

        // Then - il Flow ha emesso 2 valori: lista vuota + lista aggiornata
        assertEquals(2, collected.size)
        assertTrue(collected[0].isEmpty())
        assertEquals(2, collected[1].size)

        job.cancel()
    }

    @Test
    fun `invoke should emit only income transactions after filter`() = runTest(testDispatcher) {
        // Given
        fakeRepo.transactions.value = sampleTransactions

        // When
        val result = useCase().first().filter { it.type == TransactionType.INCOME }

        // Then
        assertEquals(1, result.size)
        assertEquals("Stipendio Marzo", result.first().title)
    }

    // ── Dispatcher ───────────────────────────────────────────────────────────

    @Test
    fun `invoke should apply flowOn with injected dispatcher`() = runTest(testDispatcher) {
        // Given - UseCase con StandardTestDispatcher: richiede advanceUntilIdle
        val standardUseCase = GetTransactionsUseCase(fakeRepo, testDispatcher)
        fakeRepo.transactions.value = sampleTransactions
        var result: List<Transaction> = emptyList()

        // When
        val job = launch { standardUseCase().collect { result = it } }
        advanceUntilIdle()

        // Then
        assertEquals(2, result.size)
        job.cancel()
    }

    // ── Cancellazione ────────────────────────────────────────────────────────

    @Test
    fun `flow collection should be cancellable`() = runTest(testDispatcher) {
        // Given
        fakeRepo.transactions.value = sampleTransactions
        val collected = mutableListOf<List<Transaction>>()

        // When - avvio raccolta e cancello immediatamente
        val job = launch { useCase().collect { collected.add(it) } }
        job.cancel()
        advanceUntilIdle()

        // Then - il job è cancellato
        assertTrue(job.isCancelled)
    }

    @Test
    fun `cancelling flow should stop receiving further emissions`() = runTest(testDispatcher) {
        // Given
        val collected = mutableListOf<List<Transaction>>()
        val job = launch { useCase().collect { collected.add(it) } }
        advanceUntilIdle()
        val sizeBeforeCancel = collected.size

        // When - cancello la raccolta
        job.cancel()
        advanceUntilIdle()

        // Tentativo di emissione dopo cancellazione
        fakeRepo.transactions.value = sampleTransactions
        advanceUntilIdle()

        // Then - nessuna nuova emissione dopo la cancellazione
        assertEquals(sizeBeforeCancel, collected.size)
    }
}

// ── Fake Repository ──────────────────────────────────────────────────────────

private class FakeGetTransactionsRepository : TransactionRepository {
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    override fun getAllTransactions(): Flow<List<Transaction>> = transactions

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactions.value.find { it.id == id }
    override suspend fun insertTransaction(transaction: Transaction): Long = transaction.id
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override suspend fun deleteAllTransactions() { transactions.value = emptyList() }
    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        flowOf(transactions.value.filter { it.timestamp in from..to })
    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        flowOf(transactions.value.filter { it.isRecurring })
    override suspend fun updateCategoryData(categoryName: String, icon: String, color: Long) {}
    override fun getDistinctTitles(): Flow<List<String>> = flowOf(transactions.value.map { it.title }.distinct())
    override fun getDistinctPayees(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctNotes(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctLocations(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctTags(): Flow<List<String>> = flowOf(emptyList())
}

*/
