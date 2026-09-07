package com.antcashmanager.android.ui.screen.home.transactionDetail

import android.content.Context
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
     * Il [context] serve per avviare l'intent di condivisione.
     */
    data class ShareTransaction(
        val transaction: Transaction,
        val context: Context,
    ) : TransactionDetailsEvent()

    data object RetryLastOperation : TransactionDetailsEvent()
}
