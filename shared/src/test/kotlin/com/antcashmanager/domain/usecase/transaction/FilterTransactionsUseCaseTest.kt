package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilterTransactionsUseCaseTest {

    private val useCase = FilterTransactionsUseCase()

    private fun createTransaction(
        id: Long,
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        timestamp: Long,
        paymentType: PaymentType,
    ) = Transaction(
        id = id,
        title = title,
        amount = amount,
        category = category,
        type = type,
        timestamp = timestamp,
        paymentType = paymentType,
    )

    private val sampleTransactions = listOf(
        createTransaction(
            id = 1,
            title = "Salary",
            amount = 2500.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = 1000L,
            paymentType = PaymentType.ELECTRONIC,
        ),
        createTransaction(
            id = 2,
            title = "Groceries",
            amount = 85.50,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 2000L,
            paymentType = PaymentType.CASH,
        ),
        createTransaction(
            id = 3,
            title = "Electric Bill",
            amount = 120.0,
            category = "Utilities",
            type = TransactionType.EXPENSE,
            timestamp = 3000L,
            paymentType = PaymentType.ELECTRONIC,
        ),
        createTransaction(
            id = 4,
            title = "Restaurant Lunch",
            amount = 35.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 4000L,
            paymentType = PaymentType.MEAL_VOUCHERS,
        ),
        createTransaction(
            id = 5,
            title = "Freelance Work",
            amount = 500.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = 5000L,
            paymentType = PaymentType.ELECTRONIC,
        ),
    )

    @Test
    fun `empty filter params returns all transactions within date range`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(5, result.size)
    }

    @Test
    fun `filter by search query returns matching transactions`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                searchQuery = "salary",
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Salary", result[0].title)
    }

    @Test
    fun `search is case insensitive`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                searchQuery = "groc",
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Groceries", result[0].title)
    }

    @Test
    fun `search matches amount as string`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                searchQuery = "2500",
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Salary", result[0].title)
    }

    @Test
    fun `search matches amount when comma decimal separator is used`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                searchQuery = "85,5",
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Groceries", result[0].title)
    }

    @Test
    fun `filter by category returns only matching category`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                categoryName = "Food",
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "Food" })
    }

    @Test
    fun `filter by transaction type INCOME returns only incomes`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                transactionType = TransactionType.INCOME,
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(2, result.size)
        assertTrue(result.all { it.type == TransactionType.INCOME })
    }

    @Test
    fun `filter by transaction type EXPENSE returns only expenses`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                transactionType = TransactionType.EXPENSE,
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(3, result.size)
        assertTrue(result.all { it.type == TransactionType.EXPENSE })
    }

    @Test
    fun `filter by payment type returns only matching payments`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                paymentType = PaymentType.ELECTRONIC,
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(3, result.size)
        assertTrue(result.all { it.paymentType == PaymentType.ELECTRONIC })
    }

    @Test
    fun `filter by payment type CASH returns only cash transactions`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                paymentType = PaymentType.CASH,
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Groceries", result[0].title)
    }

    @Test
    fun `filter by payment type MEAL_VOUCHERS returns only meal voucher transactions`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                paymentType = PaymentType.MEAL_VOUCHERS,
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Restaurant Lunch", result[0].title)
    }

    @Test
    fun `combined filters work correctly`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                transactionType = TransactionType.EXPENSE,
                paymentType = PaymentType.ELECTRONIC,
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Electric Bill", result[0].title)
    }

    @Test
    fun `combined filters with search and category`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                searchQuery = "lunch",
                categoryName = "Food",
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Restaurant Lunch", result[0].title)
    }

    @Test
    fun `date range filter works correctly`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                dateFrom = 2000L,
                dateTo = 4000L,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertEquals(3, result.size)
        assertTrue(result.all { it.timestamp in 2000L..4000L })
    }

    @Test
    fun `empty list returns empty result`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = emptyList(),
            filterParams = TransactionFilterParams(),
        )

        val result = useCase(params).getOrThrow()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `no matching filters returns empty list`() = runTest {
        val params = FilterTransactionsUseCase.Params(
            transactions = sampleTransactions,
            filterParams = TransactionFilterParams(
                searchQuery = "nonexistent",
                dateFrom = 0L,
                dateTo = Long.MAX_VALUE,
            ),
        )

        val result = useCase(params).getOrThrow()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `hasActiveFilters returns true when search query is set`() {
        val params = TransactionFilterParams(searchQuery = "test")
        assertTrue(params.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters returns true when category is set`() {
        val params = TransactionFilterParams(categoryName = "Food")
        assertTrue(params.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters returns true when transaction type is set`() {
        val params = TransactionFilterParams(transactionType = TransactionType.INCOME)
        assertTrue(params.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters returns true when payment type is set`() {
        val params = TransactionFilterParams(paymentType = PaymentType.CASH)
        assertTrue(params.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters returns false when no filters set`() {
        val params = TransactionFilterParams()
        assertTrue(!params.hasActiveFilters)
    }
}
