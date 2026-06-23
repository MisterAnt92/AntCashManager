package com.antcashmanager.android.util

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionExtensionsTest {

    @Test
    fun withCorrectAmount_shouldReturnPositiveAmount_whenTransactionTypeIsIncome() {
        val income = Transaction(
            id = 1,
            title = "Income",
            amount = -150.0,
            category = "Work",
            type = TransactionType.INCOME,
        )

        assertEquals(150.0, income.withCorrectAmount().amount, 0.01)
    }

    @Test
    fun withCorrectAmount_shouldReturnNegativeAmount_whenTransactionTypeIsExpense() {
        val expense = Transaction(
            id = 2,
            title = "Expense",
            amount = 80.0,
            category = "Food",
            type = TransactionType.EXPENSE,
        )

        assertEquals(-80.0, expense.withCorrectAmount().amount, 0.01)
    }

    @Test
    fun withCorrectAmounts_shouldKeepTotalsCoherent_whenSignsAreNormalized() {
        val list = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 1000.0,
                category = "Work",
                type = TransactionType.INCOME,
            ),
            Transaction(
                id = 2,
                title = "Refund",
                amount = -100.0,
                category = "Other",
                type = TransactionType.INCOME,
            ),
            Transaction(
                id = 3,
                title = "Rent",
                amount = 400.0,
                category = "Home",
                type = TransactionType.EXPENSE,
            ),
        ).withCorrectAmounts()

        assertEquals(1100.0, list.calculateTotalIncome(), 0.01)
        assertEquals(-400.0, list.calculateTotalExpense(), 0.01)
        assertEquals(700.0, list.calculateBalance(), 0.01)
    }
}

