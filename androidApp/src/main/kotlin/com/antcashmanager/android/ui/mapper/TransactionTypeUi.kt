package com.antcashmanager.android.ui.mapper

import androidx.annotation.StringRes
import com.antcashmanager.android.R
import com.antcashmanager.domain.model.TransactionType

/** Single place that maps [TransactionType] to its UI representation. */
@StringRes
fun TransactionType.labelRes(): Int =
    when (this) {
        TransactionType.INCOME -> R.string.transaction_type_income
        TransactionType.EXPENSE -> R.string.transaction_type_expense
    }

fun TransactionType.emoji(): String =
    when (this) {
        TransactionType.INCOME -> "💰"
        TransactionType.EXPENSE -> "💸"
    }

/** Parses the persisted category type string ("INCOME"/"EXPENSE", any case); unknown values map to EXPENSE. */
fun transactionTypeOf(raw: String): TransactionType =
    if (raw.equals(TransactionType.INCOME.name, ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE
