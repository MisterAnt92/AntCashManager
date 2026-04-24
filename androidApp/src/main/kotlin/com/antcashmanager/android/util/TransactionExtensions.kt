package com.antcashmanager.android.util

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType

/**
 * Normalizes transaction amount sign by type:
 * - INCOME  -> always positive
 * - EXPENSE -> always negative
 */
fun Transaction.withCorrectAmount(): Transaction = this.copy(
    amount = when (this.type) {
        TransactionType.INCOME -> kotlin.math.abs(this.amount)
        TransactionType.EXPENSE -> -kotlin.math.abs(this.amount)
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
