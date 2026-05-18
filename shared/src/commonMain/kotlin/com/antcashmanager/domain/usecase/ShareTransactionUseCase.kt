package com.antcashmanager.domain.usecase

import com.antcashmanager.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Use case per la formattazione dei dati di una transazione per la condivisione
 * Implementa la business logic di preparazione dei dati
 */
class ShareTransactionUseCase {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    /**
     * Formatta i dati della transazione in una stringa leggibile per la condivisione
     * @param transaction La transazione da condividere
     * @param isIncome Se la transazione è un'entrata o un'uscita
     * @return Stringa formattata pronta per la condivisione
     */
    fun formatTransactionForShare(
        transaction: Transaction,
        isIncome: Boolean,
    ): String {
        val typeString = if (isIncome) "Income" else "Expense"
        val recurrenceInfo =
            if (transaction.isRecurring && transaction.recurrenceInterval.isNotBlank()) {
                "\nRecurrence: ${transaction.recurrenceInterval}"
            } else {
                ""
            }
        val notesInfo = if (transaction.notes.isValidNote()) {
            "\nNotes: ${transaction.notes}"
        } else {
            ""
        }
        val payeeInfo = if (transaction.payee.isNotBlank()) {
            "\nPayee: ${transaction.payee}"
        } else {
            ""
        }
        val locationInfo = if (transaction.location.isNotBlank()) {
            "\nLocation: ${transaction.location}"
        } else {
            ""
        }
        val tagsInfo = if (transaction.tags.isNotBlank()) {
            "\nTags: ${transaction.tags}"
        } else {
            ""
        }
        return """
            Transaction Details
            Title: ${transaction.title}
            Category: ${transaction.category}
            Type: $typeString
            Amount: ${if (isIncome) "+" else "-"}${'$'}${String.format("%.2f", transaction.amount)}
            Date: ${dateFormat.format(Date(transaction.timestamp))}$notesInfo$payeeInfo$locationInfo$recurrenceInfo$tagsInfo
            Shared via AntCashManager
        """.trimIndent()
    }
}

/**
 * Checks if a note string is valid for display.
 * Returns false if the note is null, blank, or contains the string "null".
 * Returns true if the note should be displayed.
 */
fun String?.isValidNote(): Boolean {
    // Null or blank strings are not valid
    if (this.isNullOrBlank()) return false
    // Strings containing "null" are not valid
    if (this.equals("null", ignoreCase = true)) return false
    return true
}

