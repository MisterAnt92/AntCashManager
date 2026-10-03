package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.MealVoucherSummary
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public class GetMealVoucherSummaryUseCase(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<GetMealVoucherSummaryUseCase.Params, MealVoucherSummary?>(dispatcher) {

    public data class Params(
        val transactions: List<Transaction>,
        val voucherValue: Double,
    )

    override suspend fun execute(params: Params): MealVoucherSummary? {
        val mealVoucherTxs = params.transactions.filter { it.paymentType == PaymentType.MEAL_VOUCHERS }

        if (mealVoucherTxs.isEmpty()) return null

        val received = mealVoucherTxs
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.mealVoucherCount }

        val used = mealVoucherTxs
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.mealVoucherCount }

        if (received == 0 && used == 0) return null

        val remaining = received - used
        val remainingValue = remaining * params.voucherValue
        val usedValue = used * params.voucherValue

        return MealVoucherSummary(
            received = received,
            used = used,
            remaining = remaining,
            voucherValue = params.voucherValue,
            remainingValue = remainingValue,
            usedValue = usedValue,
        )
    }
}
