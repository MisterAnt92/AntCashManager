package com.antcashmanager.android.ui.mapper

import androidx.annotation.StringRes
import com.antcashmanager.android.util.formatTimestamp
import com.antcashmanager.android.util.isProtectedSalaryTransaction
import com.antcashmanager.android.util.isValidNote
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType

private const val TAGS_SEPARATOR = ","

/**
 * Presentation model for a transaction row. Everything derivable from the domain object without
 * Compose (dates, tags, masking flags) is computed once here, in the ViewModel, not per recomposition.
 * Category translation still happens in the UI because it needs string resources.
 */
data class TransactionUi(
    val transaction: Transaction,
    val formattedDate: String,
    val initial: String,
    val tagList: List<String>,
    val hasNote: Boolean,
    val isProtectedSalary: Boolean,
    @StringRes val recurrenceLabelRes: Int?,
) {
    val id: Long get() = transaction.id
    val isIncome: Boolean get() = transaction.type == TransactionType.INCOME

    /** Secondary line parts after the (translated) category: date, payee, location — blanks removed. */
    val subtitleParts: List<String>
        get() = listOf(formattedDate, transaction.payee, transaction.location).filter { it.isNotBlank() }
}

fun Transaction.toUi(datePattern: String): TransactionUi =
    TransactionUi(
        transaction = this,
        formattedDate = formatTimestamp(timestamp, datePattern),
        initial = category.take(1).uppercase(),
        tagList = tags.toTagList(),
        hasNote = notes.isValidNote(),
        isProtectedSalary = isProtectedSalaryTransaction(this),
        recurrenceLabelRes = if (isRecurring) recurrenceIntervalLabelRes(recurrenceInterval) else null,
    )

fun List<Transaction>.toUi(datePattern: String): List<TransactionUi> = map { it.toUi(datePattern) }

/** Splits the persisted comma-separated tag string into trimmed, non-blank tags. */
fun String.toTagList(): List<String> = split(TAGS_SEPARATOR).map { it.trim() }.filter { it.isNotBlank() }

/** Inverse of [toTagList]. */
fun List<String>.toTagString(): String = joinToString("$TAGS_SEPARATOR ")
