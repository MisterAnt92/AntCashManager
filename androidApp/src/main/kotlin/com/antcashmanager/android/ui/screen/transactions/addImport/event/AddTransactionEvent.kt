package com.antcashmanager.android.ui.screen.transactions.addImport.event

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType

// ══════════════════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Events per la schermata di aggiunta/modifica transazione.
 * Flusso semplificato: Categoria → Dettagli (con salvataggio diretto).
 */
sealed interface AddTransactionEvent {
    // ── Selezione ──
    data class SelectCategory(
        val category: Category,
    ) : AddTransactionEvent

    data class SelectType(
        val type: TransactionType,
    ) : AddTransactionEvent

    data class SelectPaymentType(
        val paymentType: PaymentType,
    ) : AddTransactionEvent

    // ── Modifica campi ──
    data class UpdateTitle(
        val title: String,
    ) : AddTransactionEvent

    data class UpdateAmount(
        val amount: String,
    ) : AddTransactionEvent

    data class UpdateNotes(
        val notes: String,
    ) : AddTransactionEvent

    data class UpdatePayee(
        val payee: String,
    ) : AddTransactionEvent

    data class UpdateLocation(
        val location: String,
    ) : AddTransactionEvent

    data class UpdateTags(
        val tags: String,
    ) : AddTransactionEvent

    data class UpdateTimestamp(
        val timestamp: Long,
    ) : AddTransactionEvent

    data class SetRecurring(
        val isRecurring: Boolean,
    ) : AddTransactionEvent

    data class UpdateRecurrenceInterval(
        val interval: String,
    ) : AddTransactionEvent

    data class UpdateMealVoucherCount(
        val count: String,
    ) : AddTransactionEvent

    data class UpdateMealVoucherDifference(
        val difference: String,
    ) : AddTransactionEvent

    // ── Navigazione ──
    data object NextStep : AddTransactionEvent

    data object PreviousStep : AddTransactionEvent

    data object Submit : AddTransactionEvent

    data object Cancel : AddTransactionEvent

    // ── Dialog inline per modifica rapida ──
    data object EditCategory : AddTransactionEvent

    data object EditType : AddTransactionEvent

    data object EditDate : AddTransactionEvent

    data object EditPaymentType : AddTransactionEvent

    data object ShowCategoryDialog : AddTransactionEvent

    data object ShowTypeDialog : AddTransactionEvent

    data object ShowPaymentTypeDialog : AddTransactionEvent

    data object DismissCategoryDialog : AddTransactionEvent

    data object DismissTypeDialog : AddTransactionEvent

    data object DismissPaymentTypeDialog : AddTransactionEvent

    data object DismissDatePicker : AddTransactionEvent

    // ── Dialog eliminazione ──
    data object ShowDeleteConfirmDialog : AddTransactionEvent

    data object DismissDeleteConfirmDialog : AddTransactionEvent

    data object ConfirmDelete : AddTransactionEvent
}
