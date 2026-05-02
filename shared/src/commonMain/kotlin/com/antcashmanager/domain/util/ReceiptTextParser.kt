package com.antcashmanager.domain.util

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.ReceiptData
import com.antcashmanager.domain.model.ReceiptItem

/**
 * Parser puro Kotlin per estrarre dati strutturati dal testo grezzo di uno scontrino.
 *
 * Gestisce formati comuni di scontrini italiani ed europei.
 * Il parsing è best-effort: i campi non trovati restano al valore default.
 * Rileva automaticamente il tipo di pagamento (contante, buoni pasto, elettronico).
 */
object ReceiptTextParser {

    // ── Regex Patterns ────────────────────────────────────────────────────────

    /** Importo totale: TOTALE, TOTAL, TOT seguiti da un valore numerico */
    private val TOTAL_PATTERN = Regex(
        """(?:TOTALE|TOTAL|TOT\.?|IMPORTO TOTALE|IMPORTO|AMOUNT)\s*[:\s€$]?\s*([\d]+[.,][\d]{2})""",
        RegexOption.IGNORE_CASE,
    )

    /** Aliquota IVA: "IVA 22%" o "IVA22%" */
    private val VAT_RATE_PATTERN = Regex(
        """IVA\s*(\d{1,2})\s*%""",
        RegexOption.IGNORE_CASE,
    )

    /** Importo IVA: "IVA 22% 4,50" o "IVA 4,50" */
    private val VAT_AMOUNT_PATTERN = Regex(
        """IVA\s*(?:\d{1,2}\s*%)?\s*([\d]+[.,][\d]{2})""",
        RegexOption.IGNORE_CASE,
    )

    /** Indirizzo: righe che iniziano con VIA, VIALE, PIAZZA, C.SO, LOC., ecc. */
    private val ADDRESS_PATTERN = Regex(
        """^(?:VIA|V\.LE|VIALE|PIAZZA|P\.ZZA|C\.SO|CORSO|LOC\.|LOCALITA\'?|STR\.|STRADA)\s+.+""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE),
    )

    /** Pattern per singole voci: Descrizione ... Prezzo (es: "PANE 1,50" o "LATTE ... 0.99") */
    private val ITEM_PATTERN = Regex(
        """^([A-Z\s]{3,})\s+[:\.]*\s*([\d]+[.,][\d]{2})$""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
    )

    /** Numero con decimali (virgola o punto come separatore) */
    private val NUMERIC_PATTERN = Regex("""[\d]+[.,][\d]{2}""")

    private val MARKET_KEYWORDS = listOf(
        "ESSELUNGA", "CONAD", "COOP", "CARREFOUR", "LIDL", "EUROSPIN", "PAM", "DESPAR",
        "TIGROS", "BENNET", "PENNY", "ALDI", "MD", "IPER", "AUCHAN", "AUTOGRILL", "AMAZON"
    )

    // ... (altri pattern invariati)

    /**
     * Parole chiave che indicano pagamento in CONTANTI.
     * Include: contante, contanti, cash, spiccioli, resto (cambio su scontrino = pagamento cash).
     */
    private val CASH_PATTERN = Regex(
        """(?:CONTANTE|CONTANTI|CASH|PAGATO IN CONTANTI|SPICCIOLI|DARE|AVERE|RESTO(?!\s*\d{1,2}%))""",
        RegexOption.IGNORE_CASE,
    )

    /**
     * Parole chiave che indicano pagamento con BUONI PASTO.
     * Include: buono/i pasto, ticket restaurant/resto, edenred, pluxee, sodexo, mybenefit, up dejeuner.
     */
    private val MEAL_VOUCHER_PATTERN = Regex(
        """(?:BUON[OI]\s+PAST[OI]|BUONI\s+PASTO|TICKET\s+(?:RESTAURANT|RISTORANTE|PASTO|RESTO)|"""
            + """EDENRED|PLUXEE|SODEXO|MYBENEFIT|UP\s+DEJEUNER|BUONO\s+MENSA|VOUCHER\s+PASTO)""",
        RegexOption.IGNORE_CASE,
    )

