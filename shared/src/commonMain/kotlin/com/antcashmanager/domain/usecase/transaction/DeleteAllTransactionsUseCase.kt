package com.antcashmanager.domain.usecase.transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.NoParamsUseCase
class DeleteAllTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
) : NoParamsUseCase<Unit>() {
    override suspend fun invoke() =
        transactionRepository.deleteAllTransactions()
}
