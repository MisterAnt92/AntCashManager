package com.antcashmanager.android.util

import androidx.compose.runtime.compositionLocalOf
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import java.util.Locale
import kotlin.math.abs

/** CompositionLocal that provides the current [CurrencyFormat] to the entire composable tree. */
val LocalCurrencyFormat = compositionLocalOf { CurrencyFormat.DEFAULT }

/** CompositionLocal that indicates whether monetary amounts should be masked with asterisks. */
val LocalAmountsMasked = compositionLocalOf { false }

/**
 * Replaces every digit in [text] with an asterisk, preserving currency symbol, sign and
 * separators. Example: "€1.234,56" -> "€*.***,**".
 */
fun maskDigits(text: String): String = text.map { if (it.isDigit()) '*' else it }.joinToString("")

const val PROTECTED_INCOME_CATEGORY = "Stipendio"

/**
 * Lo stipendio resta sempre mascherato anche dove il resto degli importi è mostrato in chiaro,
 * dato che è tipicamente la cifra più sensibile da non far vedere a chi guarda lo schermo.
 */
fun isProtectedSalaryTransaction(transaction: Transaction): Boolean =
    transaction.type == TransactionType.INCOME && transaction.category == PROTECTED_INCOME_CATEGORY

/** Variante di [isProtectedSalaryTransaction] per gli schermi dove non è ancora disponibile una [Transaction] completa. */
fun isProtectedSalaryCategory(categoryName: String?, type: TransactionType?): Boolean =
    type == TransactionType.INCOME && categoryName == PROTECTED_INCOME_CATEGORY

/**
 * Formats [amount] as an absolute monetary value using [format] preferences.
 * Example: 1234567.89, format(symbol="€", digits=2, dec=",", thou="")  -> "€1234567,89"
 */
fun formatAmount(amount: Double, format: CurrencyFormat): String {
    val absAmount = abs(amount)
    // Use Locale.US to ensure "." is always used as the internal decimal separator,
    // regardless of the device locale (e.g. Italian/French devices use "," which breaks parsing).
    val rawFormatted = String.format(Locale.US, "%.${format.decimalDigits}f", absAmount)

    val dotIndex = rawFormatted.indexOf('.')
    val intPart = if (dotIndex >= 0) rawFormatted.substring(0, dotIndex) else rawFormatted
    val decPart =
        if (dotIndex >= 0 && format.decimalDigits > 0) rawFormatted.substring(dotIndex + 1) else ""

    val intWithSeparator = if (format.thousandsSeparator.isNotEmpty() && intPart.length > 3) {
        // If thousands separator is the same as decimal separator it would create ambiguity
        // (e.g. if both are ","), so disable thousands separator in that case.
        val thousandsSepEffective = if (format.thousandsSeparator == format.decimalSeparator) {
            ""
        } else {
            format.thousandsSeparator
        }

        if (thousandsSepEffective.isNotEmpty()) {
            intPart.reversed().chunked(3).joinToString(thousandsSepEffective).reversed()
        } else {
            intPart
        }
    } else {
        intPart
    }

    return buildString {
        append(format.currencySymbol)

        val hasDecimals = format.decimalDigits > 0 && decPart.isNotEmpty()
        val hasThousandsSep = format.thousandsSeparator.isNotEmpty()
        val endsWithThousandsSep = intWithSeparator.endsWith(format.thousandsSeparator)

        val finalIntPart = if (hasDecimals && hasThousandsSep && endsWithThousandsSep) {
            intWithSeparator.removeSuffix(format.thousandsSeparator)
        } else {
            intWithSeparator
        }

        append(finalIntPart)

        if (hasDecimals) {
            append(format.decimalSeparator)
            append(decPart)
        }
    }
}

/**
 * Formats [amount] with a sign prefix depending on [isIncome].
 * Example: +€1.234,50  or  -€85,00
 */
fun formatAmountWithSign(amount: Double, format: CurrencyFormat, isIncome: Boolean): String {
    val sign = if (isIncome) "+" else "-"
    return "$sign${formatAmount(amount, format)}"
}

/**
 * Formats [amount] with negative support.
 * Negative amounts show the minus sign before the currency symbol.
 * Example: 1234.56 -> "€1.234,56", -1234.56 -> "-€1.234,56"
 */
fun formatAmountWithNegative(amount: Double, format: CurrencyFormat): String {
    return if (amount < 0) {
        "-${formatAmount(amount, format)}"
    } else {
        formatAmount(amount, format)
    }
}

/**
 * Formats [amount] for transaction display showing +/- based on actual amount sign.
 * Example: 1234.56 -> "+€1.234,56", -1234.56 -> "-€1.234,56"
 */
fun formatTransactionAmount(amount: Double, format: CurrencyFormat): String {
    val sign = if (amount < 0) "-" else "+"
    return "$sign${formatAmount(amount, format)}"
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

