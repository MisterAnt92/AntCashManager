package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.usecase.BaseUseCase

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
class FilterTransactionsUseCase : BaseUseCase<FilterTransactionsUseCase.Params, Result<List<Transaction>>>() {

    data class Params(
        val transactions: List<Transaction>,
        val filterParams: TransactionFilterParams,
    )

    override suspend fun execute(params: Params): Result<List<Transaction>> = runCatching {
        val (transactions, filterParams) = params

        // Early return for empty list
        if (transactions.isEmpty()) return@runCatching emptyList()

        // Use sequence for lazy evaluation - avoids intermediate list allocations
        transactions.asSequence()
            .filter { transaction ->
                // Date range filter (always applied, short-circuit first)
                transaction.timestamp in filterParams.dateFrom..filterParams.dateTo
            }
            .filter { transaction ->
                // Search query filter (case-insensitive)
                filterParams.searchQuery.isBlank() ||
                        transaction.title.contains(filterParams.searchQuery, ignoreCase = true) ||
                        transaction.amount.toString().contains(filterParams.searchQuery)
            }
            .filter { transaction ->
                // Category filter (exact match)
                filterParams.categoryName == null ||
                        transaction.category == filterParams.categoryName
            }
            .filter { transaction ->
                // Transaction type filter
                filterParams.transactionType == null ||
                        transaction.type == filterParams.transactionType
            }
            .filter { transaction ->
                // Payment type filter
                filterParams.paymentType == null ||
                        transaction.paymentType == filterParams.paymentType
            }
            .toList()
    }
}

