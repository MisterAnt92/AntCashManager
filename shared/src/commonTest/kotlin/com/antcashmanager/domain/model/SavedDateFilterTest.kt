package com.antcashmanager.domain.model

import com.antcashmanager.domain.model.SavedDateFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for SavedDateFilter domain model.
 *
 * Tests cover:
 * - Date filter creation
 * - Date range validation
 * - Preset index handling
 * - Date range bounds
 */
class SavedDateFilterTest {

    private fun toTimestamp(year: Int, month: Int, day: Int): Long {
        // Convert date to timestamp (milliseconds since epoch)
        // Jan 1 2024 = 1704067200000L
        val days = when (year) {
            2024 -> {
                // 2024 is a leap year
                val daysInMonths = intArrayOf(0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
                var totalDays = 0
                for (i in 1 until month) {
                    totalDays += daysInMonths[i]
                }
                totalDays + day - 1 // subtract 1 because we start from day 1
            }
            else -> throw IllegalArgumentException("Only 2024 is supported in tests")
        }
        return 1704067200000L + (days * 24 * 60 * 60 * 1000L)
    }

    @Test
    fun savedDateFilter_shouldStorePresetIndex() {
        val filter = SavedDateFilter(
            presetIndex = 0,
            from = toTimestamp(2024, 1, 1),
            to = toTimestamp(2024, 12, 31)
        )

        assertEquals(0, filter.presetIndex)
    }

    @Test
    fun savedDateFilter_shouldStoreFromDate() {
        val fromDate = toTimestamp(2024, 6, 1)
        val filter = SavedDateFilter(
            presetIndex = 1,
            from = fromDate,
            to = toTimestamp(2024, 12, 31)
        )

        assertEquals(fromDate, filter.from)
    }

    @Test
    fun savedDateFilter_shouldStoreToDate() {
        val toDate = toTimestamp(2024, 12, 31)
        val filter = SavedDateFilter(
            presetIndex = 1,
            from = toTimestamp(2024, 1, 1),
            to = toDate
        )

        assertEquals(toDate, filter.to)
    }

    @Test
    fun savedDateFilter_shouldAllowSameDateRange() {
        val today = toTimestamp(2024, 6, 15)
        val filter = SavedDateFilter(
            presetIndex = -1,
            from = today,
            to = today
        )

        assertEquals(today, filter.from)
        assertEquals(today, filter.to)
    }

    @Test
    fun savedDateFilter_shouldAllowMultiYearRange() {
        val filter = SavedDateFilter(
            presetIndex = -1,
            from = toTimestamp(2020, 1, 1),
            to = toTimestamp(2024, 12, 31)
        )

        assertEquals(toTimestamp(2020, 1, 1), filter.from)
        assertEquals(toTimestamp(2024, 12, 31), filter.to)
    }

    @Test
    fun savedDateFilter_shouldHandleNegativePresetIndex() {
        // -1 typically means custom date range
        val filter = SavedDateFilter(
            presetIndex = -1,
            from = toTimestamp(2024, 1, 1),
            to = toTimestamp(2024, 12, 31)
        )

        assertEquals(-1, filter.presetIndex)
    }

    @Test
    fun savedDateFilter_shouldHandlePresetIndices() {
        // Test various preset indices (0 = today, 1 = week, 2 = month, etc.)
        for (index in 0..4) {
            val filter = SavedDateFilter(
                presetIndex = index,
                from = toTimestamp(2024, 1, 1),
                to = toTimestamp(2024, 12, 31)
            )

            assertEquals(index, filter.presetIndex)
        }
    }

    @Test
    fun savedDateFilter_shouldHandleFirstDayOfMonth() {
        val firstDay = toTimestamp(2024, 6, 1)
        val lastDay = toTimestamp(2024, 6, 30)

        val filter = SavedDateFilter(
            presetIndex = -1,
            from = firstDay,
            to = lastDay
        )

        assertEquals(firstDay, filter.from)
        assertEquals(lastDay, filter.to)
    }

    @Test
    fun savedDateFilter_shouldHandleLeapYearDate() {
        val leapDay = toTimestamp(2024, 2, 29)
        val filter = SavedDateFilter(
            presetIndex = -1,
            from = leapDay,
            to = leapDay
        )

        assertEquals(leapDay, filter.from)
    }

    @Test
    fun savedDateFilter_equalityTest() {
        val filter1 = SavedDateFilter(
            presetIndex = 1,
            from = toTimestamp(2024, 1, 1),
            to = toTimestamp(2024, 12, 31)
        )

        val filter2 = SavedDateFilter(
            presetIndex = 1,
            from = toTimestamp(2024, 1, 1),
            to = toTimestamp(2024, 12, 31)
        )

        assertEquals(filter1, filter2)
    }

    @Test
    fun savedDateFilter_inequalityByIndexTest() {
        val filter1 = SavedDateFilter(
            presetIndex = 0,
            from = toTimestamp(2024, 1, 1),
            to = toTimestamp(2024, 12, 31)
        )

        val filter2 = SavedDateFilter(
            presetIndex = 1,
            from = toTimestamp(2024, 1, 1),
            to = toTimestamp(2024, 12, 31)
        )

        assertFalse(filter1 == filter2)
    }

    @Test
    fun savedDateFilter_inequalityByDateTest() {
        val filter1 = SavedDateFilter(
            presetIndex = 1,
            from = toTimestamp(2024, 1, 1),
            to = toTimestamp(2024, 12, 31)
        )

        val filter2 = SavedDateFilter(
            presetIndex = 1,
            from = toTimestamp(2024, 6, 1),
            to = toTimestamp(2024, 12, 31)
        )

        assertFalse(filter1 == filter2)
    }
}
