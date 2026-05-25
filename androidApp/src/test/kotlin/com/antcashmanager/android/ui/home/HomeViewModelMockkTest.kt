package com.antcashmanager.android.ui.home

import com.antcashmanager.android.ui.screen.home.HomeEvent
import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetHomeDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetHomeDateFilterStateUseCase
import com.antcashmanager.domain.usecase.transaction.FilterTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelMockkTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    private lateinit var filterTransactionsUseCase: FilterTransactionsUseCase
    private lateinit var getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase
    private lateinit var getHomeDateFilterStateUseCase: GetHomeDateFilterStateUseCase
    private lateinit var setHomeDateFilterStateUseCase: SetHomeDateFilterStateUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getTransactionsUseCase = mockk()
        filterTransactionsUseCase = mockk()
        getTransactionSuggestionsUseCase = mockk()
        getHomeDateFilterStateUseCase = mockk()
        setHomeDateFilterStateUseCase = mockk()
        getCategoriesUseCase = mockk()

        every { getTransactionsUseCase() } returns flowOf(Result.success(emptyList()))
        coEvery { filterTransactionsUseCase(any()) } answers {
            val params = firstArg<FilterTransactionsUseCase.Params>()
            Result.success(params.transactions)
        }
        every { getTransactionSuggestionsUseCase() } returns flowOf(TransactionSuggestions())
        every { getHomeDateFilterStateUseCase() } returns flowOf(
            Result.success(
                SavedDateFilter(
                    presetIndex = 1,
                    from = 1_700_000_000_000L,
                    to = 1_700_100_000_000L,
                ),
            ),
        )
        every { getCategoriesUseCase() } returns flowOf(Result.success(emptyList()))
        coEvery { setHomeDateFilterStateUseCase(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onEvent_shouldPersistCustomFilter_whenSetDateRangeEventIsReceived() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val collectJob = launch { viewModel.state.collect {} }

        val from = 1_710_000_000_000L
        val to = 1_710_100_000_000L

        viewModel.onEvent(HomeEvent.SetDateRange(from = from, to = to))
        advanceUntilIdle()

        coVerify(atLeast = 1) {
            setHomeDateFilterStateUseCase(
                match {
                    it.presetIndex == SavedDateFilter.CUSTOM_PRESET_INDEX &&
                            it.from == from &&
                            it.to == to
                },
            )
        }
        collectJob.cancel()
    }

    @Test
    fun onEvent_shouldNormalizeRange_whenFromIsGreaterThanTo() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val collectJob = launch { viewModel.state.collect {} }

        val larger = 1_710_100_000_000L
        val smaller = 1_710_000_000_000L

        viewModel.onEvent(HomeEvent.SetDateRange(from = larger, to = smaller))
        advanceUntilIdle()

        coVerify(atLeast = 1) {
            setHomeDateFilterStateUseCase(
                match {
                    it.presetIndex == SavedDateFilter.CUSTOM_PRESET_INDEX &&
                            it.from == smaller &&
                            it.to == larger
                },
            )
        }
        collectJob.cancel()
    }

    @Test
    fun onEvent_shouldPersistPresetFilter_whenSelectPresetEventIsReceived() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val collectJob = launch { viewModel.state.collect {} }

        val presetIndex = 2
        viewModel.onEvent(HomeEvent.SelectPreset(presetIndex))
        advanceUntilIdle()

        coVerify(atLeast = 1) {
            setHomeDateFilterStateUseCase(
                match {
                    it.presetIndex == presetIndex &&
                            it.to >= it.from
                },
            )
        }
        collectJob.cancel()
    }

    @Test
    fun onEvent_shouldUpdateUiState_whenPersistDateFilterFails() = runTest(testDispatcher) {
        coEvery { setHomeDateFilterStateUseCase(any()) } returns Result.failure(IllegalStateException("persist-failed"))
        val viewModel = buildViewModel()
        val collectJob = launch { viewModel.state.collect {} }

        val from = 1_712_000_000_000L
        val to = 1_712_100_000_000L
        viewModel.onEvent(HomeEvent.SetDateRange(from = from, to = to))
        advanceUntilIdle()

        coVerify(atLeast = 1) {
            setHomeDateFilterStateUseCase(
                match {
                    it.presetIndex == SavedDateFilter.CUSTOM_PRESET_INDEX &&
                            it.from == minOf(from, to) &&
                            it.to == maxOf(from, to)
                },
            )
        }
        assertTrue(viewModel.state.value.dateRangeFrom <= viewModel.state.value.dateRangeTo)
        coVerify(atLeast = 1) { setHomeDateFilterStateUseCase(any()) }

        collectJob.cancel()
    }

    private fun buildViewModel(): HomeViewModel = HomeViewModel(
        getTransactionsUseCase = getTransactionsUseCase,
        filterTransactionsUseCase = filterTransactionsUseCase,
        getTransactionSuggestionsUseCase = getTransactionSuggestionsUseCase,
        getHomeDateFilterStateUseCase = getHomeDateFilterStateUseCase,
        setHomeDateFilterStateUseCase = setHomeDateFilterStateUseCase,
        getCategoriesUseCase = getCategoriesUseCase,
        searchDebounceMs = 0L,
    )
}
