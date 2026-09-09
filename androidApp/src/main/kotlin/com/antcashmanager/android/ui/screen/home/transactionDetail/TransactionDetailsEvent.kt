package com.antcashmanager.android.ui.screen.home.transactionDetail

import com.antcashmanager.domain.model.Transaction

/**
 * UDF Pattern: Events for Transaction Details dialog.
 *
 * All user interactions (share, etc.) emit events that the ViewModel
 * processes via onEvent() routing.
 */
sealed class TransactionDetailsEvent {
    /**
     * Richiesta di condivisione della transazione tramite share sheet Android.
     * Il Context per avviare l'intent viene fornito separatamente dalla composable.
     */
    data class ShareTransaction(
        val transaction: Transaction,
    ) : TransactionDetailsEvent()

    data object RetryLastOperation : TransactionDetailsEvent()
}
