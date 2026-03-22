package com.antcashmanager.android.util

import androidx.compose.runtime.compositionLocalOf
import com.antcashmanager.domain.model.CurrencyFormat
import kotlin.math.abs

/** CompositionLocal that provides the current [CurrencyFormat] to the entire composable tree. */
val LocalCurrencyFormat = compositionLocalOf { CurrencyFormat.DEFAULT }

/**
 * Formats [amount] as an absolute monetary value using [format] preferences.
 * Example: 1234567.89, format(symbol="€", digits=2, dec=",", thou=".")  -> "€1.234.567,89"
 */
fun formatAmount(amount: Double, format: CurrencyFormat): String {
    val absAmount = abs(amount)
    val rawFormatted = "%.${format.decimalDigits}f".format(absAmount)

    val dotIndex = rawFormatted.indexOf('.')
    val intPart = if (dotIndex >= 0) rawFormatted.substring(0, dotIndex) else rawFormatted
    val decPart = if (dotIndex >= 0 && format.decimalDigits > 0) rawFormatted.substring(dotIndex + 1) else ""

    val intWithSeparator = if (format.thousandsSeparator.isNotEmpty() && intPart.length > 3) {
        intPart.reversed().chunked(3).joinToString(format.thousandsSeparator).reversed()
    } else {
        intPart
    }

    return buildString {
        append(format.currencySymbol)
        append(intWithSeparator)
        if (format.decimalDigits > 0 && decPart.isNotEmpty()) {
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

