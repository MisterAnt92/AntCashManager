package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.NoParamsResultUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


class DeleteAllTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsResultUseCase<Unit>(dispatcher) {

    override suspend fun execute(): Unit = transactionRepository.deleteAllTransactions()
}
