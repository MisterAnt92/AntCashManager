package com.antcashmanager.android.ui.screen.transactions.addImport

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType

/**
 * Flusso semplificato:
 * - Nuova transazione: Categoria -> Dettagli (salvataggio diretto)
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
    // Navigazione
    val currentStep: AddTransactionStep = AddTransactionStep.CATEGORY_SELECTION,
    val isModifying: Boolean = false,
    val transactionId: Long? = null,

    // Dati categoria e tipo
    val selectedCategory: Category? = null,
    val selectedType: TransactionType? = null,

    // Dettagli transazione
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
    val mealVoucherCount: String = "0",
    val mealVoucherValue: Double = 5.29,

    // Dati disponibili
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTransactionSaved: Boolean = false,

    // Suggerimenti per campi
    val titleSuggestions: List<String> = emptyList(),
    val payeeSuggestions: List<String> = emptyList(),
    val notesSuggestions: List<String> = emptyList(),
    val locationSuggestions: List<String> = emptyList(),
    val tagsSuggestions: List<String> = emptyList(),

    // Dialog states
    val showCategoryDialog: Boolean = false,
    val showTypeDialog: Boolean = false,
    val showDatePicker: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showPaymentTypeDialog: Boolean = false,
) {
    val isMealVouchersPayment: Boolean
        get() = selectedPaymentType == PaymentType.MEAL_VOUCHERS

    val isFormValid: Boolean
        get() = title.isNotBlank() &&
                selectedCategory != null &&
                selectedType != null &&
                // Per buoni pasto: numero voucher obbligatorio, nessun importo manuale
                if (isMealVouchersPayment) {
                    mealVoucherCount.toIntOrNull() != null && mealVoucherCount.toIntOrNull()!! > 0
                } else {
                    // Per altre transazioni: importo totale obbligatorio
                    amount.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
                }

    /**
     * Importo da persistere sulla transazione. Con pagamento in Buoni Pasto, [amount]
     * rappresenta già il totale del pagamento (buoni + differenza), inserito
     * direttamente dall'utente: non va sommato al subtotale dei buoni.
     */
    val totalAmount: Double
        get() = amount.toDoubleOrNull() ?: 0.0

    /**
     * Differenza pagata con altri mezzi rispetto al valore coperto dai buoni pasto.
     * Puramente informativa, per aiutare l'utente a verificare l'importo inserito.
     */
    val mealVoucherDifferencePaid: Double
        get() = totalAmount - (mealVoucherCount.toIntOrNull() ?: 0) * mealVoucherValue
}

