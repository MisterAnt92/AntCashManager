package com.antcashmanager.android.ui.home

import com.antcashmanager.android.ui.screen.charts.TrendDirection
import com.antcashmanager.android.ui.screen.home.HomeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HomeStateTest {
    private fun assertDateMatchesPreset(index: Int) {
        val expectedFrom =
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                when (index) {
                    0 -> add(Calendar.DAY_OF_YEAR, -1)
                    1 -> add(Calendar.DAY_OF_YEAR, -7)
                    2 -> add(Calendar.MONTH, -1)
                    3 -> add(Calendar.YEAR, -1)
                    4 -> add(Calendar.YEAR, -2)
                    5 -> add(Calendar.YEAR, -3)
                    6 -> add(Calendar.YEAR, -5)
                    7 -> add(Calendar.YEAR, -6)
                    8 -> set(2000, Calendar.JANUARY, 1)
                    else -> add(Calendar.DAY_OF_YEAR, -7)
                }
            }.timeInMillis
        assertTrue(Math.abs(HomeState.getDateFromForPreset(index) - expectedFrom) < 5_000)
    }

    @Test
    fun getDateFromForPreset_shouldReturnOneDayAgo_whenIndexIsZero() {
        assertDateMatchesPreset(0)
    }

    @Test
    fun getDateFromForPreset_shouldReturnOneYearAgo_whenIndexIsThree() {
        assertDateMatchesPreset(3)
    }

    @Test
    fun getDateFromForPreset_shouldReturnTwoYearsAgo_whenIndexIsFour() {
        assertDateMatchesPreset(4)
    }

    @Test
    fun getDateFromForPreset_shouldReturnThreeYearsAgo_whenIndexIsFive() {
        assertDateMatchesPreset(5)
    }

    @Test
    fun getDateFromForPreset_shouldReturnFiveYearsAgo_whenIndexIsSix() {
        assertDateMatchesPreset(6)
    }

    @Test
    fun getDateFromForPreset_shouldReturnSixYearsAgo_whenIndexIsSeven() {
        assertDateMatchesPreset(7)
    }

    @Test
    fun getDateFromForPreset_shouldReturnAllTimeAgo_whenIndexIsEight() {
        assertDateMatchesPreset(8)
    }

    @Test
    fun getDateFromForPreset_shouldFallBackToOneWeekAgo_whenIndexIsUnknown() {
        assertDateMatchesPreset(99)
    }

    @Test
    fun presets_shouldIncludeMultiYearRangesAndAllTime() {
        assertEquals(9, HomeState.PRESETS.size)
    }

    // --- Period comparison ---

    @Test
    fun incomeDeltaPercent_shouldReturnNull_whenPreviousPeriodIncomeIsZero() {
        val state = HomeState(totalIncome = 100.0, previousPeriodIncome = 0.0)
        assertNull(state.incomeDeltaPercent)
    }

    @Test
    fun incomeDeltaPercent_shouldReturnCorrectDelta_whenPreviousIsNonZero() {
        val state = HomeState(totalIncome = 110.0, previousPeriodIncome = 100.0)
        assertEquals(10.0, state.incomeDeltaPercent!!, 0.001)
    }

    @Test
    fun expenseDeltaPercent_shouldReturnNull_whenPreviousPeriodExpenseIsZero() {
        val state = HomeState(totalExpense = 50.0, previousPeriodExpense = 0.0)
        assertNull(state.expenseDeltaPercent)
    }

    @Test
    fun expenseDeltaPercent_shouldReturnCorrectDelta_whenPreviousIsNonZero() {
        val state = HomeState(totalExpense = 80.0, previousPeriodExpense = 100.0)
        assertEquals(-20.0, state.expenseDeltaPercent!!, 0.001)
    }

    @Test
    fun incomeTrend_shouldBeUp_whenDeltaAboveThreshold() {
        val state = HomeState(totalIncome = 110.0, previousPeriodIncome = 100.0) // +10%
        assertEquals(TrendDirection.UP, state.incomeTrend)
    }

    @Test
    fun incomeTrend_shouldBeDown_whenDeltaBelowNegativeThreshold() {
        val state = HomeState(totalIncome = 85.0, previousPeriodIncome = 100.0) // -15%
        assertEquals(TrendDirection.DOWN, state.incomeTrend)
    }

    @Test
    fun incomeTrend_shouldBeNeutral_whenDeltaWithinThreshold() {
        val state = HomeState(totalIncome = 102.0, previousPeriodIncome = 100.0) // +2%
        assertEquals(TrendDirection.NEUTRAL, state.incomeTrend)
    }

    @Test
    fun incomeTrend_shouldBeNeutral_whenPreviousIsZero() {
        val state = HomeState(totalIncome = 100.0, previousPeriodIncome = 0.0)
        assertEquals(TrendDirection.NEUTRAL, state.incomeTrend)
    }
}
