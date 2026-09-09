package com.antcashmanager.android.ui.screen.common

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
     * Compute the start of the date range for a given preset index.
     */
    fun dateFromForPreset(index: Int, now: Long = System.currentTimeMillis()): Long =
        when (index) {
            0 -> now - ONE_DAY_MS
            1 -> now - ONE_WEEK_MS
            2 -> now - THIRTY_DAYS_MS
            3 -> now - ONE_YEAR_MS
            4 -> now - TWO_YEARS_MS
            5 -> now - THREE_YEARS_MS
            6 -> now - FIVE_YEARS_MS
            7 -> now - SIX_YEARS_MS
            8 -> now - ALL_TIME_MS
            else -> now - ONE_WEEK_MS
        }
}
