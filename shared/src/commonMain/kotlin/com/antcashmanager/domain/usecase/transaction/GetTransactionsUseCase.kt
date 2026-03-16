package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) : NoParamsFlowUseCase<List<Transaction>>() {

    override fun invoke(): Flow<List<Transaction>> =
        transactionRepository.getAllTransactions()
}
