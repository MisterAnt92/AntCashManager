package com.antcashmanager.android.ui.mapper

import com.antcashmanager.android.R
import com.antcashmanager.android.util.DatePatterns
import com.antcashmanager.android.util.PROTECTED_INCOME_CATEGORY
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionUiTest {
    private val base =
        Transaction(
            id = 7,
            title = "Coffee",
            amount = -3.5,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = 0L,
            payee = "Bar",
        )

    @Test
    fun toUi_shouldDeriveInitialTagsAndSubtitle_whenFieldsPresent() {
        val ui = base.copy(tags = " a, b ,, c ").toUi(DatePatterns.ISO_DATE)

        assertEquals("F", ui.initial)
        assertEquals(listOf("a", "b", "c"), ui.tagList)
        assertEquals(listOf(ui.formattedDate, "Bar"), ui.subtitleParts)
        assertFalse(ui.isIncome)
        assertFalse(ui.hasNote)
        assertNull(ui.recurrenceLabelRes)
    }

    @Test
    fun toUi_shouldFlagProtectedSalary_whenIncomeInProtectedCategory() {
        val ui =
            base.copy(type = TransactionType.INCOME, category = PROTECTED_INCOME_CATEGORY).toUi(DatePatterns.ISO_DATE)

        assertTrue(ui.isProtectedSalary)
        assertTrue(ui.isIncome)
    }

    @Test
    fun toUi_shouldResolveRecurrenceLabel_whenRecurring() {
        val ui = base.copy(isRecurring = true, recurrenceInterval = "monthly").toUi(DatePatterns.ISO_DATE)

        assertEquals(R.string.transactions_interval_monthly, ui.recurrenceLabelRes)
    }

    @Test
    fun toUi_shouldTreatNullStringNoteAsMissing_whenNoteIsNullLiteral() {
        assertFalse(base.copy(notes = "null").toUi(DatePatterns.ISO_DATE).hasNote)
        assertTrue(base.copy(notes = "ok").toUi(DatePatterns.ISO_DATE).hasNote)
    }

    @Test
    fun toTagString_shouldRoundTrip_whenParsedBack() {
        val tags = listOf("x", "y")
        assertEquals(tags, tags.toTagString().toTagList())
    }
}
