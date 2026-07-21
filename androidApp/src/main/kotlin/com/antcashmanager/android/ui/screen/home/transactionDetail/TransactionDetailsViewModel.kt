package com.antcashmanager.android.ui.screen.home.transactionDetail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.antcashmanager.android.R
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.usecase.ShareTransactionUseCase

/**
 * ViewModel per il Transaction Details Dialog
 * Gestisce la logica di condivisione utilizzando lo use case
 * Segue la Clean Architecture Pattern
 */
class TransactionDetailsViewModel(
    private val shareTransactionUseCase: ShareTransactionUseCase,
) : ViewModel() {

    /**
     * Condivide i dati della transazione usando lo use case
     * @param transaction La transazione da condividere
     * @param context Contesto Android per avviare l'intent
     */
    fun shareTransaction(
        transaction: Transaction,
        context: Context,
    ) {
        // Usa lo use case per formattare i dati (Business Logic)
        val shareText = shareTransactionUseCase(
            ShareTransactionUseCase.Params(transaction)
        )
        // Crea l'intent di condivisione (Android-specific)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        // Avvia il chooser
        context.startActivity(
            Intent.createChooser(shareIntent, context.getString(R.string.transaction_details_share))
        )
    }
}