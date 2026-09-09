package com.antcashmanager.android.ui.mapper

import com.antcashmanager.android.R
import com.antcashmanager.domain.model.RecurrenceInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecurrenceIntervalUiTest {
    @Test
    fun recurrenceIntervalOf_shouldParseKeysCaseInsensitively_whenKnown() {
        assertEquals(RecurrenceInterval.WEEKLY, recurrenceIntervalOf(" Weekly "))
        assertEquals(RecurrenceInterval.DAILY, recurrenceIntervalOf(RecurrenceIntervalKeys.DAILY))
    }

    @Test
    fun recurrenceIntervalOf_shouldReturnNull_whenUnknownOrBlank() {
        assertNull(recurrenceIntervalOf(""))
        assertNull(recurrenceIntervalOf("fortnightly"))
    }

    @Test
    fun recurrenceIntervalLabelRes_shouldFallbackToRecurring_whenUnknown() {
        assertEquals(R.string.transactions_recurring, recurrenceIntervalLabelRes("?"))
        assertEquals(R.string.transactions_interval_yearly, recurrenceIntervalLabelRes("yearly"))
    }

    @Test
    fun key_shouldMatchAllKeys_whenIteratingEnum() {
        assertEquals(RecurrenceIntervalKeys.ALL, RecurrenceInterval.entries.map { it.key() })
    }
}
