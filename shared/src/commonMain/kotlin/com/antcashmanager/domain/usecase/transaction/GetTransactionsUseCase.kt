package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsFlowUseCase<Result<List<Transaction>>>(dispatcher) {

    override fun execute(): Flow<Result<List<Transaction>>> = flow {
        try {
            transactionRepository.getAllTransactions().collect { list ->
                emit(Result.success(list))
            }
        } catch (e: Throwable) {
            emit(Result.failure(e))
        }
    }
}
