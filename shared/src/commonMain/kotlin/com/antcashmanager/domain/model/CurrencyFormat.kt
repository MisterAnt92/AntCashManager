package com.antcashmanager.domain.model

/** Preferences for formatting monetary amounts throughout the app. */
data class CurrencyFormat(
    val currencySymbol: String = "\u20ac",
    val decimalDigits: Int = 2,
    val decimalSeparator: String = ",",
    val thousandsSeparator: String = "",
) {
    companion object {
        val DEFAULT = CurrencyFormat()

        /** Symbol to display-label pairs for all supported currencies. */
        val SUPPORTED_CURRENCIES: List<Pair<String, String>> = listOf(
            "\u20ac"  to "Euro (\u20ac)",
            "$"       to "US Dollar ($)",
            "\u00a3"  to "British Pound (\u00a3)",
            "\u00a5"  to "Japanese Yen / Chinese Yuan (\u00a5)",
            "\u20b9"  to "Indian Rupee (\u20b9)",
            "CHF"     to "Swiss Franc (CHF)",
            "kr"      to "Krona - SEK / NOK / DKK (kr)",
            "R\$"     to "Brazilian Real (R\$)",
            "A\$"     to "Australian Dollar (A\$)",
            "C\$"     to "Canadian Dollar (C\$)",
            "\u20a9"  to "Korean Won (\u20a9)",
            "\u20ba"  to "Turkish Lira (\u20ba)",
            "z\u0142" to "Polish Zloty (z\u0142)",
            "\u20b1"  to "Philippine Peso (\u20b1)",
            "\u0e3f"  to "Thai Baht (\u0e3f)",
        )

        val DECIMAL_SEPARATORS: List<Pair<String, String>> = listOf(
            "," to "Comma (,)",
            "." to "Period (.)",
        )

        /** Empty string = no thousands separator. */
        val THOUSANDS_SEPARATORS: List<Pair<String, String>> = listOf(
            "." to "Period (.)",
            "," to "Comma (,)",
            " " to "Space",
            ""  to "None",
        )
    }
}

