package com.antcashmanager.domain.util

import com.antcashmanager.domain.model.PaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test per [ReceiptTextParser] — parsing del testo grezzo degli scontrini.
 */
class ReceiptTextParserTest {

    // ── parseDecimal ─────────────────────────────────────────────────────────

    @Test
    fun parseDecimal_shouldReturnCorrectValue_whenEuropeanFormat() {
        assertEquals(1234.56, ReceiptTextParser.parseDecimal("1.234,56"), 0.001)
    }

    @Test
    fun parseDecimal_shouldReturnCorrectValue_whenCommaDecimalSeparator() {
        assertEquals(68.90, ReceiptTextParser.parseDecimal("68,90"), 0.001)
    }

    @Test
    fun parseDecimal_shouldReturnCorrectValue_whenDotDecimalSeparator() {
        assertEquals(68.90, ReceiptTextParser.parseDecimal("68.90"), 0.001)
    }

    @Test
    fun parseDecimal_shouldReturnZero_whenBlank() {
        assertEquals(0.0, ReceiptTextParser.parseDecimal(""), 0.001)
    }

    // ── extractTotal ─────────────────────────────────────────────────────────

    @Test
    fun parse_shouldExtractTotal_whenTotaleKeywordPresent() {
        val text = """
            SUPERMERCATO ABC
            Via Roma 1
            TOTALE EUR    68,90
            GRAZIE
        """.trimIndent()

        val result = ReceiptTextParser.parse(text)
        assertEquals(68.90, result.totalAmount, 0.001)
    }

    @Test
    fun parse_shouldExtractTotal_withTotalKeyword() {
        val text = "TOTAL: 25.50\nTHANK YOU"
        val result = ReceiptTextParser.parse(text)
        assertEquals(25.50, result.totalAmount, 0.001)
    }

    @Test
    fun parse_shouldFallbackToMaxValue_whenNoTotaleKeyword() {
        val text = """
            Item A  10,00
            Item B  15,50
            Item C  30,00
        """.trimIndent()

        val result = ReceiptTextParser.parse(text)
        assertEquals(30.0, result.totalAmount, 0.001)
    }

    // ── extractVat ───────────────────────────────────────────────────────────

    @Test
    fun parse_shouldExtractVatRate_whenIvaPercentPresent() {
        val text = "TOTALE 55,00\nIVA 22% 10,00"
        val result = ReceiptTextParser.parse(text)
        assertEquals(22.0, result.vatRate, 0.001)
    }

    @Test
    fun parse_shouldExtractVatAmount_whenIvaAmountPresent() {
        val text = "TOTALE 55,00\nIVA 22% 10,00"
        val result = ReceiptTextParser.parse(text)
        assertEquals(10.0, result.vatAmount, 0.001)
    }

    @Test
    fun parse_shouldReturnZeroVat_whenIvaNotPresent() {
        val text = "TOTALE 55,00"
        val result = ReceiptTextParser.parse(text)
        assertEquals(0.0, result.vatRate, 0.001)
        assertEquals(0.0, result.vatAmount, 0.001)
    }

    // ── extractPayee ─────────────────────────────────────────────────────────

    @Test
    fun parse_shouldExtractPayee_fromFirstMeaningfulLine() {
        val text = """
            ESSELUNGA S.P.A.
            Via Roma 1, Milano
            TOTALE 68,90
        """.trimIndent()

        val result = ReceiptTextParser.parse(text)
        assertEquals("ESSELUNGA S.P.A.", result.payee)
    }

    @Test
    fun parse_shouldIgnoreLinesThatAreOnlyNumbers_forPayee() {
        val text = """
            12345678
            CONAD SRL
            TOTALE 20,00
        """.trimIndent()

        val result = ReceiptTextParser.parse(text)
        assertEquals("CONAD SRL", result.payee)
    }

    // ── extractLocation ──────────────────────────────────────────────────────

    @Test
    fun parse_shouldExtractLocation_whenAddressPatternPresent() {
        val text = """
            LIDL ITALIA
            VIA GARIBALDI 10, TORINO
            TOTALE 35,00
        """.trimIndent()

        val result = ReceiptTextParser.parse(text)
        assertTrue(result.location.contains("GARIBALDI", ignoreCase = true))
    }

    // ── detectPaymentType ────────────────────────────────────────────────────

