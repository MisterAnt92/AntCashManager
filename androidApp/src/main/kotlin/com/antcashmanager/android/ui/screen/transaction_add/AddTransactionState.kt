package com.antcashmanager.android.ui.screen.transaction_add

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.TransactionType

// ══════════════════════════════════════════════════════════════════════════════
// STATE
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Enum per rappresentare i vari step del wizard di aggiunta transazioni.
 */
enum class AddTransactionStep {
    CATEGORY_SELECTION,
    TYPE_SELECTION,
    DETAILS,
    CONFIRMATION,
}

/**
 * Data class che rappresenta lo stato della schermata di aggiunta transazione.
 */
data class AddTransactionState(
    // ── Navigazione ──
    val currentStep: AddTransactionStep = AddTransactionStep.CATEGORY_SELECTION,
    
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
)

/**
 * Data class per i filtri di transazione.
 */
internal data class FilterState(
    val selectedPresetIndex: Int = 0,
    val dateRangeFrom: Long = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
)

