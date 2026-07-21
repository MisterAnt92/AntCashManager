package com.antcashmanager.android.util

import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun maskDigits_shouldReplaceOnlyDigits_whenAmountIsPositiveWithThousandsSeparator() {
        val masked = maskDigits("€1.234,56")
        assertEquals("€*.***,**", masked)
    }

    @Test
    fun maskDigits_shouldPreserveMinusSign_whenAmountIsNegative() {
        val masked = maskDigits("-€1.234,56")
        assertEquals("-€*.***,**", masked)
    }

    @Test
    fun maskDigits_shouldReturnUnchangedString_whenNoDigitsPresent() {
        val masked = maskDigits("€-,.")
        assertEquals("€-,.", masked)
    }

    @Test
    fun maskDigits_shouldMaskEveryDigit_whenCurrencySymbolIsMultiChar() {
        val masked = maskDigits("USD 12,345")
        assertEquals("USD **,***", masked)
    }

    @Test
    fun isProtectedSalaryTransaction_shouldReturnTrue_whenIncomeCategoryIsStipendio() {
        val transaction = Transaction(title = "Paga", amount = 1500.0, category = "Stipendio", type = TransactionType.INCOME)
        assertTrue(isProtectedSalaryTransaction(transaction))
    }

    @Test
    fun isProtectedSalaryTransaction_shouldReturnFalse_whenSameCategoryNameButExpense() {
        val transaction = Transaction(title = "Rimborso stipendio", amount = 100.0, category = "Stipendio", type = TransactionType.EXPENSE)
        assertFalse(isProtectedSalaryTransaction(transaction))
    }

    @Test
    fun isProtectedSalaryTransaction_shouldReturnFalse_whenIncomeButDifferentCategory() {
        val transaction = Transaction(title = "Bonus", amount = 200.0, category = "Freelance", type = TransactionType.INCOME)
        assertFalse(isProtectedSalaryTransaction(transaction))
    }

    @Test
    fun isProtectedSalaryCategory_shouldReturnTrue_whenIncomeCategoryIsStipendio() {
        assertTrue(isProtectedSalaryCategory("Stipendio", TransactionType.INCOME))
    }

    @Test
    fun isProtectedSalaryCategory_shouldReturnFalse_whenCategoryIsNull() {
        assertFalse(isProtectedSalaryCategory(null, TransactionType.INCOME))
    }

    @Test
    fun isProtectedSalaryCategory_shouldReturnFalse_whenTypeIsNull() {
        assertFalse(isProtectedSalaryCategory("Stipendio", null))
    }

    @Test
    fun isProtectedSalaryCategory_shouldReturnFalse_whenCategoryDiffersAndTypeIsIncome() {
        assertFalse(isProtectedSalaryCategory("Freelance", TransactionType.INCOME))
    }
}

