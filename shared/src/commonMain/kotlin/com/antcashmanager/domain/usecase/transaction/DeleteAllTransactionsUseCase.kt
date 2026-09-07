package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public class DeleteAllTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Unit>(dispatcher) {
    override suspend fun execute(params: Unit): Unit = transactionRepository.deleteAllTransactions()
}
