package com.antcashmanager.domain.model

/**
 * Rappresenta i dati estratti dalla scansione di uno scontrino.
 * È un modello temporaneo (non persistito): viene usato solo per creare una [Transaction].
 *
 * @property totalAmount Importo totale dello scontrino.
 * @property vatRate Aliquota IVA percentuale (es. 22.0). 0.0 se non rilevata.
 * @property vatAmount Importo IVA. 0.0 se non rilevato.
 * @property payee Nome del beneficiario/esercente.
 * @property location Indirizzo o luogo indicato sullo scontrino.
 * @property paymentType Tipo di pagamento rilevato dal testo OCR. Default [PaymentType.ELECTRONIC].
 * @property rawText Testo grezzo OCR originale, utile per debug/correzione manuale.
 * @property items Elenco dei singoli prodotti/voci estratti dallo scontrino.
 */
data class ReceiptData(
    val totalAmount: Double = 0.0,
    val vatRate: Double = 0.0,
    val vatAmount: Double = 0.0,
    val payee: String = "",
    val location: String = "",
    val paymentType: PaymentType = PaymentType.ELECTRONIC,
    val rawText: String = "",
    val items: List<ReceiptItem> = emptyList(),
)

/**
 * Rappresenta una singola voce estratta dallo scontrino (prodotto e prezzo).
 */
data class ReceiptItem(
    val name: String,
    val price: Double,
)



