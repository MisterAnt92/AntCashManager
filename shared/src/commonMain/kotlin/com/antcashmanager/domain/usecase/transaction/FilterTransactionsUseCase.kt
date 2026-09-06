package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.math.abs

/**
 * UseCase for filtering transactions based on multiple criteria.
 * Optimized for performance using sequence-based lazy evaluation.
 *
 * Filters applied:
 * - Date range (always applied)
 * - Search query (case-insensitive, matches title or amount)
 * - Category name (exact match)
 * - Transaction type (INCOME/EXPENSE)
 * - Payment type (ELECTRONIC/CASH/MEAL_VOUCHERS)
 */
public class FilterTransactionsUseCase(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<FilterTransactionsUseCase.Params, List<Transaction>>(dispatcher) {
    public companion object {
        private const val AMOUNT_COMPARISON_EPSILON = 0.000001
    }

    public data class Params(
        val transactions: List<Transaction>,
        val filterParams: TransactionFilterParams,
    )

    override suspend fun execute(params: Params): List<Transaction> {
        val (transactions, filterParams) = params

        // Early return for empty list
        if (transactions.isEmpty()) return emptyList()

        // Use sequence for lazy evaluation - avoids intermediate list allocations
        return transactions
            .asSequence()
            .filter { transaction ->
                // Date range filter (always applied, short-circuit first)
                transaction.timestamp in filterParams.dateFrom..filterParams.dateTo
            }.filter { transaction ->
                // Search query filter (case-insensitive)
                val rawQuery = filterParams.searchQuery.trim()
                if (rawQuery.isBlank()) {
                    true
                } else {
                    val normalizedQuery = rawQuery.replace(',', '.')
                    val numericQuery = normalizedQuery.toDoubleOrNull()
                    val absAmount = abs(transaction.amount)

                    transaction.title.contains(rawQuery, ignoreCase = true) ||
                        transaction.amount.toString().contains(normalizedQuery) ||
                        absAmount.toString().contains(normalizedQuery) ||
                        (
                            numericQuery != null &&
                                abs(absAmount - numericQuery) < AMOUNT_COMPARISON_EPSILON
                        )
                }
            }.filter { transaction ->
                // Category filter (exact match)
                filterParams.categoryName == null ||
                    transaction.category == filterParams.categoryName
            }.filter { transaction ->
                // Transaction type filter
                filterParams.transactionType == null ||
                    transaction.type == filterParams.transactionType
            }.filter { transaction ->
                // Payment type filter
                filterParams.paymentType == null ||
                    transaction.paymentType == filterParams.paymentType
            }.toList()
    }
}
