package com.antcashmanager.android.ui.screen.receiptScan

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.ReceiptData

/**
 * UI State per la schermata di scansione scontrino.
 */
data class ReceiptScanState(
    val step: ReceiptScanStep = ReceiptScanStep.CAPTURE,
    val receiptData: ReceiptData? = null,
    val title: String = "",
    val payee: String = "",
    val location: String = "",
    val notes: String = "",
    val vatNote: String = "",
    val selectedCategory: Category? = null,
    val selectedPaymentType: PaymentType = PaymentType.ELECTRONIC,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTransactionSaved: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val showPaymentTypeDialog: Boolean = false,
)

/**
 * Step del flusso di scansione scontrino.
 */
enum class ReceiptScanStep {
    CAPTURE,
    PROCESSING,
    REVIEW,
}
