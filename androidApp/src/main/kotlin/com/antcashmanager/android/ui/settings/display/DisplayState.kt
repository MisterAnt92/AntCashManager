package com.antcashmanager.android.ui.settings.display

data class DisplayState(
    val currencySymbol: String = "\u20ac",
    val decimalDigits: Int = 2,
    val decimalSeparator: String = ",",
    val thousandsSeparator: String = ".",
)
