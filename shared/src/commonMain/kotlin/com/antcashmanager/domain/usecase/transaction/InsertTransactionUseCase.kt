package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per l'inserimento di una transazione.
 * Restituisce un [Result] che contiene l'id della transazione inserita o l'eccezione di dominio.
 */
public class InsertTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Transaction, Long>(dispatcher) {

    override suspend fun execute(params: Transaction): Long =
        transactionRepository.insertTransaction(params)
}
