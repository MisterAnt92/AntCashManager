package com.antcashmanager.android.navigation

/**
 * Stato UI per le impostazioni globali usate dal NavHost (formato valuta, visibilità grafici, ecc.).
 * [isTutorialCompleted] è null finché il DataStore non ha emesso il primo valore (splash screen attivo).
 * [analyticsConsent] è null se l'utente non ha mai risposto al dialog GDPR.
 */
data class NavSettingsState(
    val showCharts: Boolean = true,
    val isTutorialCompleted: Boolean? = null,
    val currencySymbol: String = "€",
    val decimalDigits: Int = 2,
    val decimalSeparator: String = ",",
    val thousandsSeparator: String = "",
    val maskAmounts: Boolean = false,
    val analyticsConsent: Boolean? = false,
)
