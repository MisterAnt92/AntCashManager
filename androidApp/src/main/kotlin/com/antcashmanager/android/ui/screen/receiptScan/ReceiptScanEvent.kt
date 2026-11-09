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
    data class ScanReceiptEvent(
        val imageBytes: ByteArray,
    ) : ReceiptScanEvent()

    data class UpdateTitleEvent(
        val title: String,
    ) : ReceiptScanEvent()

    data class UpdatePayeeEvent(
        val payee: String,
    ) : ReceiptScanEvent()

    data class UpdateLocationEvent(
        val location: String,
    ) : ReceiptScanEvent()

    data class UpdateNotesEvent(
        val notes: String,
    ) : ReceiptScanEvent()

    data class UpdateAmountEvent(
        val amount: Double,
    ) : ReceiptScanEvent()

    data class SelectCategoryEvent(
        val category: Category,
    ) : ReceiptScanEvent()

    data class SelectPaymentTypeEvent(
        val paymentType: PaymentType,
    ) : ReceiptScanEvent()

    data object ShowCategoryDialogEvent : ReceiptScanEvent()

    data object DismissCategoryDialogEvent : ReceiptScanEvent()

    data object ShowPaymentTypeDialogEvent : ReceiptScanEvent()

    data object DismissPaymentTypeDialogEvent : ReceiptScanEvent()

    data object RetryCaptureEvent : ReceiptScanEvent()

    data object RetryLastOperationEvent : ReceiptScanEvent()
}
