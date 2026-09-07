package com.antcashmanager.android.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Date/time patterns used across the app. Add here instead of inlining a literal. */
object DatePatterns {
    const val DAY_MONTH = "dd MMM"
    const val DAY_MONTH_YEAR = "dd MMM yyyy"
    const val DAY_MONTH_YEAR_SLASH = "dd/MM/yyyy"
    const val ISO_DATE = "yyyy-MM-dd"
    const val TIME = "HH:mm"
    const val DAY_MONTH_YEAR_TIME = "dd MMM yyyy, HH:mm"
}

/**
 * Formats [timestamp] with [pattern]. A new formatter is created per call: `SimpleDateFormat`
 * is not thread-safe, so never keep one as a shared singleton.
 */
fun formatTimestamp(
    timestamp: Long,
    pattern: String,
    locale: Locale = Locale.getDefault(),
): String = SimpleDateFormat(pattern, locale).format(Date(timestamp))
