package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.NoParamsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


class DeleteAllTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Result<Unit>>(dispatcher) {

    override suspend fun execute(): Result<Unit> = runCatching {
        transactionRepository.deleteAllTransactions()
    }
}
