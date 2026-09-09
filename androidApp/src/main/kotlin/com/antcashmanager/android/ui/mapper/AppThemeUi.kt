package com.antcashmanager.android.ui.mapper

import androidx.annotation.StringRes
import com.antcashmanager.android.R
import com.antcashmanager.domain.model.AppTheme

@StringRes
fun AppTheme.labelRes(): Int =
    when (this) {
        AppTheme.LIGHT -> R.string.settings_theme_light
        AppTheme.DARK -> R.string.settings_theme_dark
        AppTheme.SYSTEM -> R.string.settings_theme_system
        AppTheme.ANNA -> R.string.settings_theme_anna
    }
