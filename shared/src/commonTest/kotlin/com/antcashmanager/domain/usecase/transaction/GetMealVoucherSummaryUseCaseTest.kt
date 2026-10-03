package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.MealVoucherSummary
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.testutil.BaseUseCaseTest
import com.antcashmanager.testutil.testTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetMealVoucherSummaryUseCaseTest : BaseUseCaseTest() {

    private val useCase = GetMealVoucherSummaryUseCase()

    @Test
    fun execute_shouldReturnNull_whenTransactionsListIsEmpty() = runUnitTest {
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = emptyList(),
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertNull(result)
    }

    @Test
    fun execute_shouldReturnNull_whenNoMealVoucherTransactions() = runUnitTest {
        val transactions = listOf(
            testTransaction { paymentType = PaymentType.ELECTRONIC },
            testTransaction { paymentType = PaymentType.CASH },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertNull(result)
    }

    @Test
    fun execute_shouldReturnNull_whenMealVoucherCountIsAlwaysZero() = runUnitTest {
        val transactions = listOf(
            testTransaction { paymentType = PaymentType.MEAL_VOUCHERS },
            testTransaction { paymentType = PaymentType.MEAL_VOUCHERS },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertNull(result)
    }

    @Test
    fun execute_shouldReturnCorrectSummary_whenOnlyExpenseTransactions() = runUnitTest {
        val transactions = listOf(
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 5
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 3
            },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertEquals(0, result?.received)
        assertEquals(8, result?.used)
        assertEquals(-8, result?.remaining)
        assertEquals(5.29, result?.voucherValue)
    }

    @Test
    fun execute_shouldReturnCorrectSummary_whenOnlyIncomeTransactions() = runUnitTest {
        val transactions = listOf(
            testTransaction {
                type = TransactionType.INCOME
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 22
            },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertEquals(22, result?.received)
        assertEquals(0, result?.used)
        assertEquals(22, result?.remaining)
    }

    @Test
    fun execute_shouldCalculateRemainingCorrectly_whenBothIncomeAndExpense() = runUnitTest {
        val transactions = listOf(
            testTransaction {
                type = TransactionType.INCOME
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 22
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 5
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 3
            },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertEquals(22, result?.received)
        assertEquals(8, result?.used)
        assertEquals(14, result?.remaining)
    }

    @Test
    fun execute_shouldAllowNegativeRemaining_whenUsedExceedsReceived() = runUnitTest {
        val transactions = listOf(
            testTransaction {
                type = TransactionType.INCOME
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 10
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 15
            },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertEquals(10, result?.received)
        assertEquals(15, result?.used)
        assertEquals(-5, result?.remaining)
    }

    @Test
    fun execute_shouldCalculateMonetaryValuesCorrectly() = runUnitTest {
        val voucherValue = 5.29
        val transactions = listOf(
            testTransaction {
                type = TransactionType.INCOME
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 20
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 8
            },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = voucherValue,
        )

        val result = useCase.invoke(params).getOrNull()

        val remaining = 20 - 8
        val expectedRemainingValue = remaining * voucherValue
        val expectedUsedValue = 8 * voucherValue

        assertEquals(expectedRemainingValue, result?.remainingValue)
        assertEquals(expectedUsedValue, result?.usedValue)
    }

    @Test
    fun execute_shouldIgnoreNonMealVoucherTransactions() = runUnitTest {
        val transactions = listOf(
            testTransaction {
                type = TransactionType.INCOME
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 15
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.ELECTRONIC
                mealVoucherCount = 100
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 5
            },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertEquals(15, result?.received)
        assertEquals(5, result?.used)
        assertEquals(10, result?.remaining)
    }

    @Test
    fun execute_shouldSumMultipleTransactionCounts() = runUnitTest {
        val transactions = listOf(
            testTransaction {
                type = TransactionType.INCOME
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 11
            },
            testTransaction {
                type = TransactionType.INCOME
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 11
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 2
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 3
            },
            testTransaction {
                type = TransactionType.EXPENSE
                paymentType = PaymentType.MEAL_VOUCHERS
                mealVoucherCount = 5
            },
        )
        val params = GetMealVoucherSummaryUseCase.Params(
            transactions = transactions,
            voucherValue = 5.29,
        )

        val result = useCase.invoke(params).getOrNull()

        assertEquals(22, result?.received)
        assertEquals(10, result?.used)
        assertEquals(12, result?.remaining)
    }
}
