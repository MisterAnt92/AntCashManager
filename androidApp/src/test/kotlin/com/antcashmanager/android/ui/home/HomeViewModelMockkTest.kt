package com.antcashmanager.android.ui.home

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.tracker.SegmentationTracker
import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.android.ui.screen.home.event.HomeEvent
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.SettingsRepository
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelMockkTest : BaseUnitTest() {
    private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    private lateinit var filterTransactionsUseCase: FilterTransactionsUseCase
    private lateinit var getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase
    private lateinit var getHomeDateFilterStateUseCase: GetHomeDateFilterStateUseCase
    private lateinit var setHomeDateFilterStateUseCase: SetHomeDateFilterStateUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var segmentationTracker: SegmentationTracker

    @Before
    fun setup() {
        getTransactionsUseCase = mockk()
        filterTransactionsUseCase = mockk()
        getTransactionSuggestionsUseCase = mockk()
        getHomeDateFilterStateUseCase = mockk()
        setHomeDateFilterStateUseCase = mockk()
        getCategoriesUseCase = mockk()
        settingsRepository = mockk(relaxed = true)
        segmentationTracker = mockk(relaxed = true)

        every { getTransactionsUseCase() } returns flowOf(Result.success(emptyList()))
        coEvery { filterTransactionsUseCase(any()) } answers {
            val params = firstArg<FilterTransactionsUseCase.Params>()
            Result.success(params.transactions)
        }
        every { getTransactionSuggestionsUseCase() } returns
            flowOf(
                Result.success(
                    TransactionSuggestions(),
                ),
            )
        every { getHomeDateFilterStateUseCase() } returns
            flowOf(
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

    @Test
    fun onEvent_shouldPersistCustomFilter_whenSetDateRangeEventIsReceived() =
        runViewModelTest {
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
    fun onEvent_shouldNormalizeRange_whenFromIsGreaterThanTo() =
        runViewModelTest {
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
    fun onEvent_shouldPersistPresetFilter_whenSelectPresetEventIsReceived() =
        runViewModelTest {
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
    fun onEvent_shouldUpdateUiState_whenPersistDateFilterFails() =
        runViewModelTest {
            coEvery { setHomeDateFilterStateUseCase(any()) } returns
                Result.failure(
                    IllegalStateException("persist-failed"),
                )
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

    @Test
    fun totalsAndBalance_shouldBeCalculatedCorrectly_whenTransactionsContainIncomeAndExpense() =
        runViewModelTest {
            val now = 1_700_000_000_000L
            val transactions =
                listOf(
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
                        title = "Freelance",
                        amount = 500.0,
                        category = "Work",
                        type = TransactionType.INCOME,
                        timestamp = now,
                    ),
                    Transaction(
                        id = 3,
                        title = "Rent",
                        amount = 800.0,
                        category = "Home",
                        type = TransactionType.EXPENSE,
                        timestamp = now,
                    ),
                    Transaction(
                        id = 4,
                        title = "Groceries",
                        amount = 200.0,
                        category = "Food",
                        type = TransactionType.EXPENSE,
                        timestamp = now,
                    ),
                )

            every { getTransactionsUseCase() } returns flowOf(Result.success(transactions))
            coEvery { filterTransactionsUseCase(any()) } answers {
                Result.success(firstArg<FilterTransactionsUseCase.Params>().transactions)
            }

            val viewModel = buildViewModel()
            val collectJob = launch { viewModel.state.collect {} }
            advanceUntilIdle()

            viewModel.onEvent(HomeEvent.SetDateRange(0L, Long.MAX_VALUE))
            advanceUntilIdle()

            val uiState = viewModel.state.value
            assertEquals(2500.0, uiState.totalIncome, 0.01)
            assertEquals(-1000.0, uiState.totalExpense, 0.01)
            assertEquals(1500.0, uiState.balance, 0.01)

            collectJob.cancel()
        }

    @Test
    fun totals_shouldUseNormalizedAmountSigns_whenStoredTransactionSignsAreInconsistent() =
        runViewModelTest {
            val now = 1_700_000_000_000L
            val transactions =
                listOf(
                    Transaction(
                        id = 1,
                        title = "Refund",
                        amount = -120.0,
                        category = "Other",
                        type = TransactionType.INCOME,
                        timestamp = now,
                    ),
                    Transaction(
                        id = 2,
                        title = "Coffee",
                        amount = 20.0,
                        category = "Food",
                        type = TransactionType.EXPENSE,
                        timestamp = now,
                    ),
                )

            every { getTransactionsUseCase() } returns flowOf(Result.success(transactions))
            coEvery { filterTransactionsUseCase(any()) } answers {
                Result.success(firstArg<FilterTransactionsUseCase.Params>().transactions)
            }

            val viewModel = buildViewModel()
            val collectJob = launch { viewModel.state.collect {} }
            advanceUntilIdle()

            viewModel.onEvent(HomeEvent.SetDateRange(0L, Long.MAX_VALUE))
            advanceUntilIdle()

            val uiState = viewModel.state.value
            assertEquals(120.0, uiState.totalIncome, 0.01)
            assertEquals(-20.0, uiState.totalExpense, 0.01)
            assertEquals(100.0, uiState.balance, 0.01)

            collectJob.cancel()
        }

    private fun buildViewModel(): HomeViewModel =
        HomeViewModel(
            getTransactionsUseCase = getTransactionsUseCase,
            filterTransactionsUseCase = filterTransactionsUseCase,
            getTransactionSuggestionsUseCase = getTransactionSuggestionsUseCase,
            getHomeDateFilterStateUseCase = getHomeDateFilterStateUseCase,
            setHomeDateFilterStateUseCase = setHomeDateFilterStateUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            settingsRepository = settingsRepository,
            searchDebounceMs = 0L,
            segmentationTracker = segmentationTracker,
        )
}
