package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType

/**
 * Immutable data class for transaction filter parameters.
 * Uses default values for optional filters to enable efficient copy() updates.
 */
public data class TransactionFilterParams(
    public val searchQuery: String = "",
    public val categoryName: String? = null,
    public val transactionType: TransactionType? = null,
    public val paymentType: PaymentType? = null,
    public val dateFrom: Long = 0L,
    public val dateTo: Long = Long.MAX_VALUE,
) {
    /**
     * Returns true if any filter is active (excluding date range which is always applied).
     */
    public val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
                categoryName != null ||
                transactionType != null ||
                paymentType != null

    public companion object {
        /**
         * Empty filter params - returns all transactions within date range.
         */
        public val EMPTY: TransactionFilterParams = TransactionFilterParams()
    }
}

