package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.ObservableUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

public data class DateRange(
    val from: Long,
    val to: Long,
)

public class GetTransactionsByDateRangeUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ObservableUseCase<DateRange, List<Transaction>>(dispatcher) {
    override fun execute(params: DateRange): Flow<List<Transaction>> =
        transactionRepository.getTransactionsByDateRange(params.from, params.to)
}
