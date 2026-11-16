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
    data class SelectCategoryEvent(
        val category: Category,
    ) : AddTransactionEvent

    data class SelectTypeEvent(
        val type: TransactionType,
    ) : AddTransactionEvent

    data class SelectPaymentTypeEvent(
        val paymentType: PaymentType,
    ) : AddTransactionEvent

    // ── Modifica campi ──
    data class UpdateTitleEvent(
        val title: String,
    ) : AddTransactionEvent

    data class UpdateAmountEvent(
        val amount: String,
    ) : AddTransactionEvent

    data class UpdateNotesEvent(
        val notes: String,
    ) : AddTransactionEvent

    data class UpdatePayeeEvent(
        val payee: String,
    ) : AddTransactionEvent

    data class UpdateLocationEvent(
        val location: String,
    ) : AddTransactionEvent

    data class UpdateTagsEvent(
        val tags: String,
    ) : AddTransactionEvent

    data class UpdateTimestampEvent(
        val timestamp: Long,
    ) : AddTransactionEvent

    data class SetRecurringEvent(
        val isRecurring: Boolean,
    ) : AddTransactionEvent

    data class UpdateRecurrenceIntervalEvent(
        val interval: String,
    ) : AddTransactionEvent

    data class UpdateMealVoucherCountEvent(
        val count: String,
    ) : AddTransactionEvent

    data class UpdateMealVoucherDifferenceEvent(
        val difference: String,
    ) : AddTransactionEvent

    // ── Navigazione ──
    data object NextStepEvent : AddTransactionEvent

    data object PreviousStepEvent : AddTransactionEvent

    data object SubmitEvent : AddTransactionEvent

    data object CancelEvent : AddTransactionEvent

    // ── Dialog inline per modifica rapida ──
    data object EditCategoryEvent : AddTransactionEvent

    data object EditTypeEvent : AddTransactionEvent

    data object EditDateEvent : AddTransactionEvent

    data object EditPaymentTypeEvent : AddTransactionEvent

    data object ShowCategoryDialogEvent : AddTransactionEvent

    data object ShowTypeDialogEvent : AddTransactionEvent

    data object ShowPaymentTypeDialogEvent : AddTransactionEvent

    data object DismissCategoryDialogEvent : AddTransactionEvent

    data object DismissTypeDialogEvent : AddTransactionEvent

    data object DismissPaymentTypeDialogEvent : AddTransactionEvent

    data object DismissDatePickerEvent : AddTransactionEvent

    // ── Dialog eliminazione ──
    data object ShowDeleteConfirmDialogEvent : AddTransactionEvent

    data object DismissDeleteConfirmDialogEvent : AddTransactionEvent

    data object ConfirmDeleteEvent : AddTransactionEvent
}
