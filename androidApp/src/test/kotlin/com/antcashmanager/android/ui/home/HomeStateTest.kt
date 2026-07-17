package com.antcashmanager.android.ui.home

import com.antcashmanager.android.ui.screen.home.HomeConstant
import com.antcashmanager.android.ui.screen.home.HomeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeStateTest {

    private fun assertApproximatelyAgo(actualFrom: Long, expectedDurationMs: Long, toleranceMs: Long = 5_000) {
        val expectedFrom = System.currentTimeMillis() - expectedDurationMs
        assertTrue(Math.abs(actualFrom - expectedFrom) < toleranceMs)
    }

    @Test
    fun getDateFromForPreset_shouldReturnOneDayAgo_whenIndexIsZero() {
        assertApproximatelyAgo(HomeState.getDateFromForPreset(0), HomeConstant.ONE_DAY_MS)
    }

    @Test
    fun getDateFromForPreset_shouldReturnOneYearAgo_whenIndexIsThree() {
        assertApproximatelyAgo(HomeState.getDateFromForPreset(3), HomeConstant.ONE_YEAR_MS)
    }

    @Test
    fun getDateFromForPreset_shouldReturnTwoYearsAgo_whenIndexIsFour() {
        assertApproximatelyAgo(HomeState.getDateFromForPreset(4), HomeConstant.TWO_YEARS_MS)
    }

    @Test
    fun getDateFromForPreset_shouldReturnThreeYearsAgo_whenIndexIsFive() {
        assertApproximatelyAgo(HomeState.getDateFromForPreset(5), HomeConstant.THREE_YEARS_MS)
    }

    @Test
    fun getDateFromForPreset_shouldReturnFiveYearsAgo_whenIndexIsSix() {
        assertApproximatelyAgo(HomeState.getDateFromForPreset(6), HomeConstant.FIVE_YEARS_MS)
    }

    @Test
    fun getDateFromForPreset_shouldReturnSixYearsAgo_whenIndexIsSeven() {
        assertApproximatelyAgo(HomeState.getDateFromForPreset(7), HomeConstant.SIX_YEARS_MS)
    }

    @Test
    fun getDateFromForPreset_shouldReturnAllTimeAgo_whenIndexIsEight() {
        assertApproximatelyAgo(HomeState.getDateFromForPreset(8), HomeConstant.ALL_TIME_MS)
    }

    @Test
    fun getDateFromForPreset_shouldFallBackToOneWeekAgo_whenIndexIsUnknown() {
        assertApproximatelyAgo(HomeState.getDateFromForPreset(99), HomeConstant.ONE_WEEK_MS)
    }

    @Test
    fun presets_shouldIncludeMultiYearRangesAndAllTime() {
        assertEquals(9, HomeState.PRESETS.size)
    }
}
