package com.antcashmanager.android.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.home.event.HomeEvent
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HomeViewModel search functionality.
 *
 * Tests the search/filter logic:
 * - Search query updates and filtering
 * - Search expansion toggle
 * - Search suggestions generation
 * - Case-insensitive search
 * - Amount search with decimal formats
 * - Debounce behavior
 * - State persistence
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelSearchTest : BaseUnitTest() {
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = HomeViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            categoryRepository = fakeCategoryRepo,
            dispatcher = testDispatcher,
            searchDebounceMs = 0L, // Disable debounce for unit tests
            segmentationTracker = mockk(relaxed = true),
        )
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun updateSearchQuery_shouldFilterTransactions_whenQueryIsTyped() = runViewModelTest {
        // Setup: Set date range to include all transactions, then add test transactions
        fakeSettingsRepository.homeDateFilterState.value = SavedDateFilter(
            presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
            from = 0L,
            to = Long.MAX_VALUE,
        )

        val now = 1_700_000_000_000L
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Groceries",
                amount = 50.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
            Transaction(
                id = 3,
                title = "Grocery Store",
                amount = 75.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Initial state: all transactions visible (no filter)
        assertEquals(3, viewModel.state.value.filteredTransactions.size)

        // Apply search filter for "gro"
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("gro"))
        advanceUntilIdle()

        // Should filter to 2 transactions matching "gro" (case-insensitive)
        assertEquals(2, viewModel.state.value.filteredTransactions.size)
        assertTrue(
            viewModel.state.value.filteredTransactions.all {
                it.title.contains("gro", ignoreCase = true)
            }
        )
        collectJob.cancel()
    }

    @Test
    fun updateSearchQuery_shouldHandleEmptyQuery_whenCleared() = runViewModelTest {
        fakeSettingsRepository.homeDateFilterState.value = SavedDateFilter(
            presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
            from = 0L,
            to = Long.MAX_VALUE,
        )

        val now = 1_700_000_000_000L
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Set search query
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("Salary"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredTransactions.size)
        assertEquals("Salary", viewModel.state.value.searchQuery)

        // Clear search query
        viewModel.onEvent(HomeEvent.UpdateSearchQuery(""))
        advanceUntilIdle()

        // Should return to showing all transactions
        assertEquals(1, viewModel.state.value.filteredTransactions.size)
        assertEquals("", viewModel.state.value.searchQuery)
        collectJob.cancel()
    }

    @Test
    fun updateSearchQuery_shouldBeCaseInsensitive_whenSearching() = runViewModelTest {
        fakeSettingsRepository.homeDateFilterState.value = SavedDateFilter(
            presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
            from = 0L,
            to = Long.MAX_VALUE,
        )

        val now = 1_700_000_000_000L
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Test lowercase search
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("salary"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredTransactions.size)

        // Test uppercase search
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("SALARY"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredTransactions.size)

        // Test mixed case search
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("SaLaRy"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredTransactions.size)

        collectJob.cancel()
    }

    @Test
    fun updateSearchQuery_shouldMatchAmount_withDecimalSeparator() = runViewModelTest {
        fakeSettingsRepository.homeDateFilterState.value = SavedDateFilter(
            presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
            from = 0L,
            to = Long.MAX_VALUE,
        )

        val now = 1_700_000_000_000L
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Groceries",
                amount = 85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Restaurant",
                amount = 45.75,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Test search with decimal point
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("85.50"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredTransactions.size)
        assertEquals("Groceries", viewModel.state.value.filteredTransactions.first().title)

        // Test search with comma separator
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("45,75"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredTransactions.size)
        assertEquals("Restaurant", viewModel.state.value.filteredTransactions.first().title)

        collectJob.cancel()
    }

    @Test
    fun toggleSearchExpanded_shouldToggleState_whenEventIsReceived() = runViewModelTest {
        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Initial state: search is collapsed
        assertFalse(viewModel.state.value.isSearchExpanded)

        // Toggle to expand
        viewModel.onEvent(HomeEvent.ToggleSearchExpanded)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isSearchExpanded)

        // Toggle to collapse
        viewModel.onEvent(HomeEvent.ToggleSearchExpanded)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isSearchExpanded)

        collectJob.cancel()
    }

    @Test
    fun toggleSearchExpanded_shouldNotClearQuery_whenTogglingState() = runViewModelTest {
        val now = 1_700_000_000_000L
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Set search query
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("Salary"))
        advanceUntilIdle()
        assertEquals("Salary", viewModel.state.value.searchQuery)

        // Toggle search expansion
        viewModel.onEvent(HomeEvent.ToggleSearchExpanded)
        advanceUntilIdle()

        // Query should persist
        assertEquals("Salary", viewModel.state.value.searchQuery)
        assertTrue(viewModel.state.value.isSearchExpanded)

        collectJob.cancel()
    }

    @Test
    fun searchSuggestions_shouldAppear_whenQueryIsNotEmpty() = runViewModelTest {
        val now = 1_700_000_000_000L
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Salary Bonus",
                amount = 500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Initial state: no query, no suggestions
        assertEquals("", viewModel.state.value.searchQuery)
        assertTrue(viewModel.state.value.searchSuggestions.isEmpty())

        // Set search query
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("Sal"))
        advanceUntilIdle()

        // Suggestions should appear matching the query
        assertTrue(viewModel.state.value.searchSuggestions.isNotEmpty())
        assertTrue(
            viewModel.state.value.searchSuggestions.any { it.contains("Sal", ignoreCase = true) }
        )

        collectJob.cancel()
    }

    @Test
    fun searchSuggestions_shouldBeEmpty_whenQueryIsEmpty() = runViewModelTest {
        val now = 1_700_000_000_000L
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Set query and verify suggestions appear
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("Sal"))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.searchSuggestions.isNotEmpty())

        // Clear query
        viewModel.onEvent(HomeEvent.UpdateSearchQuery(""))
        advanceUntilIdle()

        // Suggestions should disappear
        assertTrue(viewModel.state.value.searchSuggestions.isEmpty())

        collectJob.cancel()
    }

    @Test
    fun searchQuery_shouldNotMatchPartially_whenQueryDoesNotExist() = runViewModelTest {
        val now = 1_700_000_000_000L
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Groceries",
                amount = 50.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Search for something that doesn't exist
        viewModel.onEvent(HomeEvent.UpdateSearchQuery("xyz"))
        advanceUntilIdle()

        // Should return empty results
        assertTrue(viewModel.state.value.filteredTransactions.isEmpty())

        collectJob.cancel()
    }
}
