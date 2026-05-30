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
    )

    const val ONE_DAY_MS = 24L * 60 * 60 * 1000
    const val ONE_WEEK_MS = 7L * ONE_DAY_MS
    const val THIRTY_DAYS_MS = 30L * ONE_DAY_MS
    const val ONE_YEAR_MS = 365L * ONE_DAY_MS
    const val DEFAULT_PRESET_INDEX = 1
}

