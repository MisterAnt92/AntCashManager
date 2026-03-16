package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.BaseUseCase

class InsertTransactionUseCase(
    private val transactionRepository: TransactionRepository
) : BaseUseCase<Transaction, Long>() {

    override suspend fun invoke(params: Transaction): Long =
        transactionRepository.insertTransaction(params)
}
