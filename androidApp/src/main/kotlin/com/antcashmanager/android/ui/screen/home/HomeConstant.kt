package com.antcashmanager.android.ui.screen.home

import com.antcashmanager.android.util.DatePatterns
import com.antcashmanager.android.ui.screen.common.DateRangePreset

/**
 * Shared constants for Home feature.
 */
object HomeConstant {
    val PRESETS = DateRangePreset.PRESETS
    val ONE_DAY_MS = DateRangePreset.ONE_DAY_MS
    val ONE_WEEK_MS = DateRangePreset.ONE_WEEK_MS
    val THIRTY_DAYS_MS = DateRangePreset.THIRTY_DAYS_MS
    val ONE_YEAR_MS = DateRangePreset.ONE_YEAR_MS
    val TWO_YEARS_MS = DateRangePreset.TWO_YEARS_MS
    val THREE_YEARS_MS = DateRangePreset.THREE_YEARS_MS
    val FIVE_YEARS_MS = DateRangePreset.FIVE_YEARS_MS
    val SIX_YEARS_MS = DateRangePreset.SIX_YEARS_MS
    val ALL_TIME_MS = DateRangePreset.ALL_TIME_MS
    const val DEFAULT_PRESET_INDEX = DateRangePreset.DEFAULT_PRESET_INDEX
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
