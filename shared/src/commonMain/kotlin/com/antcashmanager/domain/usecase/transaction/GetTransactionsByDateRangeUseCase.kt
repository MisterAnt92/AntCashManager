package com.antcashmanager.domain.usecase.transaction
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
data class DateRange(val from: Long, val to: Long)
class GetTransactionsByDateRangeUseCase(
    private val transactionRepository: TransactionRepository,
) : FlowUseCase<DateRange, List<Transaction>>() {
    override fun invoke(params: DateRange): Flow<List<Transaction>> =
        transactionRepository.getTransactionsByDateRange(params.from, params.to)
}
