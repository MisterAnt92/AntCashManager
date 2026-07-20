package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.NoParamsResultFlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsResultFlowUseCase<List<Transaction>>(dispatcher) {

    override fun execute(): Flow<List<Transaction>> = transactionRepository.getAllTransactions()
}
