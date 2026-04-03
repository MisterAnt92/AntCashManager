package com.antcashmanager.android.ui.screen.transaction_add

import com.antcashmanager.domain.model.Category
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

    // ── Dati disponibili ──
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTransactionSaved: Boolean = false,

    // ── Dialog states ──
    val showCategoryDialog: Boolean = false,
    val showTypeDialog: Boolean = false,
    val showDatePicker: Boolean = false,
)

/**
 * Data class per i filtri di transazione.
 */
internal data class FilterState(
    val selectedPresetIndex: Int = 0,
    val dateRangeFrom: Long = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
)
