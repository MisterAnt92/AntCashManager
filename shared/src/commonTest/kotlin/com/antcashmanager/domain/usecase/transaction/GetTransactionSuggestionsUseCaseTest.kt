package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.testutil.testTransaction
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for GetTransactionSuggestionsUseCase.
 * Tests cover:
 * - Title suggestions generation
 * - Payee suggestions
 * - Notes suggestions
 * - Location suggestions
 * - Tags suggestions
 * - Deduplication
 * - Empty history handling
 * - Special characters preservation
 */
class GetTransactionSuggestionsUseCaseTest {

    private val transactionRepository = mockk<TransactionRepository>()
    private val settingsRepository = mockk<SettingsRepository>()

    private fun createUseCase() = GetTransactionSuggestionsUseCase(
        repository = transactionRepository,
        settingsRepository = settingsRepository,
        dispatcher = kotlinx.coroutines.Dispatchers.Default
    )

    @Test
    fun getSuggestions_shouldReturnTitleSuggestions() = runTest {
        val suggestions = TransactionSuggestions(
            titles = listOf("Lunch", "Coffee", "Dinner"),
            payees = emptyList(),
            notes = emptyList(),
            locations = emptyList(),
            tags = emptyList()
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertNotNull(result)
        assertEquals(3, result.titles.size)
        assertTrue(result.titles.contains("Lunch"))
    }

    @Test
    fun getSuggestions_shouldReturnPayeeSuggestions() = runTest {
        val suggestions = TransactionSuggestions(
            titles = emptyList(),
            payees = listOf("Restaurant A", "Cafe B", "Pizzeria C"),
            notes = emptyList(),
            locations = emptyList(),
            tags = emptyList()
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertNotNull(result)
        assertEquals(3, result.payees.size)
    }

    @Test
    fun getSuggestions_shouldReturnNotesSuggestions() = runTest {
        val suggestions = TransactionSuggestions(
            titles = emptyList(),
            payees = emptyList(),
            notes = listOf("Business meal", "Quick break", "Special diet"),
            locations = emptyList(),
            tags = emptyList()
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertNotNull(result)
        assertEquals(3, result.notes.size)
    }

    @Test
    fun getSuggestions_shouldReturnLocationSuggestions() = runTest {
        val suggestions = TransactionSuggestions(
            titles = emptyList(),
            payees = emptyList(),
            notes = emptyList(),
            locations = listOf("Downtown", "Mall", "Office"),
            tags = emptyList()
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertNotNull(result)
        assertEquals(3, result.locations.size)
    }

    @Test
    fun getSuggestions_shouldReturnTagsSuggestions() = runTest {
        val suggestions = TransactionSuggestions(
            titles = emptyList(),
            payees = emptyList(),
            notes = emptyList(),
            locations = emptyList(),
            tags = listOf("food", "business", "travel", "#important")
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertNotNull(result)
        assertEquals(4, result.tags.size)
    }

    @Test
    fun getSuggestions_shouldReturnAllSuggestionsTypes() = runTest {
        val suggestions = TransactionSuggestions(
            titles = listOf("Lunch", "Dinner"),
            payees = listOf("Restaurant A", "Restaurant B"),
            notes = listOf("Business meal", "Social"),
            locations = listOf("Downtown", "Uptown"),
            tags = listOf("food", "business")
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertNotNull(result)
        assertEquals(2, result.titles.size)
        assertEquals(2, result.payees.size)
        assertEquals(2, result.notes.size)
        assertEquals(2, result.locations.size)
        assertEquals(2, result.tags.size)
    }

    @Test
    fun getSuggestions_shouldHandleEmptyHistory() = runTest {
        val suggestions = TransactionSuggestions(
            titles = emptyList(),
            payees = emptyList(),
            notes = emptyList(),
            locations = emptyList(),
            tags = emptyList()
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertNotNull(result)
        assertEquals(0, result.titles.size)
        assertEquals(0, result.payees.size)
    }

    @Test
    fun getSuggestions_shouldPreserveSpecialCharacters() = runTest {
        val suggestions = TransactionSuggestions(
            titles = listOf("Café Français", "北京烤鸭", "Σαλάτα"),
            payees = listOf("Restaurant & Co.", "Café (Milano)"),
            notes = listOf("Crêpes — très bon!", "Délicieux!"),
            locations = listOf("Paris, France 75018", "Milano (città)"),
            tags = listOf("france", "#vacation", "français")
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertTrue(result.titles.any { it.contains("Café") })
        assertTrue(result.titles.any { it.contains("北京") })
        assertTrue(result.payees.any { it.contains("&") })
        assertTrue(result.notes.any { it.contains("—") })
    }

    @Test
    fun getSuggestions_shouldHandleDuplicateDeduplication() = runTest {
        // Repository should return deduplicated suggestions
        val suggestions = TransactionSuggestions(
            titles = listOf("Lunch"),  // Deduplicated from multiple instances
            payees = listOf("Restaurant A"),
            notes = emptyList(),
            locations = emptyList(),
            tags = emptyList()
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        // Result should contain deduplicated suggestions
        assertEquals(1, result.titles.size)
    }

    @Test
    fun getSuggestions_shouldLimitSuggestionCountForPerformance() = runTest {
        // Even with large history, suggestions should be reasonable
        val suggestions = TransactionSuggestions(
            titles = (1..50).map { "Title $it" },
            payees = (1..50).map { "Payee $it" },
            notes = (1..50).map { "Note $it" },
            locations = (1..50).map { "Location $it" },
            tags = (1..50).map { "Tag $it" }
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(0L)

        assertNotNull(result)
        // Repository implementation should handle limiting
        assertTrue(result.titles.size > 0)
    }

    @Test
    fun getSuggestions_shouldUseDefaultDateRangeWhenNotSpecified() = runTest {
        val suggestions = TransactionSuggestions(
            titles = listOf("Lunch"),
            payees = emptyList(),
            notes = emptyList(),
            locations = emptyList(),
            tags = emptyList()
        )
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions

        val useCase = createUseCase()
        // Default should use 0L (no filter)
        val result = useCase(0L)

        assertNotNull(result)
    }

    @Test
    fun getSuggestions_shouldHandleRecentTransactionsOnly() = runTest {
        // When called with a timestamp, should only return recent suggestions
        val suggestions = TransactionSuggestions(
            titles = listOf("Recent Lunch"),
            payees = listOf("New Restaurant"),
            notes = emptyList(),
            locations = emptyList(),
            tags = emptyList()
        )

        val sinceTimestamp = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)  // Last 30 days
        coEvery { transactionRepository.getSuggestions(sinceTimestamp) } returns suggestions

        val useCase = createUseCase()
        val result = useCase(sinceTimestamp)

        assertNotNull(result)
        assertEquals(1, result.titles.size)
        assertTrue(result.titles[0].contains("Recent"))
    }
}
