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
    data class SelectPresetEvent(
        val index: Int,
    ) : TransactionsEvent

    data class SetDateRangeEvent(
        val from: Long,
        val to: Long,
    ) : TransactionsEvent

    // Search & Filter events
    data class UpdateSearchQueryEvent(
        val query: String,
    ) : TransactionsEvent

    data class UpdateCategoryFilterEvent(
        val category: String?,
    ) : TransactionsEvent

    data class UpdateTransactionTypeFilterEvent(
        val type: TransactionType?,
    ) : TransactionsEvent

    data class UpdatePaymentTypeFilterEvent(
        val paymentType: PaymentType?,
    ) : TransactionsEvent

    data object ToggleSearchExpandedEvent : TransactionsEvent

    data object ToggleFiltersExpandedEvent : TransactionsEvent

    data object ApplyFiltersEvent : TransactionsEvent

    data object CancelFilterChangesEvent : TransactionsEvent

    data object ClearAllFiltersEvent : TransactionsEvent

    data class SetDateFilterExpandedEvent(
        val expanded: Boolean,
    ) : TransactionsEvent

    // Transaction CRUD events
    data object AddTransactionClickedEvent : TransactionsEvent

    data class AddTransactionEvent(
        val title: String,
        val amount: Double,
        val category: String,
        val type: TransactionType,
        val timestamp: Long,
        val notes: String = "",
        val payee: String = "",
        val location: String = "",
        val tags: String = "",
        val isRecurring: Boolean = false,
        val recurrenceInterval: String = "",
    ) : TransactionsEvent

    data class DeleteTransactionEvent(
        val transaction: Transaction,
    ) : TransactionsEvent

    data class UpdateTransactionEvent(
        val transaction: Transaction,
    ) : TransactionsEvent

    data class ShowTransactionDetailsEvent(
        val index: Int,
    ) : TransactionsEvent

    data object DismissTransactionDetailsEvent : TransactionsEvent

    data object HelpOpenedEvent : TransactionsEvent

    data object ReceiptScanOpenedEvent : TransactionsEvent

    data object AddTransactionOpenedEvent : TransactionsEvent
}
