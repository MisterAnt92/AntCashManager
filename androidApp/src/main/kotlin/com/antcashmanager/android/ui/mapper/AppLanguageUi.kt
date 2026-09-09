package com.antcashmanager.android.ui.mapper

import androidx.annotation.StringRes
import com.antcashmanager.android.R
import com.antcashmanager.domain.model.AppLanguage

@StringRes
fun AppLanguage.labelRes(): Int =
    when (this) {
        AppLanguage.SYSTEM -> R.string.language_system
        AppLanguage.ENGLISH -> R.string.language_english
        AppLanguage.ITALIAN -> R.string.language_italian
        AppLanguage.FRENCH -> R.string.language_french
        AppLanguage.GERMAN -> R.string.language_german
        AppLanguage.SPANISH -> R.string.language_spanish
        AppLanguage.CHINESE_SIMPLIFIED -> R.string.language_chinese_simplified
        AppLanguage.CHINESE_TRADITIONAL -> R.string.language_chinese_traditional
        AppLanguage.JAPANESE -> R.string.language_japanese
        AppLanguage.POLISH -> R.string.language_polish
        AppLanguage.HINDI -> R.string.language_hindi
        AppLanguage.RUSSIAN -> R.string.language_russian
        AppLanguage.UKRAINIAN -> R.string.language_ukrainian
        AppLanguage.KOREAN -> R.string.language_korean
    }
