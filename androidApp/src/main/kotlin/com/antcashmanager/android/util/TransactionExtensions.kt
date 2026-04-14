package com.antcashmanager.android.util

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType

/**
 * Transforms transaction amount to be negative for EXPENSE types.
 * This ensures consistent behavior across the app where expenses are negative values.
 */
fun Transaction.withCorrectAmount(): Transaction = this.copy(
    amount = if (this.type == TransactionType.EXPENSE && this.amount > 0) {
        -this.amount
    } else {
        this.amount
    }
)

/**
 * Transforms a list of transactions to have correct amounts (negative for expenses).
 */
fun List<Transaction>.withCorrectAmounts(): List<Transaction> = this.map { it.withCorrectAmount() }

/**
 * Calculates the total income from a list of transactions (positive values only).
 */
fun List<Transaction>.calculateTotalIncome(): Double =
    this.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

/**
 * Calculates the total expenses from a list of transactions (negative values).
 */
fun List<Transaction>.calculateTotalExpense(): Double =
    this.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

/**
 * Calculates the balance from a list of transactions (sum of all amounts).
 */
fun List<Transaction>.calculateBalance(): Double = this.sumOf { it.amount }
