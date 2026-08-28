package com.antcashmanager.android.ui.screen.settings.displaySettings

data class DisplayState(
    val currencySymbol: String = "\u20ac",
    val decimalDigits: Int = 2,
    val decimalSeparator: String = ",",
    val thousandsSeparator: String = "",
    val mealVoucherValue: Double = 5.29,
    val defaultPaymentType: String = "ELECTRONIC",
)
