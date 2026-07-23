package com.antcashmanager.android.ui.screen.charts

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.util.withCorrectAmounts
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.settings.GetChartsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetChartsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.transaction.DateRange
import com.antcashmanager.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModel(
    private val getTransactionsByDateRangeUseCase: GetTransactionsByDateRangeUseCase,
    private val getChartsDateFilterStateUseCase: GetChartsDateFilterStateUseCase,
    private val setChartsDateFilterStateUseCase: SetChartsDateFilterStateUseCase,
) : ViewModel() {

    constructor(
        transactionRepository: TransactionRepository,
        settingsRepository: SettingsRepository,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) : this(
        getTransactionsByDateRangeUseCase = GetTransactionsByDateRangeUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        ),
        getChartsDateFilterStateUseCase = GetChartsDateFilterStateUseCase(settingsRepository),
        setChartsDateFilterStateUseCase = SetChartsDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        ),
    )

    private val _dateRange = MutableStateFlow(getDefaultDateRange())
    val dateRange: StateFlow<DateRange> = _dateRange.asStateFlow()
    private val _selectedPresetIndex = MutableStateFlow(1)
    val selectedPresetIndex: StateFlow<Int> = _selectedPresetIndex.asStateFlow()

    val chartData: StateFlow<ChartData> = _dateRange
        .flatMapLatest { range ->
            getTransactionsByDateRangeUseCase(range).map { result ->
                // Apply amount correction for EXPENSE transactions
                buildChartData(result.getOrElse { emptyList() }.withCorrectAmounts())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChartData(),
        )

    init {
        observeSavedDateFilter()
    }

    fun setDateRange(from: Long, to: Long) {
        val normalizedFrom = minOf(from, to)
        val normalizedTo = maxOf(from, to)
        Logger.d(tag = "ChartsViewModel") { "Setting date range: $normalizedFrom - $normalizedTo" }
        _selectedPresetIndex.value = SavedDateFilter.CUSTOM_PRESET_INDEX
        _dateRange.value = DateRange(normalizedFrom, normalizedTo)
        persistDateFilter(
            SavedDateFilter(
                presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
                from = normalizedFrom,
                to = normalizedTo,
            ),
        )
    }

    fun setPresetRange(preset: RangePreset) {
        val range = buildPresetDateRange(preset)
        _selectedPresetIndex.value = preset.ordinal
        _dateRange.value = range
        persistDateFilter(
            SavedDateFilter(
                presetIndex = preset.ordinal,
                from = range.from,
                to = range.to,
            ),
        )
    }

    private fun observeSavedDateFilter() {
        viewModelScope.launch {
            getChartsDateFilterStateUseCase().mapNotNull { result: Result<SavedDateFilter> -> result.getOrNull() }
                .collect { savedFilter: SavedDateFilter ->
                    _selectedPresetIndex.value = savedFilter.presetIndex
                    _dateRange.value = DateRange(savedFilter.from, savedFilter.to)
                }
        }
    }

    private fun buildPresetDateRange(preset: RangePreset): DateRange {
        val now = Calendar.getInstance()
        val from = Calendar.getInstance()

        when (preset) {
            RangePreset.WEEK -> from.add(Calendar.DAY_OF_YEAR, -7)
            RangePreset.MONTH -> from.add(Calendar.MONTH, -1)
            RangePreset.THREE_MONTHS -> from.add(Calendar.MONTH, -3)
            RangePreset.SIX_MONTHS -> from.add(Calendar.MONTH, -6)
            RangePreset.YEAR -> from.add(Calendar.YEAR, -1)
            RangePreset.TWO_YEARS -> from.add(Calendar.YEAR, -2)
            RangePreset.THREE_YEARS -> from.add(Calendar.YEAR, -3)
            RangePreset.FIVE_YEARS -> from.add(Calendar.YEAR, -5)
            RangePreset.SIX_YEARS -> from.add(Calendar.YEAR, -6)
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

        return DateRange(from.timeInMillis, now.timeInMillis)
    }

    private fun persistDateFilter(filter: SavedDateFilter) {
        viewModelScope.launch {
            val result = setChartsDateFilterStateUseCase(filter)
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                Logger.e(throwable = error, tag = "ChartsViewModel") {
                    "Failed to persist charts date filter: ${error.message}"
                }
            }
        }
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
        Logger.d(tag = "ChartsViewModel") { "Building chart data from ${transactions.size} transactions" }

        val incomeTransactions = transactions.filter { it.type == TransactionType.INCOME }
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

        Logger.d(tag = "ChartsViewModel") { "Income transactions: ${incomeTransactions.size}, Expense transactions: ${expenseTransactions.size}" }

        val incomeByCategory = incomeTransactions
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        val expenseByCategory = expenseTransactions
            .groupBy { it.category }
            .mapValues { (_, txs) -> kotlin.math.abs(txs.sumOf { it.amount }) } // Use absolute value for pie chart

        val totalIncome = incomeByCategory.values.sum()
        val totalExpense =
            expenseByCategory.values.sum() // Use absolute value (already absolute from map)

        Logger.d(tag = "ChartsViewModel") { "Total Income: $totalIncome, Total Expense: $totalExpense" }

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
                TransactionType.EXPENSE -> current.first to (current.second + kotlin.math.abs(tx.amount)) // Use absolute value
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
                MonthlyAmount(label, amounts.first, amounts.second) // Both are positive
            }

        Logger.d(tag = "ChartsViewModel") { "Monthly data: ${monthlyData.map { "${it.label}: income=${it.income}, expense=${it.expense}" }}" }

        // Build yearly aggregation
        val yearlyMap = mutableMapOf<Int, Pair<Double, Double>>()

        transactions.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            val year = cal.get(Calendar.YEAR)
            val current = yearlyMap.getOrDefault(year, 0.0 to 0.0)
            yearlyMap[year] = when (tx.type) {
                TransactionType.INCOME -> (current.first + tx.amount) to current.second
                TransactionType.EXPENSE -> current.first to (current.second + kotlin.math.abs(tx.amount)) // Use absolute value
            }
        }

        val yearlyData = yearlyMap.entries
            .sortedBy { it.key }
            .map { (year, amounts) ->
                YearlyAmount(
                    year = year,
                    label = year.toString(),
                    income = amounts.first,
                    expense = amounts.second, // Positive
                )
            }

        Logger.d(tag = "ChartsViewModel") { "Yearly data: ${yearlyData.map { "${it.label}: income=${it.income}, expense=${it.expense}" }}" }

        // Ripartizione per metodo di pagamento (entrate + uscite nette, come HomeViewModel).
        val paymentTypeBreakdown = transactions
            .groupBy { it.paymentType }
            .mapValues { (_, txs) -> kotlin.math.abs(txs.sumOf { it.amount }) }
            .filterValues { it != 0.0 }

        return ChartData(
            incomeByCategory = incomeByCategory,
            expenseByCategory = expenseByCategory,
            totalIncome = totalIncome,
            totalExpense = totalExpense, // Will be negative
            monthlyData = monthlyData,
            yearlyData = yearlyData,
            paymentTypeBreakdown = paymentTypeBreakdown,
        )
    }
}

enum class RangePreset(@StringRes val labelResId: Int) {
    WEEK(R.string.range_week),
    MONTH(R.string.range_month),
    THREE_MONTHS(R.string.range_three_months),
    SIX_MONTHS(R.string.range_six_months),
    YEAR(R.string.range_year),
    TWO_YEARS(R.string.range_two_years),
    THREE_YEARS(R.string.range_three_years),
    FIVE_YEARS(R.string.range_five_years),
    SIX_YEARS(R.string.range_six_years),
    ALL(R.string.range_all),
}
