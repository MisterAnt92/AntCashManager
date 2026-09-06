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

    private val sampleTransactions =
        listOf(
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
    fun emptyFilterParamsReturnsAllTransactionsWithinDateRange() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
                            dateFrom = 0L,
                            dateTo = Long.MAX_VALUE,
                        ),
                )

            val result = useCase(params).getOrThrow()

            assertEquals(5, result.size)
        }

    @Test
    fun filterBySearchQueryReturnsMatchingTransactions() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun searchIsCaseInsensitive() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun searchMatchesAmountAsString() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun searchMatchesAmountWhenCommaDecimalSeparatorIsUsed() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun filterByCategoryReturnsOnlyMatchingCategory() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun filterByTransactionTypeINCOMEReturnsOnlyIncomes() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun filterByTransactionTypeEXPENSEReturnsOnlyExpenses() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun filterByPaymentTypeReturnsOnlyMatchingPayments() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun filterByPaymentTypeCASHReturnsOnlyCashTransactions() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun filterByPaymentTypeMEALVOUCHERSReturnsOnlyMealVoucherTransactions() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun combinedFiltersWorkCorrectly() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun combinedFiltersWithSearchAndCategory() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
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
    fun dateRangeFilterWorksCorrectly() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
                            dateFrom = 2000L,
                            dateTo = 4000L,
                        ),
                )

            val result = useCase(params).getOrThrow()

            assertEquals(3, result.size)
            assertTrue(result.all { it.timestamp in 2000L..4000L })
        }

    @Test
    fun emptyListReturnsEmptyResult() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = emptyList(),
                    filterParams = TransactionFilterParams(),
                )

            val result = useCase(params).getOrThrow()

            assertTrue(result.isEmpty())
        }

    @Test
    fun noMatchingFiltersReturnsEmptyList() =
        runTest {
            val params =
                FilterTransactionsUseCase.Params(
                    transactions = sampleTransactions,
                    filterParams =
                        TransactionFilterParams(
                            searchQuery = "nonexistent",
                            dateFrom = 0L,
                            dateTo = Long.MAX_VALUE,
                        ),
                )

            val result = useCase(params).getOrThrow()

            assertTrue(result.isEmpty())
        }

    @Test
    fun hasActiveFiltersReturnsTrueWhenSearchQueryIsSet() {
        val params = TransactionFilterParams(searchQuery = "test")
        assertTrue(params.hasActiveFilters)
    }

    @Test
    fun hasActiveFiltersReturnsTrueWhenCategoryIsSet() {
        val params = TransactionFilterParams(categoryName = "Food")
        assertTrue(params.hasActiveFilters)
    }

    @Test
    fun hasActiveFiltersReturnsTrueWhenTransactionTypeIsSet() {
        val params = TransactionFilterParams(transactionType = TransactionType.INCOME)
        assertTrue(params.hasActiveFilters)
    }

    @Test
    fun hasActiveFiltersReturnsTrueWhenPaymentTypeIsSet() {
        val params = TransactionFilterParams(paymentType = PaymentType.CASH)
        assertTrue(params.hasActiveFilters)
    }

    @Test
    fun hasActiveFiltersReturnsFalseWhenNoFiltersSet() {
        val params = TransactionFilterParams()
        assertTrue(!params.hasActiveFilters)
    }
}
