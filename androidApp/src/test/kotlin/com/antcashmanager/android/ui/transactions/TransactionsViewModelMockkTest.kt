package com.antcashmanager.android.ui.transactions

import com.antcashmanager.android.ui.screen.transactions.TransactionsEvent
import com.antcashmanager.android.ui.screen.transactions.TransactionsViewModel
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.FilterTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelMockkTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    private lateinit var insertTransactionUseCase: InsertTransactionUseCase
    private lateinit var updateTransactionUseCase: UpdateTransactionUseCase
    private lateinit var deleteTransactionUseCase: DeleteTransactionUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var filterTransactionsUseCase: FilterTransactionsUseCase
    private lateinit var getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase
    private lateinit var getTransactionsDateFilterStateUseCase: GetTransactionsDateFilterStateUseCase
    private lateinit var setTransactionsDateFilterStateUseCase: SetTransactionsDateFilterStateUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getTransactionsUseCase = mockk()
        insertTransactionUseCase = mockk()
        updateTransactionUseCase = mockk(relaxed = true)
        deleteTransactionUseCase = mockk()
        getCategoriesUseCase = mockk()
        filterTransactionsUseCase = mockk()
        getTransactionSuggestionsUseCase = mockk()
        getTransactionsDateFilterStateUseCase = mockk()
        setTransactionsDateFilterStateUseCase = mockk(relaxed = true)

        every { getTransactionsUseCase() } returns flowOf(Result.success(emptyList()))
        every { getCategoriesUseCase() } returns flowOf(Result.success(emptyList()))
        coEvery { filterTransactionsUseCase(any()) } returns Result.success(emptyList())
        every { getTransactionSuggestionsUseCase() } returns flowOf(TransactionSuggestions())
        every { getTransactionsDateFilterStateUseCase() } returns flowOf(
            Result.success(
                SavedDateFilter(
                    presetIndex = 1,
                    from = 1_700_000_000_000L,
                    to = 1_700_100_000_000L,
                ),
            ),
        )
        coEvery { insertTransactionUseCase(any()) } returns Result.success(100L)
        coEvery { deleteTransactionUseCase(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addTransaction_shouldCallInsertUseCase_whenInputIsValid() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.addTransaction(
            title = "Lunch",
            amount = 18.5,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 1_712_000_000_000L,
            notes = "menu",
            payee = "Cafe",
        )
        advanceUntilIdle()

        coVerify(atLeast = 1) {
            insertTransactionUseCase(
                match {
                    it.title == "Lunch" &&
                            it.amount == 18.5 &&
                            it.category == "Food" &&
                            it.type == TransactionType.EXPENSE &&
                            it.notes == "menu" &&
                            it.payee == "Cafe"
                },
            )
        }
    }

    @Test
    fun onEvent_shouldCallDeleteUseCase_whenDeleteTransactionEventArrives() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val transaction = Transaction(
            id = 44L,
            title = "Subscription",
            amount = 9.99,
            category = "Services",
            type = TransactionType.EXPENSE,
            timestamp = 1_712_100_000_000L,
        )

        viewModel.onEvent(TransactionsEvent.DeleteTransaction(transaction))
        advanceUntilIdle()

        coVerify(atLeast = 1) { deleteTransactionUseCase(transaction) }
    }

    private fun buildViewModel(): TransactionsViewModel = TransactionsViewModel(
        getTransactionsUseCase = getTransactionsUseCase,
        insertTransactionUseCase = insertTransactionUseCase,
        updateTransactionUseCase = updateTransactionUseCase,
        deleteTransactionUseCase = deleteTransactionUseCase,
        getCategoriesUseCase = getCategoriesUseCase,
        filterTransactionsUseCase = filterTransactionsUseCase,
        getTransactionSuggestionsUseCase = getTransactionSuggestionsUseCase,
        getTransactionsDateFilterStateUseCase = getTransactionsDateFilterStateUseCase,
        setTransactionsDateFilterStateUseCase = setTransactionsDateFilterStateUseCase,
    )
}

