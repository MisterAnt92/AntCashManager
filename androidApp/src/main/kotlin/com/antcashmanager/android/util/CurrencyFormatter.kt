package com.antcashmanager.android.util

import androidx.compose.runtime.compositionLocalOf
import com.antcashmanager.domain.model.CurrencyFormat
import kotlin.math.abs

/** CompositionLocal that provides the current [CurrencyFormat] to the entire composable tree. */
val LocalCurrencyFormat = compositionLocalOf { CurrencyFormat.DEFAULT }

/**
 * Formats [amount] as an absolute monetary value using [format] preferences.
 * Example: 1234567.89, format(symbol="€", digits=2, dec=",", thou="")  -> "€1234567,89"
 */
fun formatAmount(amount: Double, format: CurrencyFormat): String {
    val absAmount = abs(amount)
    val rawFormatted = "%.${format.decimalDigits}f".format(absAmount)

    val dotIndex = rawFormatted.indexOf('.')
    val intPart = if (dotIndex >= 0) rawFormatted.substring(0, dotIndex) else rawFormatted
    val decPart = if (dotIndex >= 0 && format.decimalDigits > 0) rawFormatted.substring(dotIndex + 1) else ""

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
        append(intWithSeparator)
        if (format.decimalDigits > 0 && decPart.isNotEmpty()) {
            // Ensure we don't end up with a trailing thousands separator right before
            // the decimal separator (some edge cases / custom separators can cause this).
            if (format.thousandsSeparator.isNotEmpty() && intWithSeparator.endsWith(format.thousandsSeparator)) {
                // remove trailing thousands separator to avoid e.g. "1.000. ,50" or ambiguity
                val trimmed = intWithSeparator.removeSuffix(format.thousandsSeparator)
                // replace previous appended intWithSeparator (simple approach: rebuild string)
                setLength(0)
                append(format.currencySymbol)
                append(trimmed)
            }

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
