package com.antcashmanager.domain.model

/**
 * Enum che rappresenta i diversi formati di visualizzazione delle date supportati dall'applicazione.
 */
public enum class DateFormatType(
    public val pattern: String,
) {
    DD_MM_YYYY("dd/MM/yyyy"),
    MM_DD_YYYY("MM/dd/yyyy"),
    YYYY_MM_DD("yyyy-MM-dd"),
    DD_MMM_YYYY("dd MMM yyyy"),
    ;

    public companion object {
        /**
         * Tutti i formati disponibili con la loro descrizione leggibile.
         */
        public val SUPPORTED_FORMATS: List<Pair<DateFormatType, String>> =
            listOf(
                DD_MM_YYYY to "DD/MM/YYYY",
                MM_DD_YYYY to "MM/DD/YYYY",
                YYYY_MM_DD to "YYYY-MM-DD",
                DD_MMM_YYYY to "DD MMM YYYY",
            )

        /**
         * Restituisce il formato corrispondente al pattern specificato.
         */
        public fun fromPattern(pattern: String): DateFormatType = entries.find { it.pattern == pattern } ?: DD_MM_YYYY
    }
}
