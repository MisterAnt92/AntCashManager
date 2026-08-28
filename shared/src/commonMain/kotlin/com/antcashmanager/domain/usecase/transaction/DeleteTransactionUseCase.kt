package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per la cancellazione di una transazione.
 */
public class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Transaction, Unit>(dispatcher) {

    override suspend fun execute(params: Transaction): Unit =
        transactionRepository.deleteTransaction(params)
}
