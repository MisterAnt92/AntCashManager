package com.antcashmanager.domain.util

/**
 * Helper per rilevare la lingua del testo di uno scontrino basandosi su keyword dominanti.
 * Supporta: Italiano, Inglese, Spagnolo, Tedesco, Francese
 */
public object LanguageDetectionHelper {

    private val KEYWORDS_IT = listOf(
        "TOTALE", "IVA", "IMPORTO", "SCONTRINO", "BENEFICIARIO",
        "ESSELUNGA", "CONAD", "COOP", "CARREFOUR", "LIDL",
        "CONTANTE", "CONTANTI", "BUONO PASTO", "BUONI PASTO"
    )

    private val KEYWORDS_EN = listOf(
        "TOTAL", "TAX", "AMOUNT", "RECEIPT", "PAYEE",
        "SUBTOTAL", "GRAND TOTAL", "CASH", "CARD", "MEAL VOUCHER"
    )

    private val KEYWORDS_ES = listOf(
        "TOTAL", "IVA", "CANTIDAD", "RECIBO", "BENEFICIARIO",
        "EFECTIVO", "TARJETA", "CHEQUE RESTAURANTE", "IMPORTE"
    )

    private val KEYWORDS_DE = listOf(
        "GESAMT", "SUMME", "BETRAG", "MwSt", "BELEG",
        "KASSE", "KARTE", "ESSENSMARKEN", "GESAMTBETRAG"
    )

    private val KEYWORDS_FR = listOf(
        "TOTAL", "TVA", "MONTANT", "REÇU", "BÉNÉFICIAIRE",
        "ESPÈCES", "CARTE", "TICKET RESTAURANT", "SOUS-TOTAL"
    )

    /**
     * Rileva la lingua del testo dello scontrino basandosi su keyword dominanti.
     *
     * @param text Testo estratto dall'OCR
     * @return Language code: "it", "en", "es", "de", "fr" (default: "it")
     */
    public fun detectLanguage(text: String): String {
        val upperText = text.uppercase()

        val itCount = KEYWORDS_IT.count { upperText.contains(it) }
        val enCount = KEYWORDS_EN.count { upperText.contains(it) }
        val esCount = KEYWORDS_ES.count { upperText.contains(it) }
        val deCount = KEYWORDS_DE.count { upperText.contains(it) }
        val frCount = KEYWORDS_FR.count { upperText.contains(it) }

        val maxCount = maxOf(itCount, enCount, esCount, deCount, frCount)

        return when (maxCount) {
            itCount -> "it"
            enCount -> "en"
            esCount -> "es"
            deCount -> "de"
            frCount -> "fr"
            else -> "it"  // default
        }
    }
}
