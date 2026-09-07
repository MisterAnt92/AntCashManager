package com.antcashmanager.android.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R

/**
 * Maps weekday numbers (1=Monday, 7=Sunday, EU convention) to string resource IDs for localization.
 */
private fun weekdayResId(dayOfWeek: Int): Int? =
    when (dayOfWeek) {
        1 -> R.string.weekday_1
        2 -> R.string.weekday_2
        3 -> R.string.weekday_3
        4 -> R.string.weekday_4
        5 -> R.string.weekday_5
        6 -> R.string.weekday_6
        7 -> R.string.weekday_7
        else -> null
    }

/**
 * Translates a weekday number (1-7) to its localized name using Compose stringResource.
 *
 * @param dayOfWeek Weekday number where 1=Monday, 7=Sunday (EU convention)
 * @return Localized weekday name (e.g. "Mon", "Lun", "月") or day number as string if invalid
 */
@Composable
fun translateWeekday(dayOfWeek: Int): String {
    val resId = weekdayResId(dayOfWeek)
    return if (resId != null) stringResource(resId) else dayOfWeek.toString()
}

/**
 * Non-Composable variant for contexts without a Compose runtime (e.g. Glance widgets).
 *
 * @param context Android context for accessing string resources
 * @param dayOfWeek Weekday number where 1=Monday, 7=Sunday (EU convention)
 * @return Localized weekday name or day number as string if invalid
 */
fun translateWeekdayPlain(
    context: Context,
    dayOfWeek: Int,
): String {
    val resId = weekdayResId(dayOfWeek)
    return if (resId != null) context.getString(resId) else dayOfWeek.toString()
}
