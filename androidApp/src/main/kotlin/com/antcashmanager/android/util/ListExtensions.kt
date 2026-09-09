package com.antcashmanager.android.util

/**
 * Returns a copy with the element at [from] moved to [to]. Out-of-range indices return the list unchanged,
 * so callers can pass `index - 1` / `index + 1` without bounds checks.
 */
fun <T> List<T>.moved(
    from: Int,
    to: Int,
): List<T> {
    if (from == to || from !in indices || to !in indices) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}
