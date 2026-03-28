package com.antcashmanager.android.ui.screen.home.charts

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.DateRange
import com.antcashmanager.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModel(
    transactionRepository: TransactionRepository,
) : ViewModel() {

    private val getTransactionsByDateRangeUseCase =
        GetTransactionsByDateRangeUseCase(transactionRepository)

    private val calendar = Calendar.getInstance()

    private val _dateRange = MutableStateFlow(getDefaultDateRange())
    val dateRange: StateFlow<DateRange> = _dateRange.asStateFlow()

    val chartData: StateFlow<ChartData> = _dateRange
        .flatMapLatest { range ->
            getTransactionsByDateRangeUseCase(range).map { transactions ->
                buildChartData(transactions)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChartData(),
        )

    fun setDateRange(from: Long, to: Long) {
        Logger.d("ChartsViewModel") { "Setting date range: $from - $to" }
        _dateRange.value = DateRange(from, to)
    }

    fun setPresetRange(preset: RangePreset) {
        val now = Calendar.getInstance()
        val from = Calendar.getInstance()

        when (preset) {
            RangePreset.WEEK -> from.add(Calendar.DAY_OF_YEAR, -7)
            RangePreset.MONTH -> from.add(Calendar.MONTH, -1)
            RangePreset.THREE_MONTHS -> from.add(Calendar.MONTH, -3)
            RangePreset.SIX_MONTHS -> from.add(Calendar.MONTH, -6)
            RangePreset.YEAR -> from.add(Calendar.YEAR, -1)
            RangePreset.ALL -> from.set(2000, 0, 1)
        }

        from.set(Calendar.HOUR_OF_DAY, 0)
        from.set(Calendar.MINUTE, 0)
        from.set(Calendar.SECOND, 0)
        from.set(Calendar.MILLISECOND, 0)

        now.set(Calendar.HOUR_OF_DAY, 23)
        now.set(Calendar.MINUTE, 59)
        now.set(Calendar.SECOND, 59)
        now.set(Calendar.MILLISECOND, 999)

        _dateRange.value = DateRange(from.timeInMillis, now.timeInMillis)
    }

    private fun getDefaultDateRange(): DateRange {
        val now = Calendar.getInstance()
        val from = Calendar.getInstance()
        from.add(Calendar.MONTH, -1)
        from.set(Calendar.HOUR_OF_DAY, 0)
        from.set(Calendar.MINUTE, 0)
        from.set(Calendar.SECOND, 0)
        from.set(Calendar.MILLISECOND, 0)
        now.set(Calendar.HOUR_OF_DAY, 23)
        now.set(Calendar.MINUTE, 59)
        now.set(Calendar.SECOND, 59)
        now.set(Calendar.MILLISECOND, 999)
        return DateRange(from.timeInMillis, now.timeInMillis)
    }

    private fun buildChartData(transactions: List<Transaction>): ChartData {
        val incomeByCategory = transactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        val expenseByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        val totalIncome = incomeByCategory.values.sum()
        val totalExpense = expenseByCategory.values.sum()

        // Build monthly aggregation
        val cal = Calendar.getInstance()
        val monthlyMap = mutableMapOf<String, Pair<Double, Double>>()
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        )

        transactions.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            val key = "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR) % 100}"
            val current = monthlyMap.getOrDefault(key, 0.0 to 0.0)
            monthlyMap[key] = when (tx.type) {
                TransactionType.INCOME -> (current.first + tx.amount) to current.second
                TransactionType.EXPENSE -> current.first to (current.second + tx.amount)
            }
        }

        val monthlyData = monthlyMap.entries
            .sortedBy { entry ->
                val parts = entry.key.split(" ")
                val monthIdx = monthNames.indexOf(parts[0])
                val year = parts[1].toIntOrNull() ?: 0
                year * 100 + monthIdx
            }
            .map { (label, amounts) ->
                MonthlyAmount(label, amounts.first, amounts.second)
            }

        return ChartData(
            incomeByCategory = incomeByCategory,
            expenseByCategory = expenseByCategory,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            monthlyData = monthlyData,
        )
    }
}

enum class RangePreset(@StringRes val labelResId: Int) {
    WEEK(R.string.range_week),
    MONTH(R.string.range_month),
    THREE_MONTHS(R.string.range_three_months),
    SIX_MONTHS(R.string.range_six_months),
    YEAR(R.string.range_year),
    ALL(R.string.range_all),
}

