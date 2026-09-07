package com.antcashmanager.android.ui.screen.home

import com.antcashmanager.android.R
import com.antcashmanager.android.util.DatePatterns

/**
 * Shared constants for Home feature.
 */
object HomeConstant {
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

    // Copre praticamente qualunque storico realistico di transazioni personali.
    const val ALL_TIME_MS = 50L * ONE_YEAR_MS
    const val DEFAULT_PRESET_INDEX = 1
    const val DEFAULT_TOP_CARDS_ORDER = "balance,income_expense,quick_insights"

    /** Date shown on each recent-transaction row. */
    const val ITEM_DATE_PATTERN = DatePatterns.DAY_MONTH

    /** Scroll-to-top FAB appears once the list is scrolled past this item index. */
    const val SCROLL_TO_TOP_ITEM_THRESHOLD = 2

    // Animations
    const val BALANCE_COLOR_ANIM_MS = 600
    const val BALANCE_BREAKDOWN_ANIM_MS = 400
    const val BREAKDOWN_ITEM_FADE_MS = 300
    const val ROW_FADE_IN_MS = 800

    // Income/expense trend icon badge
    const val ICON_BADGE_ALPHA = 0.25f
    const val ICON_BADGE_CORNER_DP = 32
    const val SUBTITLE_TEXT_ALPHA = 0.7f
    const val NOTE_TEXT_ALPHA = 0.6f
}
