package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

@OptIn(ExperimentalCoroutinesApi::class)

class GetTransactionSuggestionsUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSuggestionsTransactionRepository
    private lateinit var useCase: GetTransactionSuggestionsUseCase

    @BeforeTest
    fun setup() {
        repository = FakeSuggestionsTransactionRepository()
        // UnconfinedTestDispatcher: il Flow emette immediatamente nei test
        useCase = GetTransactionSuggestionsUseCase(repository, UnconfinedTestDispatcher(testDispatcher.scheduler))
    }

    @Test
    fun `flow collection should be cancellable`() = runTest(testDispatcher) {
        // Given
        repository.titlesToReturn = listOf("Test")
        val cancellableUseCase = GetTransactionSuggestionsUseCase(
            repository,
            testDispatcher,
        )
        val collected = mutableListOf<TransactionSuggestions>()

        // When - avvio collector e cancello subito
        val job = launch { cancellableUseCase().collect { collected.add(it) } }
        job.cancel()
        advanceUntilIdle()

        // Then
        assertTrue(job.isCancelled)
    }

    @Test
    fun `invoke should handle partial data correctly`() = runTest(testDispatcher) {
        // Given - Solo alcuni campi hanno dati
        repository.titlesToReturn = listOf("Spesa")
        repository.payeesToReturn = emptyList()
        repository.notesToReturn = listOf("Note")
        repository.locationsToReturn = emptyList()
        repository.tagsToReturn = listOf("Tag1", "Tag2")

        // When
        val result = useCase().first()

        // Then
        assertEquals(listOf("Spesa"), result.titles)
        assertEquals(emptyList(), result.payees)
        assertEquals(listOf("Note"), result.notes)
        assertEquals(emptyList(), result.locations)
        assertEquals(listOf("Tag1", "Tag2"), result.tags)
    }

    @Test
    fun `invoke should return all suggestions from repository`() = runTest(testDispatcher) {
        // Given
        val expectedTitles = listOf("Spesa", "Carburante", "Ristorante")
        val expectedPayees = listOf("Supermercato", "Stazione servizio")
        val expectedNotes = listOf("Cena con amici", "Spesa mensile")
        val expectedLocations = listOf("Milano", "Roma")
        val expectedTags = listOf("Food", "Transport")

        repository.titlesToReturn = expectedTitles
        repository.payeesToReturn = expectedPayees
        repository.notesToReturn = expectedNotes
        repository.locationsToReturn = expectedLocations
        repository.tagsToReturn = expectedTags

        // When
        val result = useCase().first()

        // Then
        assertEquals(expectedTitles, result.titles)
        assertEquals(expectedPayees, result.payees)
        assertEquals(expectedNotes, result.notes)
        assertEquals(expectedLocations, result.locations)
        assertEquals(expectedTags, result.tags)
    }

    @Test
    fun `invoke should return empty suggestions when no data available`() = runTest(testDispatcher) {
        // Given
        repository.titlesToReturn = emptyList()
        repository.payeesToReturn = emptyList()
        repository.notesToReturn = emptyList()
        repository.locationsToReturn = emptyList()
        repository.tagsToReturn = emptyList()

        // When
        val result = useCase().first()

        // Then
        assertEquals(TransactionSuggestions(), result)
    }
}

/**
 * Fake implementation di TransactionRepository per i test.
 */
private class FakeSuggestionsTransactionRepository : TransactionRepository {
    var titlesToReturn: List<String> = emptyList()
    var payeesToReturn: List<String> = emptyList()
    var notesToReturn: List<String> = emptyList()
    var locationsToReturn: List<String> = emptyList()
    var tagsToReturn: List<String> = emptyList()

    override fun getDistinctTitles() = flowOf(titlesToReturn)
    override fun getDistinctPayees() = flowOf(payeesToReturn)
    override fun getDistinctNotes() = flowOf(notesToReturn)
    override fun getDistinctLocations() = flowOf(locationsToReturn)
    override fun getDistinctTags() = flowOf(tagsToReturn)

    // Altri metodi non implementati per i test
    override fun getAllTransactions() = flowOf(emptyList<Transaction>())
    override suspend fun getTransactionById(id: Long) = null
    override suspend fun insertTransaction(transaction: Transaction) = 0L
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override suspend fun deleteAllTransactions() {}
    override fun getTransactionsByDateRange(from: Long, to: Long) = flowOf(emptyList<Transaction>())
    override fun getRecurringTransactions() = flowOf(emptyList<Transaction>())
    override suspend fun updateCategoryData(categoryName: String, icon: String, color: Long) {}
}
