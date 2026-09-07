package com.antcashmanager.android.ui.screen.transactions.addImport.validator

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
