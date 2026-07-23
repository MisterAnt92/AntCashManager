package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionMaintenanceUseCasesMockkTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var transactionRepository: TransactionRepository

    @BeforeTest
    fun setup() {
        transactionRepository = mockk()
    }

    @Test
    fun invoke_shouldReturnSuccess_whenUpdateTransactionSucceeds() = runTest(dispatcher) {
        val transaction = sampleTransaction()
        val useCase = UpdateTransactionUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        )
        coEvery { transactionRepository.updateTransaction(transaction) } just Runs

        val result = useCase(transaction)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { transactionRepository.updateTransaction(transaction) }
    }

    @Test
    fun invoke_shouldReturnFailure_whenUpdateTransactionThrows() = runTest(dispatcher) {
        val transaction = sampleTransaction()
        val expectedError = IllegalStateException("update-failed")
        val useCase = UpdateTransactionUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        )
        coEvery { transactionRepository.updateTransaction(transaction) } throws expectedError

        val result = useCase(transaction)

        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
        coVerify(exactly = 1) { transactionRepository.updateTransaction(transaction) }
    }

    @Test
    fun invoke_shouldEmitRepositoryTransactions_whenDateRangeIsProvided() = runTest(dispatcher) {
        val expected = listOf(sampleTransaction(id = 10L), sampleTransaction(id = 11L))
        val from = 1_710_000_000_000L
        val to = 1_720_000_000_000L
        var isRepositoryCalled = false
        val useCase = GetTransactionsByDateRangeUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        )
        every { transactionRepository.getTransactionsByDateRange(from, to) } answers {
            isRepositoryCalled = true
            flowOf(expected)
        }

        val result = useCase(DateRange(from = from, to = to)).take(1).toList().single().getOrThrow()

        assertEquals(expected, result)
        assertTrue(isRepositoryCalled)
    }

    @Test
    fun invoke_shouldRenameCategoryOnTransactions_whenCategoryNameChanges() = runTest(dispatcher) {
        val oldCategoryName = "Pranzi / Cenette fuori"
        val category = Category(
            id = 6L,
            name = "Pranzi/Cene fuori",
            icon = "local_dining",
            color = 0xFFE57373,
            type = "EXPENSE",
        )
        val useCase = SyncTransactionCategoriesUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        )
        coEvery {
            transactionRepository.renameCategory(
                oldCategoryName = oldCategoryName,
                newCategoryName = category.name,
                icon = category.icon,
                color = category.color,
            )
        } just Runs

        val result = useCase(SyncTransactionCategoriesUseCase.Params(oldCategoryName, category))

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            transactionRepository.renameCategory(
                oldCategoryName = oldCategoryName,
                newCategoryName = category.name,
                icon = category.icon,
                color = category.color,
            )
        }
    }

    @Test
    fun invoke_shouldReturnFailure_whenTransactionRepositoryThrows() = runTest(dispatcher) {
        val oldCategoryName = "Food"
        val category = Category(
            id = 7L,
            name = "Food",
            icon = "restaurant",
            color = 0xFFE57373,
            type = "EXPENSE",
        )
        val useCase = SyncTransactionCategoriesUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        )
        val expectedError = IllegalStateException("rename-failed")
        coEvery {
            transactionRepository.renameCategory(any(), any(), any(), any())
        } throws expectedError

        val result = useCase(SyncTransactionCategoriesUseCase.Params(oldCategoryName, category))

        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun invoke_shouldInsertOccurrencesAndUpdateTemplate_whenRecurringTransactionIsPastDue() =
        runTest(dispatcher) {
            val dayMillis = 24L * 60 * 60 * 1000
            val timestamp = Clock.System.now().toEpochMilliseconds() - (2 * dayMillis + 1_000)
            val recurringTemplate = sampleTransaction(
                id = 101L,
                timestamp = timestamp,
                isRecurring = true,
                recurrenceInterval = "DAILY",
            )
            every { transactionRepository.getRecurringTransactions() } returns flowOf(
                listOf(
                    recurringTemplate
                )
            )
            coEvery { transactionRepository.insertTransactions(any()) } returns listOf(1L, 2L)
            coEvery { transactionRepository.updateTransactions(any()) } just Runs

            ProcessRecurringTransactionsUseCase(
                transactionRepository,
                Clock.System,
                dispatcher
            ).invoke()

            coVerify(exactly = 1) {
                transactionRepository.insertTransactions(
                    match { list ->
                        list.size == 2 && list.all { !it.isRecurring && it.recurrenceInterval.isEmpty() }
                    }
                )
            }
            coVerify(exactly = 1) {
                transactionRepository.updateTransactions(
                    match { list ->
                        list.size == 1 && list[0].id == recurringTemplate.id &&
                                list[0].timestamp >= recurringTemplate.timestamp + (2 * dayMillis)
                    }
                )
            }
        }

    @Test
    fun invoke_shouldSkipRecurringTransaction_whenRecurrenceIntervalIsInvalid() =
        runTest(dispatcher) {
            val recurringTemplate = sampleTransaction(
                id = 202L,
                isRecurring = true,
                recurrenceInterval = "INVALID",
            )
            every { transactionRepository.getRecurringTransactions() } returns flowOf(
                listOf(
                    recurringTemplate
                )
            )

            ProcessRecurringTransactionsUseCase(transactionRepository).invoke()

            coVerify(exactly = 0) { transactionRepository.insertTransaction(any()) }
            coVerify(exactly = 0) { transactionRepository.updateTransaction(any()) }
        }

    private fun sampleTransaction(
        id: Long = 1L,
        timestamp: Long = 1_715_000_000_000L,
        isRecurring: Boolean = false,
        recurrenceInterval: String = "",
    ): Transaction = Transaction(
        id = id,
        title = "Groceries",
        amount = 24.50,
        category = "Food",
        type = TransactionType.EXPENSE,
        timestamp = timestamp,
        notes = "Weekly shopping",
        payee = "Market",
        location = "Rome",
        isRecurring = isRecurring,
        tags = "home,food",
        recurrenceInterval = recurrenceInterval,
        paymentType = PaymentType.ELECTRONIC,
        categoryIcon = "shopping_cart",
        categoryColor = 0xFF90A4AE,
    )
}