    @Test
    fun detectPaymentType_shouldReturnCash_whenContantePresent() {
        val text = "TOTALE 20,00\nCONTANTE 20,00\nRESTO 0,00"
        assertEquals(PaymentType.CASH, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldReturnCash_whenCashKeyword() {
        val text = "TOTALE 15,50\nCASH"
        assertEquals(PaymentType.CASH, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldReturnMealVouchers_whenBuonoPastoPresent() {
        val text = "TOTALE 8,50\nBUONO PASTO 8,50"
        assertEquals(PaymentType.MEAL_VOUCHERS, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldReturnMealVouchers_whenEdenredPresent() {
        val text = "TOTALE 7,00\nEDENRED"
        assertEquals(PaymentType.MEAL_VOUCHERS, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldReturnMealVouchers_whenTicketRestaurantPresent() {
        val text = "TOTALE 9,00\nTICKET RESTAURANT"
        assertEquals(PaymentType.MEAL_VOUCHERS, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldReturnElectronic_whenBancomatPresent() {
        val text = "TOTALE 35,00\nBANCOMAT"
        assertEquals(PaymentType.ELECTRONIC, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldReturnElectronic_whenVisaPresent() {
        val text = "TOTALE 55,00\nVISA **** 1234"
        assertEquals(PaymentType.ELECTRONIC, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldReturnElectronic_whenSatispayPresent() {
        val text = "TOTALE 12,00\nSATISPAY"
        assertEquals(PaymentType.ELECTRONIC, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldReturnElectronic_whenNoKeywordPresent() {
        val text = "TOTALE 10,00"
        assertEquals(PaymentType.ELECTRONIC, ReceiptTextParser.detectPaymentType(text))
    }

    @Test
    fun detectPaymentType_shouldPrioritizeMealVouchers_overCash() {
        // Scontrino con ENTRAMBI i marcatori: deve vincere MEAL_VOUCHERS
        val text = "TOTALE 8,00\nBUONO PASTO\nCONTANTE"
        assertEquals(PaymentType.MEAL_VOUCHERS, ReceiptTextParser.detectPaymentType(text))
    }

    // ── Consistenza IVA ──────────────────────────────────────────────────────

    @Test
    fun parse_shouldResetVatAmount_whenVatExceedsTotal() {
        // IVA 50 > totale 10 → dato incoerente → vatAmount deve essere azzerato
        val text = "TOTALE 10,00\nIVA 22% 50,00"
        val result = ReceiptTextParser.parse(text)
        assertEquals(0.0, result.vatAmount, 0.001)
    }

    @Test
    fun parse_shouldKeepVatAmount_whenVatIsLessThanTotal() {
        val text = "TOTALE 55,00\nIVA 22% 10,00"
        val result = ReceiptTextParser.parse(text)
        assertEquals(10.0, result.vatAmount, 0.001)
    }

    // ── paymentType nel ReceiptData ──────────────────────────────────────────

    @Test
    fun parse_shouldIncludeDetectedPaymentType_inReceiptData() {
        val text = "TOTALE 20,00\nBANCOMAT"
        val result = ReceiptTextParser.parse(text)
        assertEquals(PaymentType.ELECTRONIC, result.paymentType)
    }

    @Test
    fun parse_shouldDetectCash_inReceiptData() {
        val text = "TOTALE 20,00\nCONTANTE"
        val result = ReceiptTextParser.parse(text)
        assertEquals(PaymentType.CASH, result.paymentType)
    }

    @Test
    fun parse_shouldDetectMealVouchers_inReceiptData() {
        val text = "TOTALE 8,00\nBUONI PASTO"
        val result = ReceiptTextParser.parse(text)
        assertEquals(PaymentType.MEAL_VOUCHERS, result.paymentType)
    }

    // ── rawText ──────────────────────────────────────────────────────────────

    @Test
    fun parse_shouldPreserveRawText() {
        val text = "TOTALE 10,00"
        val result = ReceiptTextParser.parse(text)
        assertEquals(text, result.rawText)
    }

    // ── Full receipt ─────────────────────────────────────────────────────────

    @Test
    fun parse_shouldExtractAllFields_forCompleteItalianReceipt() {
        val text = """
            COOP ITALIA
            PIAZZA VENEZIA 3, ROMA
            P.IVA 01234567890
            -----------------
            PASTA         1,50
            ACQUA         0,50
            PANE          1,20
            -----------------
            IVA 22% 3,20
            TOTALE EUR    3,20
            CONTANTE      5,00
            RESTO         1,80
        """.trimIndent()

        val result = ReceiptTextParser.parse(text)
        assertEquals(3.20, result.totalAmount, 0.01)
        assertEquals(22.0, result.vatRate, 0.001)
        assertEquals("COOP ITALIA", result.payee)
        assertTrue(result.location.isNotBlank())
    }
}

