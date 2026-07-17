package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.BaseResultUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per la cancellazione di una transazione.
 */
class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseResultUseCase<Transaction, Unit>(dispatcher) {

    override suspend fun execute(params: Transaction): Unit =
        transactionRepository.deleteTransaction(params)
}
