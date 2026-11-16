package com.antcashmanager.android.ui.home

import com.antcashmanager.android.ui.screen.home.HomeState
import org.junit.Assert.assertEquals
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
}
