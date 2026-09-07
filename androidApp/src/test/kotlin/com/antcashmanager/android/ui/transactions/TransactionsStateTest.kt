package com.antcashmanager.android.ui.transactions

import com.antcashmanager.android.ui.screen.transactions.TransactionsConstant
import com.antcashmanager.android.ui.screen.transactions.TransactionsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionsStateTest {
    private fun assertApproximatelyAgo(
        actualFrom: Long,
        expectedDurationMs: Long,
        toleranceMs: Long = 5_000,
    ) {
        val expectedFrom = System.currentTimeMillis() - expectedDurationMs
        assertTrue(Math.abs(actualFrom - expectedFrom) < toleranceMs)
    }

    @Test
    fun getDateFromForPreset_shouldReturnOneDayAgo_whenIndexIsZero() {
        assertApproximatelyAgo(
            TransactionsState.getDateFromForPreset(0),
            TransactionsConstant.ONE_DAY_MS,
        )
    }

    @Test
    fun getDateFromForPreset_shouldReturnOneYearAgo_whenIndexIsThree() {
        assertApproximatelyAgo(
            TransactionsState.getDateFromForPreset(3),
            TransactionsConstant.ONE_YEAR_MS,
        )
    }

    @Test
    fun getDateFromForPreset_shouldReturnTwoYearsAgo_whenIndexIsFour() {
        assertApproximatelyAgo(
            TransactionsState.getDateFromForPreset(4),
            TransactionsConstant.TWO_YEARS_MS,
        )
    }

    @Test
    fun getDateFromForPreset_shouldReturnThreeYearsAgo_whenIndexIsFive() {
        assertApproximatelyAgo(
            TransactionsState.getDateFromForPreset(5),
            TransactionsConstant.THREE_YEARS_MS,
        )
    }

    @Test
    fun getDateFromForPreset_shouldReturnFiveYearsAgo_whenIndexIsSix() {
        assertApproximatelyAgo(
            TransactionsState.getDateFromForPreset(6),
            TransactionsConstant.FIVE_YEARS_MS,
        )
    }

    @Test
    fun getDateFromForPreset_shouldReturnSixYearsAgo_whenIndexIsSeven() {
        assertApproximatelyAgo(
            TransactionsState.getDateFromForPreset(7),
            TransactionsConstant.SIX_YEARS_MS,
        )
    }

    @Test
    fun getDateFromForPreset_shouldReturnAllTimeAgo_whenIndexIsEight() {
        assertApproximatelyAgo(
            TransactionsState.getDateFromForPreset(8),
            TransactionsConstant.ALL_TIME_MS,
        )
    }

    @Test
    fun getDateFromForPreset_shouldFallBackToOneWeekAgo_whenIndexIsUnknown() {
        assertApproximatelyAgo(
            TransactionsState.getDateFromForPreset(99),
            TransactionsConstant.ONE_WEEK_MS,
        )
    }

    @Test
    fun presets_shouldIncludeMultiYearRangesAndAllTime() {
        assertEquals(9, TransactionsState.PRESETS.size)
    }
}
