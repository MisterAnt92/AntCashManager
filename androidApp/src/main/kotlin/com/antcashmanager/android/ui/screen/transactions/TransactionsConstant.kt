package com.antcashmanager.android.ui.screen.transactions

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.screen.common.DateRangePreset

/**
 * Shared constants for Transactions screen.
 */
object TransactionsConstant {
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

    // Layout
    val SCREEN_HORIZONTAL_PADDING = 16.dp
    val SCREEN_VERTICAL_PADDING = 12.dp
    val CARD_SPACING = 12.dp
    val SECTION_SPACING = 8.dp
    val FILTER_CHIP_SPACING = 8.dp
    val FILTER_CHIP_VERTICAL_SPACING = 4.dp
    val FILTER_CARD_PADDING = 12.dp
    val FILTER_HEADER_SPACING = 4.dp
    val FILTER_HEADER_FONT_WEIGHT = FontWeight.SemiBold
    val FILTER_SECTION_SPACING = 6.dp
    val LIST_BOTTOM_PADDING_DP = 80.dp

    // Scroll-to-top FAB threshold
    const val SCROLL_TO_TOP_ITEM_THRESHOLD = 2

    // Skeleton loader
    val SKELETON_LOADER_HEIGHT = 16.dp
    val SKELETON_LOADER_CORNER_RADIUS = 8
    val SKELETON_LOADER_ROW_HEIGHT = 12.dp
    val SKELETON_LOADER_ROW_CORNER_RADIUS = 6
    val SKELETON_LOADER_BOTTOM_HEIGHT = 20.dp

    // FAB icons
    val SCROLL_TO_TOP_BUTTON_SIZE = 44.dp
    val SCROLL_TO_TOP_ICON_SIZE = 20.dp
    val RECEIPT_BUTTON_ICON_SIZE = 24.dp
    val ADD_BUTTON_ICON_SIZE = 28.dp

    // Filter categories limit
    const val MAX_FILTER_CATEGORIES = 8

    // Skeleton items count
    const val SKELETON_ITEMS = 6
}
