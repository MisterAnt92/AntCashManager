package com.antcashmanager.android.util

import com.antcashmanager.domain.model.CurrencyFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun `thousands disabled when same as decimal`() {
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
    fun `thousands enabled when different separator`() {
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
    fun `no thousands for values under thousand`() {
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
    fun `no decimal digits outputs rounded integer and no separator`() {
        val format = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 0,
            decimalSeparator = ",",
            thousandsSeparator = ".",
        )

        val formatted = formatAmount(1234.56, format)
        // with 0 decimal digits the formatter uses %.0f so it will round
        assertEquals("€1235", formatted)
    }

    @Test
    fun `negative value shows minus before currency`() {
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

