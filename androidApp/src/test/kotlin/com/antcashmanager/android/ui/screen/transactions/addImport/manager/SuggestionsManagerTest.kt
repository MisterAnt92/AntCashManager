package com.antcashmanager.android.ui.screen.transactions.addImport.manager

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test unitari per [SuggestionsManager].
 *
 * Copre:
 * - Caricamento suggerimenti
 * - Filtraggio suggerimenti per query
 * - Limitazione risultati ai top N
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SuggestionsManagerTest : BaseUnitTest() {

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var getSuggestionsUseCase: GetTransactionSuggestionsUseCase
    private lateinit var manager: SuggestionsManager

    private val mockTransactions = listOf(
        Transaction(
            id = 1,
            title = "Lunch at McDonald's",
            amount = -15.00,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = System.currentTimeMillis(),
            notes = "Quick lunch",
            payee = "McDonald's",
            location = "Downtown",
            tags = "food,fast-food",
        ),
        Transaction(
            id = 2,
            title = "Lunch at Subway",
            amount = -12.00,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = System.currentTimeMillis(),
            notes = "Healthy lunch",
            payee = "Subway",
            location = "Mall",
            tags = "food,healthy",
        ),
        Transaction(
            id = 3,
            title = "Dinner at Italian Restaurant",
            amount = -45.00,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = System.currentTimeMillis(),
            notes = "Pasta night",
            payee = "Italian Restaurant",
            location = "Downtown",
            tags = "food,restaurant",
        ),
        Transaction(
            id = 4,
            title = "Coffee",
            amount = -4.50,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = System.currentTimeMillis(),
            payee = "Starbucks",
            location = "Office",
            tags = "food,coffee",
        ),
    )

    @Before
    fun setup() {
        transactionRepository = FakeTransactionRepository(mockTransactions)
        settingsRepository = FakeSettingsRepository()
        getSuggestionsUseCase = GetTransactionSuggestionsUseCase(transactionRepository, settingsRepository)
        manager = SuggestionsManager(
            getTransactionSuggestionsUseCase = getSuggestionsUseCase,
        )
    }

    // ── Get Suggestions Tests ──

    @Test
    fun `getSuggestions returns flow of suggestions`() = runUnitTest {
        val suggestionsFlow = manager.getSuggestions()
        val result = suggestionsFlow.first()

        assertTrue("Should return success", result.isSuccess)
        val suggestions = result.getOrNull()
        assertNotNull("Suggestions should not be null", suggestions)
        assertTrue("Should have title suggestions", suggestions!!.titles.isNotEmpty())
        assertTrue("Should have payee suggestions", suggestions.payees.isNotEmpty())
    }

    @Test
    fun `getSuggestions includes all unique titles`() = runUnitTest {
        val suggestionsFlow = manager.getSuggestions()
        val result = suggestionsFlow.first()
        val suggestions = result.getOrThrow()

        assertTrue("Should contain 'Lunch at McDonald\\'s'",
            suggestions.titles.contains("Lunch at McDonald's"))
        assertTrue("Should contain 'Lunch at Subway'",
            suggestions.titles.contains("Lunch at Subway"))
        assertTrue("Should contain 'Dinner at Italian Restaurant'",
            suggestions.titles.contains("Dinner at Italian Restaurant"))
        assertTrue("Should contain 'Coffee'",
            suggestions.titles.contains("Coffee"))
    }

    @Test
    fun `getSuggestions includes all unique payees`() = runUnitTest {
        val suggestionsFlow = manager.getSuggestions()
        val result = suggestionsFlow.first()
        val suggestions = result.getOrThrow()

        assertTrue("Should contain 'McDonald\\'s'",
            suggestions.payees.contains("McDonald's"))
        assertTrue("Should contain 'Subway'",
            suggestions.payees.contains("Subway"))
        assertTrue("Should contain 'Italian Restaurant'",
            suggestions.payees.contains("Italian Restaurant"))
        assertTrue("Should contain 'Starbucks'",
            suggestions.payees.contains("Starbucks"))
    }

    @Test
    fun `getSuggestions includes all unique notes`() = runUnitTest {
        val suggestionsFlow = manager.getSuggestions()
        val result = suggestionsFlow.first()
        val suggestions = result.getOrThrow()

        assertTrue("Should contain 'Quick lunch'",
            suggestions.notes.contains("Quick lunch"))
        assertTrue("Should contain 'Healthy lunch'",
            suggestions.notes.contains("Healthy lunch"))
        assertTrue("Should contain 'Pasta night'",
            suggestions.notes.contains("Pasta night"))
    }

    @Test
    fun `getSuggestions includes all unique tags`() = runUnitTest {
        val suggestionsFlow = manager.getSuggestions()
        val result = suggestionsFlow.first()
        val suggestions = result.getOrThrow()

        // Tags are returned as comma-separated strings, not split
        assertTrue("Should contain comma-separated tags from first transaction",
            suggestions.tags.contains("food,fast-food"))
        assertTrue("Should contain tags from other transactions",
            suggestions.tags.any { it.contains("food") })
        // Verify we have multiple distinct tag combinations
        assertTrue("Should have multiple distinct tag strings", suggestions.tags.size >= 2)
    }

    // ── Filter Suggestions Tests ──

    @Test
    fun `filterSuggestions returns empty list for blank query`() {
        val suggestions = listOf("Apple", "Banana", "Cherry")

        val result = manager.filterSuggestions(suggestions, "")

        assertTrue("Should return empty list", result.isEmpty())
    }

    @Test
    fun `filterSuggestions returns empty list for query with only spaces`() {
        val suggestions = listOf("Apple", "Banana", "Cherry")

        val result = manager.filterSuggestions(suggestions, "   ")

        assertTrue("Should return empty list", result.isEmpty())
    }

    @Test
    fun `filterSuggestions filters case-insensitive`() {
        val suggestions = listOf("Apple", "BANANA", "cherry")

        val result = manager.filterSuggestions(suggestions, "app")

        assertEquals(1, result.size)
        assertTrue("Should contain 'Apple'", result.contains("Apple"))
    }

    @Test
    fun `filterSuggestions filters partial matches`() {
        val suggestions = listOf("Lunch at McDonald's", "Lunch at Subway", "Dinner at Italian")

        val result = manager.filterSuggestions(suggestions, "Lunch")

        assertEquals(2, result.size)
        assertTrue("Should contain Lunch suggestions",
            result.contains("Lunch at McDonald's") && result.contains("Lunch at Subway"))
    }

    @Test
    fun `filterSuggestions returns multiple matches`() {
        val suggestions = listOf("Food", "Fast Food", "Food Court", "Coffee")

        val result = manager.filterSuggestions(suggestions, "Food")

        assertEquals(3, result.size)
    }

    @Test
    fun `filterSuggestions returns all suggestions when query matches all`() {
        val suggestions = listOf("A", "AB", "ABC")

        val result = manager.filterSuggestions(suggestions, "A")

        assertEquals(3, result.size)
    }

    // ── Get Top Filtered Suggestions Tests ──

    @Test
    fun `getTopFilteredSuggestions returns limited results`() {
        val suggestions = (1..10).map { "Suggestion $it" }

        val result = manager.getTopFilteredSuggestions(suggestions, "Suggestion", limit = 5)

        assertEquals(5, result.size)
    }

    @Test
    fun `getTopFilteredSuggestions returns fewer results when available fewer than limit`() {
        val suggestions = listOf("Apple", "Apricot", "Avocado")

        val result = manager.getTopFilteredSuggestions(suggestions, "A", limit = 10)

        assertEquals(3, result.size)
    }

    @Test
    fun `getTopFilteredSuggestions default limit is 5`() {
        val suggestions = (1..10).map { "Item $it" }

        val result = manager.getTopFilteredSuggestions(suggestions, "Item")

        assertEquals(5, result.size)
    }

    @Test
    fun `getTopFilteredSuggestions returns empty for no matches`() {
        val suggestions = listOf("Apple", "Banana", "Cherry")

        val result = manager.getTopFilteredSuggestions(suggestions, "XYZ")

        assertTrue("Should return empty list", result.isEmpty())
    }

    @Test
    fun `getTopFilteredSuggestions respects custom limit`() {
        val suggestions = listOf("A1", "A2", "A3", "A4", "A5")

        val resultLimit2 = manager.getTopFilteredSuggestions(suggestions, "A", limit = 2)
        val resultLimit3 = manager.getTopFilteredSuggestions(suggestions, "A", limit = 3)

        assertEquals(2, resultLimit2.size)
        assertEquals(3, resultLimit3.size)
    }

    @Test
    fun `getTopFilteredSuggestions filters and limits correctly`() {
        val suggestions = listOf(
            "Lunch",
            "Lunch at McDonald's",
            "Lunch at Subway",
            "Lunch at Restaurant",
            "Lunch at Home",
            "Dinner",
            "Breakfast"
        )

        val result = manager.getTopFilteredSuggestions(suggestions, "Lunch", limit = 3)

        assertEquals(3, result.size)
        assertTrue("All results should contain 'Lunch'", result.all { it.contains("Lunch") })
    }

    @Test
    fun `getTopFilteredSuggestions preserves order`() {
        val suggestions = listOf("B1", "A2", "A1", "A3")

        val result = manager.getTopFilteredSuggestions(suggestions, "A", limit = 5)

        // Should preserve original order from the input list
        assertEquals(listOf("A2", "A1", "A3"), result)
    }
}
