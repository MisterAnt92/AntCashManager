package com.antcashmanager.android.util

import com.antcashmanager.domain.model.CurrencyFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormatterExtraTest {

    @Test
    fun `space thousands separator formatting`() {
        val fmt = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 2,
            decimalSeparator = ",",
            thousandsSeparator = " "
        )
        val formatted = formatAmount(1234567.89, fmt)
        assertEquals("€1 234 567,89", formatted)
    }

    @Test
    fun `swapped separators dot decimal and comma thousands`() {
        val fmt = CurrencyFormat(
            currencySymbol = "$",
            decimalDigits = 2,
            decimalSeparator = ".",
            thousandsSeparator = ","
        )
        val formatted = formatAmount(1234.56, fmt)
        assertEquals("$1,234.56", formatted)
    }

    @Test
    fun `formatAmountWithSign positive and negative`() {
        val fmt = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 2,
            decimalSeparator = ",",
            thousandsSeparator = "."
        )
        val pos = formatAmountWithSign(1234.56, fmt, isIncome = true)
        val neg = formatAmountWithSign(1234.56, fmt, isIncome = false)
        assertEquals("+€1.234,56", pos)
        assertEquals("-€1.234,56", neg)
    }

    @Test
    fun `formatTransactionAmount uses sign from value`() {
        val fmt = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 2,
            decimalSeparator = ",",
            thousandsSeparator = "."
        )
        assertEquals("+€1.234,56", formatTransactionAmount(1234.56, fmt))
        assertEquals("-€1.234,56", formatTransactionAmount(-1234.56, fmt))
    }

    @Test
    fun `isValidNote edge cases`() {
        assertFalse((null as String?).isValidNote())
        assertFalse("".isValidNote())
        assertFalse("   ".isValidNote())
        assertFalse("null".isValidNote())
        assertFalse("Null".isValidNote())
        assertTrue("Una nota valida".isValidNote())
    }

    @Test
    fun `thousands disabled when equal to decimal separator`() {
        val fmt = CurrencyFormat(
            currencySymbol = "€",
            decimalDigits = 2,
            decimalSeparator = ",",
            thousandsSeparator = ","
        )
        val formatted = formatAmount(1234.56, fmt)
        // thousands separator should be disabled when equal to decimal separator
        assertEquals("€1234,56", formatted)
    }

}

