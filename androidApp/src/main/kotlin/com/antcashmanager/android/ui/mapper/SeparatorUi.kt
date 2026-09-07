package com.antcashmanager.android.ui.mapper

import androidx.annotation.StringRes
import com.antcashmanager.android.R

/** Separator characters accepted by CurrencyFormat, with their labels. */
object SeparatorKeys {
    const val COMMA = ","
    const val PERIOD = "."
    const val SPACE = " "
    const val NONE = ""
}

/** Label resource for a decimal/thousands separator value, or `null` for unknown values. */
@StringRes
fun separatorLabelRes(value: String): Int? =
    when (value) {
        SeparatorKeys.COMMA -> R.string.settings_separator_comma
        SeparatorKeys.PERIOD -> R.string.settings_separator_period
        SeparatorKeys.SPACE -> R.string.settings_separator_space
        SeparatorKeys.NONE -> R.string.settings_separator_none
        else -> null
    }
