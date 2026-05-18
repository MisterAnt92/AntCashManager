package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType

/**
 * Immutable data class for transaction filter parameters.
 * Uses default values for optional filters to enable efficient copy() updates.
 */
data class TransactionFilterParams(
    val searchQuery: String = "",
    val categoryName: String? = null,
    val transactionType: TransactionType? = null,
    val paymentType: PaymentType? = null,
    val dateFrom: Long = 0L,
    val dateTo: Long = Long.MAX_VALUE,
) {
    /**
     * Returns true if any filter is active (excluding date range which is always applied).
     */
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
                categoryName != null ||
                transactionType != null ||
                paymentType != null

    companion object {
        /**
         * Empty filter params - returns all transactions within date range.
         */
        val EMPTY = TransactionFilterParams()
    }
}

