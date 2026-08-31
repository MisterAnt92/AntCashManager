package com.antcashmanager.android.ui.screen.transactions.event

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType

// ══════════════════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * UI Events for Transactions screen.
 */
sealed interface TransactionsEvent {
    // Date range events
    data class SelectPreset(val index: Int) : TransactionsEvent
    data class SetDateRange(val from: Long, val to: Long) : TransactionsEvent

    // Search & Filter events
    data class UpdateSearchQuery(val query: String) : TransactionsEvent
    data class UpdateCategoryFilter(val category: String?) : TransactionsEvent
    data class UpdateTransactionTypeFilter(val type: TransactionType?) : TransactionsEvent
    data class UpdatePaymentTypeFilter(val paymentType: PaymentType?) : TransactionsEvent
    data object ToggleSearchExpanded : TransactionsEvent
    data object ToggleFiltersExpanded : TransactionsEvent
    data object ApplyFilters : TransactionsEvent
    data object CancelFilterChanges : TransactionsEvent
    data object ClearAllFilters : TransactionsEvent
    data class SetDateFilterExpanded(val expanded: Boolean) : TransactionsEvent

    // Transaction CRUD events
    data object AddTransactionClicked : TransactionsEvent
    data class DeleteTransaction(val transaction: Transaction) : TransactionsEvent
    data class UpdateTransaction(val transaction: Transaction) : TransactionsEvent
}