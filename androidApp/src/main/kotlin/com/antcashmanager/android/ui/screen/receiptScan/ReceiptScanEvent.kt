package com.antcashmanager.android.ui.screen.receiptScan

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType

/**
 * UDF Pattern: Events for ReceiptScan screen.
 *
 * All OCR scan operations and transaction field updates emit events
 * that the ViewModel processes to update receipt state.
 */
sealed class ReceiptScanEvent {
    data class ScanReceipt(
        val imageBytes: ByteArray,
    ) : ReceiptScanEvent()

    data class UpdateTitle(
        val title: String,
    ) : ReceiptScanEvent()

    data class UpdatePayee(
        val payee: String,
    ) : ReceiptScanEvent()

    data class UpdateLocation(
        val location: String,
    ) : ReceiptScanEvent()

    data class UpdateNotes(
        val notes: String,
    ) : ReceiptScanEvent()

    data class UpdateAmount(
        val amount: Double,
    ) : ReceiptScanEvent()

    data class SelectCategory(
        val category: Category,
    ) : ReceiptScanEvent()

    data class SelectPaymentType(
        val paymentType: PaymentType,
    ) : ReceiptScanEvent()

    data object ShowCategoryDialog : ReceiptScanEvent()

    data object DismissCategoryDialog : ReceiptScanEvent()

    data object ShowPaymentTypeDialog : ReceiptScanEvent()

    data object DismissPaymentTypeDialog : ReceiptScanEvent()

    data object RetryCapture : ReceiptScanEvent()

    data object RetryLastOperation : ReceiptScanEvent()
}
