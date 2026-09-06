package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per l'aggiornamento di una transazione.
 * Restituisce [Result] con Unit in caso di successo o l'eccezione in caso di errore.
 */
public class UpdateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Transaction, Unit>(dispatcher) {
    override suspend fun execute(params: Transaction): Unit = transactionRepository.updateTransaction(params)
}
