package com.antcashmanager.android.ui.mapper

import androidx.annotation.StringRes
import com.antcashmanager.android.R
import com.antcashmanager.domain.model.RecurrenceInterval

/**
 * Persisted keys for [Transaction.recurrenceInterval] (lower-case) and their labels.
 * The domain stores the interval as a raw string; this is the only place that knows the keys.
 */
object RecurrenceIntervalKeys {
    const val DAILY = "daily"
    const val WEEKLY = "weekly"
    const val MONTHLY = "monthly"
    const val YEARLY = "yearly"

    val ALL: List<String> = listOf(DAILY, WEEKLY, MONTHLY, YEARLY)
}

fun RecurrenceInterval.key(): String = name.lowercase()

/** Returns the domain enum for a persisted key, or `null` when the key is blank/unknown. */
fun recurrenceIntervalOf(raw: String): RecurrenceInterval? =
    RecurrenceInterval.entries.firstOrNull { it.key() == raw.trim().lowercase() }

@StringRes
fun RecurrenceInterval.labelRes(): Int =
    when (this) {
        RecurrenceInterval.DAILY -> R.string.transactions_interval_daily
        RecurrenceInterval.WEEKLY -> R.string.transactions_interval_weekly
        RecurrenceInterval.MONTHLY -> R.string.transactions_interval_monthly
        RecurrenceInterval.YEARLY -> R.string.transactions_interval_yearly
    }

/** Label for a raw persisted key; falls back to the generic "Recurring" label for unknown keys. */
@StringRes
fun recurrenceIntervalLabelRes(raw: String): Int =
    recurrenceIntervalOf(raw)?.labelRes() ?: R.string.transactions_recurring
