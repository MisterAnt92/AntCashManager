package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.BaseUseCase

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository
) : BaseUseCase<Transaction, Unit>() {

    override suspend fun invoke(params: Transaction) =
        transactionRepository.deleteTransaction(params)
}
