package com.antcashmanager.domain.usecase.transaction

// Test concreti per DeleteAllTransactionsUseCase con dispatcher injection e cancellazione
// si trovano in shared/src/test/ per JVM (JUnit).
// Questo file è riservato a futuri test KMP-only (es. iOS, Desktop).
// NOTA: la classe DeleteAllTransactionsUseCaseTest è definita in src/test/ (JVM).

/*

/**
 * Test concreti per [DeleteAllTransactionsUseCase].
 *
 * Verifica:
 * - Happy path: eliminazione di tutte le transazioni
 * - Idempotenza: cancelation di lista già vuota non causa errori
 * - Error path: propagazione eccezione dal repository
 * - Dispatcher injection: TestDispatcher per determinismo
 * - Cancellazione cooperativa: il job può essere annullato prima del completamento
 * - CancellationException: non viene inghiottita
 */
class DeleteAllTransactionsUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeDeleteAllRepository
    private lateinit var useCase: DeleteAllTransactionsUseCase

    @BeforeTest
    fun setup() {
        fakeRepo = FakeDeleteAllRepository()
        useCase = DeleteAllTransactionsUseCase(fakeRepo, testDispatcher)
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun `invoke should delete all transactions from repository`() = runTest(testDispatcher) {
        // Given
        fakeRepo.hasData = true

        // When
        useCase()

        // Then
        assertTrue(fakeRepo.deleteAllCalled)
        assertFalse(fakeRepo.hasData)
    }

    @Test
    fun `invoke should be idempotent on empty repository`() = runTest(testDispatcher) {
        // Given - repository già vuoto
        fakeRepo.hasData = false

        // When
        useCase()
        useCase() // seconda chiamata

        // Then - entrambe le chiamate completano senza errori
        assertEquals(2, fakeRepo.deleteAllCallCount)
    }

    // ── Error Path ───────────────────────────────────────────────────────────

    @Test
    fun `invoke should propagate exception from repository`() = runTest(testDispatcher) {
        // Given
        fakeRepo.shouldThrow = true

        // When / Then
        assertFailsWith<RuntimeException> {
            useCase()
        }
    }

    // ── Cancellazione ────────────────────────────────────────────────────────

    @Test
    fun `invoke should be cancellable before execution`() = runTest(testDispatcher) {
        // Given - repo lento
        val slowRepo = SlowDeleteAllRepository(delayMs = 10_000L)
        val cancellableUseCase = DeleteAllTransactionsUseCase(slowRepo, testDispatcher)

        // When - lancio e cancello immediatamente
        val job: Job = launch { cancellableUseCase() }
        job.cancel()
        advanceUntilIdle()

        // Then - il job è cancellato, deleteAll NON è stato chiamato
        assertTrue(job.isCancelled)
        assertFalse(slowRepo.deleteAllCalled)
    }

    @Test
    fun `cancellation should not leave repository in inconsistent state`() = runTest(testDispatcher) {
        // Given - due operazioni, la prima viene cancellata
        val slowRepo = SlowDeleteAllRepository(delayMs = 10_000L)
        val cancellableUseCase = DeleteAllTransactionsUseCase(slowRepo, testDispatcher)
        val fastUseCase = DeleteAllTransactionsUseCase(fakeRepo, testDispatcher)

        // Prima operazione (lenta) cancellata
        val slowJob: Job = launch { cancellableUseCase() }
        slowJob.cancel()
        advanceUntilIdle()

        // Seconda operazione (fast) completata
        val fastJob: Job = launch { fastUseCase() }
        advanceUntilIdle()

        // Then
        assertTrue(slowJob.isCancelled)
        assertTrue(fastJob.isCompleted)
        assertTrue(fakeRepo.deleteAllCalled)
        assertFalse(slowRepo.deleteAllCalled)
    }

    // ── CancellationException non viene inghiottita ──────────────────────────

    @Test
    fun `invoke should not swallow CancellationException`() = runTest(testDispatcher) {
        // Given - repository che tenta di non propagare CancellationException
        val badRepo = CancellationSwallowingRepository()
        val badUseCase = DeleteAllTransactionsUseCase(badRepo, testDispatcher)

        // When - cancello il job
        val job: Job = launch { badUseCase() }
        job.cancel()
        advanceUntilIdle()

        // Then - il job risulta cancellato (CancellationException propagata correttamente)
        assertTrue(job.isCancelled)
    }
}

// ── Fake Repositories ────────────────────────────────────────────────────────

private class FakeDeleteAllRepository : TransactionRepository {
    var hasData = false
    var shouldThrow = false
    var deleteAllCalled = false
    var deleteAllCallCount = 0

    override suspend fun deleteAllTransactions() {
        if (shouldThrow) throw RuntimeException("DB error simulato")
        deleteAllCalled = true
        deleteAllCallCount++
        hasData = false
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun insertTransaction(transaction: Transaction): Long = 0L
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> = flowOf(emptyList())
    override fun getRecurringTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) {}
    override fun getDistinctTitles(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctPayees(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctNotes(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctLocations(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctTags(): Flow<List<String>> = flowOf(emptyList())
}

/** Simula un repository lento per testare la cancellazione. */
private class SlowDeleteAllRepository(private val delayMs: Long) : TransactionRepository {
    var deleteAllCalled = false

    override suspend fun deleteAllTransactions() {
        delay(delayMs) // qui la coroutine deve essere cancellata
        deleteAllCalled = true
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun insertTransaction(transaction: Transaction): Long = 0L
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> = flowOf(emptyList())
    override fun getRecurringTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) {}
    override fun getDistinctTitles(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctPayees(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctNotes(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctLocations(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctTags(): Flow<List<String>> = flowOf(emptyList())
}

/**
 * Repository che tenta di inghiottire CancellationException (anti-pattern).
 * Usato per verificare che la base class propaghi correttamente la CancellationException.
 */
private class CancellationSwallowingRepository : TransactionRepository {
    override suspend fun deleteAllTransactions() {
        try {
            delay(Long.MAX_VALUE) // attende "per sempre"
        } catch (e: Exception) {
            // Anti-pattern: cattura tutto senza rethrow
            // withContext nel BaseUseCase gestisce comunque la cancellazione
        }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun getTransactionById(id: Long): Transaction? = null
    override suspend fun insertTransaction(transaction: Transaction): Long = 0L
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> = flowOf(emptyList())
    override fun getRecurringTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) {}
    override fun getDistinctTitles(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctPayees(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctNotes(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctLocations(): Flow<List<String>> = flowOf(emptyList())
    override fun getDistinctTags(): Flow<List<String>> = flowOf(emptyList())
}

*/