    /**
     * Parole chiave che indicano pagamento ELETTRONICO.
     * Include: bancomat, carta, visa, mastercard, maestro, pos, paywave, contactless,
     * satispay, apple pay, google pay, amex, pagamento elettronico.
     */
    private val ELECTRONIC_PATTERN = Regex(
        """(?:BANCOMAT|CARTA\s+(?:DI\s+CREDITO|DI\s+DEBITO)?|VISA|MASTERCARD|MAESTRO|"""
            + """POS|PAYWAVE|CONTACTLESS|SATISPAY|APPLE\s+PAY|GOOGLE\s+PAY|AMEX|"""
            + """AMERICAN\s+EXPRESS|BONIFICO|PAGAMENTO\s+ELETTRONICO|ELECTRONIC\s+PAYMENT)""",
        RegexOption.IGNORE_CASE,
    )

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Parsa il testo grezzo di uno scontrino e restituisce un [ReceiptData] validato.
     *
     * Il parser:
     * - Estrae importo totale, IVA, beneficiario, luogo
     * - Rileva automaticamente il tipo di pagamento
     * - Valida la consistenza dei dati (IVA non può superare il totale)
     *
     * @param rawText Testo estratto dall'OCR.
     * @return [ReceiptData] con i campi valorizzati e validati.
     */
    fun parse(rawText: String): ReceiptData {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val totalAmount = extractTotal(rawText, lines)
        val vatRate = extractVatRate(rawText)
        val rawVatAmount = extractVatAmount(rawText)

        // Validazione consistenza: IVA non può superare il totale (tolleranza 1%)
        val vatAmount = if (totalAmount > 0.0 && rawVatAmount > totalAmount * 1.01) {
            0.0 // dato incoerente: scarta il valore IVA
        } else {
            rawVatAmount
        }

        return ReceiptData(
            totalAmount = totalAmount,
            vatRate = vatRate,
            vatAmount = vatAmount,
            payee = extractPayee(lines),
            location = extractLocation(lines),
            paymentType = detectPaymentType(rawText),
            rawText = rawText,
            items = extractItems(lines),
        )
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun extractTotal(text: String, lines: List<String>): Double {
        // 1. Cerca pattern esplicito TOTALE/TOTAL
        val match = TOTAL_PATTERN.find(text)
        if (match != null) return parseDecimal(match.groupValues[1])

        // 2. Fallback: cerca il valore massimo nella riga contenente "TOTALE"
        val totalLine = lines.firstOrNull {
            it.contains("TOTALE", ignoreCase = true) || it.contains("TOTAL", ignoreCase = true)
        }
        if (totalLine != null) {
            val amounts = NUMERIC_PATTERN.findAll(totalLine)
                .map { parseDecimal(it.value) }
                .filter { it > 0.0 }
                .toList()
            if (amounts.isNotEmpty()) return amounts.max()
        }

        // 3. Ultimo fallback: il valore numerico più grande nel documento
        return NUMERIC_PATTERN.findAll(text)
            .map { parseDecimal(it.value) }
            .filter { it > 0.0 }
            .maxOrNull() ?: 0.0
    }

    private fun extractVatRate(text: String): Double {
        return VAT_RATE_PATTERN.find(text)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    private fun extractVatAmount(text: String): Double {
        return VAT_AMOUNT_PATTERN.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { parseDecimal(it) }
            ?: 0.0
    }

    private fun extractPayee(lines: List<String>): String {
        // 1. Cerca nomi di market noti nelle prime righe
        for (i in 0 until minOf(5, lines.size)) {
            val line = lines[i].uppercase()
            val market = MARKET_KEYWORDS.find { line.contains(it) }
            if (market != null) return market
        }

        // 2. Fallback: prima riga non numerica
        return lines.firstOrNull { line ->
            line.length >= 3 &&
                !line.matches(Regex("""[\d\s.,:;/\\-]+""")) &&
                !line.startsWith("P.IVA", ignoreCase = true) &&
                !line.startsWith("C.F.", ignoreCase = true)
        } ?: ""
    }

    private fun extractLocation(lines: List<String>): String {
        // 1. Cerca pattern indirizzo noto
        val addressLine = lines.firstOrNull { ADDRESS_PATTERN.containsMatchIn(it) }
        
        // 2. Fallback: la prima o seconda riga spesso contengono il luogo (se non sono il payee)
        val fallbackLine = if (lines.size >= 2) {
            val firstLine = lines[0]
            val secondLine = lines[1]
            when {
                secondLine.length > 5 && !secondLine.contains("TOTALE", true) -> secondLine
                firstLine.length > 5 && !firstLine.contains("TOTALE", true) -> firstLine
                else -> ""
            }
        } else ""
        
        return addressLine ?: fallbackLine
    }

    private fun extractItems(lines: List<String>): List<ReceiptItem> {
        val excludedKeywords = listOf("TOTALE", "TOTAL", "IVA", "EURO")
        return lines.filter { line ->
            excludedKeywords.none { line.contains(it, true) }
        }.mapNotNull { line ->
            ITEM_PATTERN.find(line)?.let { match ->
                val name = match.groupValues[1].trim()
                val price = parseDecimal(match.groupValues[2])
                if (name.length > 2 && price > 0.0) {
                    ReceiptItem(name, price)
                } else null
            }
        }
    }

    /**
     * Rileva il tipo di pagamento dall'intero testo dello scontrino.
     *
     * Priorità di rilevamento:
     * 1. [PaymentType.MEAL_VOUCHERS] — parole chiave specifiche (es. "BUONO PASTO", "EDENRED")
     * 2. [PaymentType.CASH] — parole chiave contante (es. "CONTANTE", "RESTO")
     * 3. [PaymentType.ELECTRONIC] — parole chiave elettroniche (es. "BANCOMAT", "VISA") o default
     *
     * La priorità dei buoni pasto è massima perché spesso i scontrini buoni pasto
     * contengono anche la parola "TOTALE" con importo, ma il metodo di pagamento
     * è distintivo e specifico.
     */
    fun detectPaymentType(text: String): PaymentType {
        if (MEAL_VOUCHER_PATTERN.containsMatchIn(text)) return PaymentType.MEAL_VOUCHERS
        if (CASH_PATTERN.containsMatchIn(text)) return PaymentType.CASH
        if (ELECTRONIC_PATTERN.containsMatchIn(text)) return PaymentType.ELECTRONIC
        // Default: ELECTRONIC (il più comune nei contesti moderni)
        return PaymentType.ELECTRONIC
    }

    /**
     * Converte una stringa decimale (virgola o punto) in Double.
     * Esempio: "1.234,56" → 1234.56, "1234.56" → 1234.56
     */
    fun parseDecimal(value: String): Double {
        if (value.isBlank()) return 0.0
        val normalized = when {
            // formato europeo: "1.234,56"
            value.contains(',') && value.contains('.') && value.lastIndexOf(',') > value.lastIndexOf('.') ->
                value.replace(".", "").replace(",", ".")
            // solo virgola come separatore decimale: "1234,56"
            value.contains(',') && !value.contains('.') ->
                value.replace(",", ".")
            else -> value
        }
        return normalized.toDoubleOrNull() ?: 0.0
    }
}

