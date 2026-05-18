package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per l'aggiornamento di una transazione.
 * Restituisce [Result] con Unit in caso di successo o l'eccezione in caso di errore.
 */
class UpdateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<Transaction, Result<Unit>>(dispatcher) {

    override suspend fun execute(params: Transaction): Result<Unit> = runCatching {
        transactionRepository.updateTransaction(params)
    }
}

