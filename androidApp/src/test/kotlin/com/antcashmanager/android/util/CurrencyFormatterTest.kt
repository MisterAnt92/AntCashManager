package com.antcashmanager.android.util

import com.antcashmanager.domain.model.CurrencyFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun formatAmount_shouldDisableThousandsSeparator_whenSameAsDecimalSeparator() {
        val format = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 2,
            decimalSeparator = ",",
            thousandsSeparator = ",",
        )

        val formatted = formatAmount(1234.56, format)
        // thousands separator should be disabled because it equals decimal separator
        assertEquals("€1234,56", formatted)
    }

    @Test
    fun formatAmount_shouldApplyThousandsSeparator_whenDifferentFromDecimalSeparator() {
        val format = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 2,
            decimalSeparator = ",",
            thousandsSeparator = ".",
        )

        val formatted = formatAmount(1234.56, format)
        assertEquals("€1.234,56", formatted)
    }

    @Test
    fun formatAmount_shouldNotApplyThousandsSeparator_whenValueIsUnderOneThousand() {
        val format = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 2,
            decimalSeparator = ",",
            thousandsSeparator = ".",
        )

        val formatted = formatAmount(999.99, format)
        assertEquals("€999,99", formatted)
    }

    @Test
    fun formatAmount_shouldReturnRoundedIntegerAndKeepThousandsSeparator_whenDecimalDigitsAreZero() {
        val format = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 0,
            decimalSeparator = ",",
            thousandsSeparator = ".",
        )

        val formatted = formatAmount(1234.56, format)
        // with 0 decimal digits the value is rounded and still grouped by thousands
        assertEquals("€1.235", formatted)
    }

    @Test
    fun formatAmountWithNegative_shouldPrefixMinusBeforeCurrency_whenAmountIsNegative() {
        val format = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 2,
            decimalSeparator = ",",
            thousandsSeparator = ".",
        )

        val formatted = formatAmountWithNegative(-1234.56, format)
        assertEquals("-€1.234,56", formatted)
    }
}

