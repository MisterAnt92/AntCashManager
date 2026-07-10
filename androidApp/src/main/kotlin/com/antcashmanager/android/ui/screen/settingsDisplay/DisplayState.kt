package com.antcashmanager.android.ui.screen.settingsDisplay

data class DisplayState(
    val currencySymbol: String = "\u20ac",
    val decimalDigits: Int = 2,
    val decimalSeparator: String = ",",
    val thousandsSeparator: String = "",
    val mealVoucherValue: Double = 5.29,
)
