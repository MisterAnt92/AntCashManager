package com.antcashmanager.android.ui.screen.transactionAdd

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType

// ══════════════════════════════════════════════════════════════════════════════
// STATE
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Flusso semplificato:
 * - Nuova transazione: Categoria → Dettagli (salvataggio diretto)
 * - Modifica transazione: Dettagli (salvataggio diretto)
 *
 * Categoria, Tipo e Data sono sempre modificabili al tap tramite dialog.
 */
enum class AddTransactionStep {
    CATEGORY_SELECTION,
    DETAILS,
}

/**
 * Data class che rappresenta lo stato della schermata di aggiunta/modifica transazione.
 */
data class AddTransactionState(
    // ── Navigazione ──
    val currentStep: AddTransactionStep = AddTransactionStep.CATEGORY_SELECTION,
    val isModifying: Boolean = false,
    val transactionId: Long? = null,

    // ── Dati categoria e tipo ──
    val selectedCategory: Category? = null,
    val selectedType: TransactionType? = null,

    // ── Dettagli transazione ──
    val title: String = "",
    val amount: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val payee: String = "",
    val location: String = "",
    val tags: String = "",
    val isRecurring: Boolean = false,
    val recurrenceInterval: String = "",
    val selectedPaymentType: PaymentType = PaymentType.ELECTRONIC,

    // ── Dati disponibili ──
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTransactionSaved: Boolean = false,

    // ── Dialog states ──
    val showCategoryDialog: Boolean = false,
    val showTypeDialog: Boolean = false,
    val showDatePicker: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showPaymentTypeDialog: Boolean = false,
) {
    /**
     * Verifica se il form è valido per il salvataggio.
     */
    val isFormValid: Boolean
        get() = title.isNotBlank() &&
                amount.isNotBlank() &&
                amount.toDoubleOrNull() != null &&
                selectedCategory != null &&
                selectedType != null
}

/**
 * Data class per i filtri di transazione.
 */
internal data class FilterState(
    val selectedPresetIndex: Int = 0,
    val dateRangeFrom: Long = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
)
