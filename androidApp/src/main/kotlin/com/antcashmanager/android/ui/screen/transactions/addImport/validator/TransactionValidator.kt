package com.antcashmanager.android.ui.screen.transactions.addImport.validator

import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionState
import com.antcashmanager.domain.model.Category

/**
 * Centralizza tutta la logica di validazione per le transazioni.
 *
 * Responsabilità:
 * - Validare i singoli campi (titolo, importo, category, ricorrenza, etc)
 * - Validare lo stato completo del form
 * - Fornire messaggi di errore specifici
 *
 * Pattern: Service pattern - metodi statici per validazione pura
 */
object TransactionValidator {

    /**
     * Valida l'intero form di aggiunta/modifica transazione.
     *
     * Ritorna `true` se il form è valido e pronto per il submit, `false` altrimenti.
     */
    fun isFormValid(state: AddTransactionState): Boolean {
        // Campo titolo: obbligatorio e non vuoto
        if (state.title.isBlank()) return false

        // Categoria: obbligatoria
        if (state.selectedCategory == null) return false

        // Tipo transazione: obbligatorio
        if (state.selectedType == null) return false

        // Ricorrenza: se abilitata, richiede intervallo
        if (state.isRecurring && state.recurrenceInterval.isBlank()) return false

        // Importo: dipende dal tipo di pagamento
        if (state.isMealVouchersPayment) {
            // Per MEAL_VOUCHERS: obbligatorio numero voucher positivo
            val voucherCount = state.mealVoucherCount.toIntOrNull() ?: return false
            return voucherCount > 0
        } else {
            // Per altri pagamenti: obbligatorio importo positivo
            val amount = state.amount.toDoubleOrNull() ?: return false
            return amount > 0
        }
    }

    /**
     * Valida il titolo della transazione.
     */
    fun isValidTitle(title: String): Boolean {
        return title.isNotBlank()
    }

    /**
     * Valida l'importo della transazione.
     *
     * @param amount Stringa di importo con punto decimale
     * @return true se l'importo è valido (numero positivo)
     */
    fun isValidAmount(amount: String): Boolean {
        val parsedAmount = amount.toDoubleOrNull() ?: return false
        return parsedAmount > 0
    }

    /**
     * Valida il numero di buoni pasto.
     *
     * @param count Stringa con il numero di voucher
     * @return true se il count è un numero intero positivo
     */
    fun isValidMealVoucherCount(count: String): Boolean {
        val parsedCount = count.toIntOrNull() ?: return false
        return parsedCount > 0
    }

    /**
     * Valida l'impostazione di ricorrenza.
     *
     * @param isRecurring Se la transazione è ricorrente
     * @param recurrenceInterval Intervallo di ricorrenza (es. "1", "DAILY", "MONTHLY")
     * @return true se la configurazione è valida
     */
    fun isValidRecurrence(isRecurring: Boolean, recurrenceInterval: String): Boolean {
        if (!isRecurring) return true // Se non ricorrente, valido
        return recurrenceInterval.isNotBlank()
    }

    /**
     * Valida che la categoria esista ancora (non sia stata cancellata).
     *
     * @param selectedCategory Categoria selezionata nello stato
     * @param availableCategories Lista di categorie disponibili dal repository
     * @return true se la categoria è ancora disponibile
     */
    fun isCategoryStillAvailable(
        selectedCategory: Category?,
        availableCategories: List<Category>,
    ): Boolean {
        if (selectedCategory == null) return false
        return availableCategories.any { it.id == selectedCategory.id }
    }

    /**
     * Normalizza l'importo inserito dall'utente.
     *
     * Conversioni:
     * - Virgola (locale italiano) → Punto (formato interno)
     * - Multipli punti → Mantiene solo il primo
     *
     * @param input Testo inserito dall'utente
     * @return Importo normalizzato
     */
    fun normalizeAmount(input: String): String {
        // Filtra: mantiene solo numeri e separatori decimali
        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }

        // Normalizza: virgola → punto
        val normalized = filtered.replace(',', '.')

        // Previeni multipli punti
        val dotCount = normalized.count { it == '.' }
        return if (dotCount <= 1) {
            normalized
        } else {
            // Mantieni solo il primo punto
            val firstDotIndex = normalized.indexOf('.')
            normalized.substring(0, firstDotIndex + 1) +
                    normalized.substring(firstDotIndex + 1).replace(".", "")
        }
    }

    /**
     * Valida il numero di voucher inserito dall'utente.
     *
     * @param input Testo inserito dall'utente
     * @return Numero di voucher normalizzato (vuoto se invalido)
     */
    fun normalizeMealVoucherCount(input: String): String {
        // Filtra: mantiene solo numeri
        return input.filter { it.isDigit() }
    }

    /**
     * Valida la differenza pagata per i buoni pasto.
     *
     * @param difference Stringa con l'importo aggiuntivo
     * @return true se la differenza è valida (>= 0.00, massimo 2 decimali)
     */
    fun isValidMealVoucherDifference(difference: String): Boolean {
        if (difference.isBlank()) return true // Opzionale, default "0"

        val parsedDifference = difference.toDoubleOrNull() ?: return false

        // Non può essere negativo
        if (parsedDifference < 0.0) return false

        // Massimo 2 decimali
        val decimalPart = difference.substringAfter(".")
        return decimalPart.length <= 2
    }

    /**
     * Normalizza la differenza pagata inserita dall'utente.
     *
     * Conversioni:
     * - Virgola (locale italiano) → Punto (formato interno)
     * - Multipli punti → Mantiene solo il primo
     *
     * @param input Testo inserito dall'utente
     * @return Importo della differenza normalizzato
     */
    fun normalizeMealVoucherDifference(input: String): String {
        // Filtra: mantiene solo numeri e separatori decimali
        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }

        // Normalizza: virgola → punto
        val normalized = filtered.replace(',', '.')

        // Previeni multipli punti
        val dotCount = normalized.count { it == '.' }
        return if (dotCount <= 1) {
            normalized
        } else {
            // Mantieni solo il primo punto
            val firstDotIndex = normalized.indexOf('.')
            normalized.substring(0, firstDotIndex + 1) +
                    normalized.substring(firstDotIndex + 1).replace(".", "")
        }
    }
}
