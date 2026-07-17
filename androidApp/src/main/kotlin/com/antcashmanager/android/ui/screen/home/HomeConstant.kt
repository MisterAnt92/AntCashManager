package com.antcashmanager.android.ui.screen.home

import com.antcashmanager.android.R

/**
 * Shared constants for Home feature.
 */
object HomeConstant {
    val PRESETS = listOf(
        R.string.range_label_today to "today",
        R.string.range_week to "week",
        R.string.range_month to "month",
        R.string.range_year to "year",
        R.string.range_two_years to "two_years",
        R.string.range_three_years to "three_years",
        R.string.range_five_years to "five_years",
        R.string.range_six_years to "six_years",
    )

    const val ONE_DAY_MS = 24L * 60 * 60 * 1000
    const val ONE_WEEK_MS = 7L * ONE_DAY_MS
    const val THIRTY_DAYS_MS = 30L * ONE_DAY_MS
    const val ONE_YEAR_MS = 365L * ONE_DAY_MS
    const val TWO_YEARS_MS = 2L * ONE_YEAR_MS
    const val THREE_YEARS_MS = 3L * ONE_YEAR_MS
    const val FIVE_YEARS_MS = 5L * ONE_YEAR_MS
    const val SIX_YEARS_MS = 6L * ONE_YEAR_MS
    const val DEFAULT_PRESET_INDEX = 1
    const val DEFAULT_TOP_CARDS_ORDER = "balance,income_expense,quick_insights"
}
