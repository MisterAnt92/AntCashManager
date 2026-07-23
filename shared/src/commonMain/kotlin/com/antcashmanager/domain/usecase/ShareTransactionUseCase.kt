package com.antcashmanager.domain.usecase

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case per la formattazione dei dati di una transazione per la condivisione.
 * Implementa la business logic di preparazione dei dati estendendo [UseCase].
 */
class ShareTransactionUseCase : UseCase<ShareTransactionUseCase.Params, String>() {

    data class Params(val transaction: Transaction)

    /**
     * Formatta i dati della transazione in una stringa leggibile per la condivisione.
     */
    override suspend fun execute(params: Params): String {
        val transaction = params.transaction
        val isIncome = transaction.type == TransactionType.INCOME
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
        val formattedDate = formatDateForShare(transaction.timestamp)
        val formattedAmount = formatAmountForShare(transaction.amount)
        return """
            Transaction Details
            Title: ${transaction.title}
            Category: ${transaction.category}
            Type: $typeString
            Amount: ${if (isIncome) "+" else "-"}${'$'}$formattedAmount
            Date: $formattedDate$notesInfo$payeeInfo$locationInfo$recurrenceInfo$tagsInfo
            Shared via AntCashManager
        """.trimIndent()
    }

    private fun formatDateForShare(timestampMillis: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestampMillis)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.month.toString().take(3)
        val year = localDateTime.year
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        return "$day $month $year $hour:$minute"
    }

    private fun formatAmountForShare(amount: Double): String {
        val cents = (amount * 100).toLong()
        val euros = cents / 100
        val centRemainder = cents % 100
        return if (centRemainder == 0L) {
            euros.toString()
        } else {
            val paddedCents = centRemainder.toString().padStart(2, '0')
            "$euros.$paddedCents"
        }
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
