package com.antcashmanager.android.ui.screen.common

import android.icu.util.Calendar
import com.antcashmanager.android.R

/**
 * Shared date range preset constants and utilities for Home and Transactions screens.
 */
object DateRangePreset {
    val PRESETS =
        listOf(
            R.string.range_label_today to "today",
            R.string.range_week to "week",
            R.string.range_month to "month",
            R.string.range_year to "year",
            R.string.range_two_years to "two_years",
            R.string.range_three_years to "three_years",
            R.string.range_five_years to "five_years",
            R.string.range_six_years to "six_years",
            R.string.range_all to "all",
        )

    const val ONE_DAY_MS = 24L * 60 * 60 * 1000
    const val ONE_WEEK_MS = 7L * ONE_DAY_MS
    const val THIRTY_DAYS_MS = 30L * ONE_DAY_MS
    const val ONE_YEAR_MS = 365L * ONE_DAY_MS
    const val TWO_YEARS_MS = 2L * ONE_YEAR_MS
    const val THREE_YEARS_MS = 3L * ONE_YEAR_MS
    const val FIVE_YEARS_MS = 5L * ONE_YEAR_MS
    const val SIX_YEARS_MS = 6L * ONE_YEAR_MS
    const val ALL_TIME_MS = 50L * ONE_YEAR_MS
    const val DEFAULT_PRESET_INDEX = 1

    /**
     * Compute the start of the date range for a given preset index using calendar-aware arithmetic.
     * Month and year presets use Calendar.add() to handle varying month lengths and leap years.
     */
    fun dateFromForPreset(index: Int, now: Long = System.currentTimeMillis()): Long {
        val fromCal = Calendar.getInstance()
        fromCal.timeInMillis = now

        when (index) {
            0 -> fromCal.add(Calendar.DAY_OF_YEAR, -1) // today -> -1 day
            1 -> fromCal.add(Calendar.DAY_OF_YEAR, -7) // week -> -7 days
            2 -> fromCal.add(Calendar.MONTH, -1) // month -> -1 calendar month
            3 -> fromCal.add(Calendar.YEAR, -1) // year -> -1 calendar year
            4 -> fromCal.add(Calendar.YEAR, -2)
            5 -> fromCal.add(Calendar.YEAR, -3)
            6 -> fromCal.add(Calendar.YEAR, -5)
            7 -> fromCal.add(Calendar.YEAR, -6)
            8 -> fromCal.set(2000, 0, 1) // all time
            else -> fromCal.add(Calendar.DAY_OF_YEAR, -7)
        }

        return fromCal.timeInMillis
    }
}
