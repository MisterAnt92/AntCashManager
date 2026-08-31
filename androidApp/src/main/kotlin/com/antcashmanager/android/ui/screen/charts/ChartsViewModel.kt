package com.antcashmanager.android.ui.screen.charts

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.PerformanceTracker
import com.antcashmanager.android.analytics.SegmentationTracker
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.android.ui.screen.charts.view.ChartDetailsData
import com.antcashmanager.android.ui.screen.charts.view.TrendDirection
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModel(
    private val getTransactionsByDateRangeUseCase: GetTransactionsByDateRangeUseCase,
    private val getChartsDateFilterStateUseCase: GetChartsDateFilterStateUseCase,
    private val setChartsDateFilterStateUseCase: SetChartsDateFilterStateUseCase,
    private val performanceTracker: PerformanceTracker,
    private val segmentationTracker: SegmentationTracker,
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseViewModel<ChartEvent>(dispatcher) {

    constructor(
        transactionRepository: TransactionRepository,
        settingsRepository: SettingsRepository,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        performanceTracker: PerformanceTracker,
        segmentationTracker: SegmentationTracker,
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
        performanceTracker = performanceTracker,
        segmentationTracker = segmentationTracker,
        settingsRepository = settingsRepository,
        dispatcher = dispatcher,
    )

    private val _dateRange = MutableStateFlow(getDefaultDateRange())
    val dateRange: StateFlow<DateRange> = _dateRange.asStateFlow()
    private val _selectedPresetIndex = MutableStateFlow(1)
    val selectedPresetIndex: StateFlow<Int> = _selectedPresetIndex.asStateFlow()

    // Chart details for tap-to-details interaction
    private val _selectedChartDetails = MutableStateFlow<ChartDetailsData?>(null)
    val selectedChartDetails: StateFlow<ChartDetailsData?> = _selectedChartDetails.asStateFlow()

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

    val chartsZoomEnabled: StateFlow<Boolean> = settingsRepository.getChartsZoomEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    init {
        observeSavedDateFilter()
    }

    override fun onEvent(event: ChartEvent) {
        logDebug("Event: $event")
        when (event) {
            is ChartEvent.SetDateRange -> setDateRange(event.from, event.to)
            is ChartEvent.SetPresetRange -> setPresetRange(event.preset)
            is ChartEvent.SelectChartCategory -> selectChartCategory(
                event.categoryName,
                event.amount,
                event.colorHex,
                event.isExpense
            )
            is ChartEvent.ClearChartSelection -> clearChartSelection()
            is ChartEvent.RetryLastOperation -> logInfo("Retry requested")
        }
    }

    private fun setDateRange(from: Long, to: Long) {
        val normalizedFrom = minOf(from, to)
        val normalizedTo = maxOf(from, to)
        logDebug("Setting date range: $normalizedFrom - $normalizedTo")
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

    private fun setPresetRange(preset: RangePreset) {
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
                logError("Failed to persist charts date filter: ${error.message}", error)
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
        val startTime = System.currentTimeMillis()
        logDebug("Building chart data from ${transactions.size} transactions")

        val incomeTransactions = transactions.filter { it.type == TransactionType.INCOME }
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

        logDebug("Income transactions: ${incomeTransactions.size}, Expense transactions: ${expenseTransactions.size}")

        val incomeByCategory = incomeTransactions
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        val expenseByCategory = expenseTransactions
            .groupBy { it.category }
            .mapValues { (_, txs) -> kotlin.math.abs(txs.sumOf { it.amount }) } // Use absolute value for pie chart

        val totalIncome = incomeByCategory.values.sum()
        val totalExpense =
            expenseByCategory.values.sum() // Use absolute value (already absolute from map)

        logDebug("Total Income: $totalIncome, Total Expense: $totalExpense")

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

        logDebug("Monthly data: ${monthlyData.map { "${it.label}: income=${it.income}, expense=${it.expense}" }}")

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

        logDebug("Yearly data: ${yearlyData.map { "${it.label}: income=${it.income}, expense=${it.expense}" }}")

        // Ripartizione per metodo di pagamento (entrate + uscite nette, come HomeViewModel).
        val paymentTypeBreakdown = transactions
            .groupBy { it.paymentType }
            .mapValues { (_, txs) -> kotlin.math.abs(txs.sumOf { it.amount }) }
            .filterValues { it != 0.0 }

        // Calculate expense by weekday (1=Mon...7=Sun per EU convention)
        // Returns total expense amount per weekday, not average
        val expenseByWeekday = expenseTransactions
            .groupBy { tx ->
                cal.timeInMillis = tx.timestamp
                // Calendar.DAY_OF_WEEK: 1=Sun, 2=Mon, ..., 7=Sat
                // Convert to EU convention: 1=Mon, 2=Tue, ..., 7=Sun
                val calendarDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                if (calendarDayOfWeek == 1) 7 else calendarDayOfWeek - 1
            }
            .mapValues { (_, txs) ->
                if (txs.isNotEmpty()) {
                    kotlin.math.abs(txs.sumOf { it.amount })
                } else 0.0
            }

        // Calculate daily timeline (sorted by date, only expenses)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val dailyMap = mutableMapOf<String, Double>()
        expenseTransactions.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            val dateLabel = dateFormat.format(cal.time)
            val current = dailyMap.getOrDefault(dateLabel, 0.0)
            dailyMap[dateLabel] = current + kotlin.math.abs(tx.amount)
        }

        val dailyTimeline = dailyMap.entries
            .sortedBy { it.key }
            .map { (dateLabel, expense) ->
                DailyAmount(dateLabel = dateLabel, expense = expense)
            }

        val duration = System.currentTimeMillis() - startTime
        performanceTracker.trackChartRenderingTime(duration, transactions.size)

        // Track spending patterns
        if (expenseByCategory.isNotEmpty()) {
            val maxExpenseCategory = expenseByCategory.maxByOrNull { it.value }
            if (maxExpenseCategory != null) {
                val avgExpense = maxExpenseCategory.value / kotlin.math.max(1, expenseTransactions.count { it.category == maxExpenseCategory.key })
                segmentationTracker.trackSpendingPatternDetected("category_focus", maxExpenseCategory.key, avgExpense)
            }
        }

        // Track income sources
        if (incomeByCategory.isNotEmpty()) {
            val maxIncomeCategory = incomeByCategory.maxByOrNull { it.value }
            if (maxIncomeCategory != null) {
                val avgIncome = maxIncomeCategory.value / kotlin.math.max(1, incomeTransactions.count { it.category == maxIncomeCategory.key })
                segmentationTracker.trackIncomeSourceTracking(maxIncomeCategory.key, "monthly", avgIncome)
            }
        }

        return ChartData(
            incomeByCategory = incomeByCategory,
            expenseByCategory = expenseByCategory,
            totalIncome = totalIncome,
            totalExpense = totalExpense, // Will be negative
            monthlyData = monthlyData,
            yearlyData = yearlyData,
            paymentTypeBreakdown = paymentTypeBreakdown,
            dailyTimeline = dailyTimeline,
            expenseByWeekday = expenseByWeekday,
        )
    }

    /**
     * Select a chart category to show details in bottom sheet.
     *
     * @param categoryName The category to select
     * @param amount The total amount for this category
     * @param colorHex The color code for visualization
     * @param isExpense Whether this is an expense category
     */
    private fun selectChartCategory(
        categoryName: String,
        amount: Double,
        colorHex: Long,
        isExpense: Boolean = true,
    ) {
        val currentData = chartData.value
        val totalAmount = if (isExpense) currentData.totalExpense else currentData.totalIncome

        val percentage = if (totalAmount != 0.0) {
            ((abs(amount) / totalAmount) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // Calculate trend based on transaction count for this category
        val transactionCount = when {
            isExpense -> currentData.expenseByCategory[categoryName]?.let { 1 } ?: 0
            else -> currentData.incomeByCategory[categoryName]?.let { 1 } ?: 0
        }

        val details = ChartDetailsData(
            categoryName = categoryName,
            amount = amount,
            percentage = percentage,
            colorHex = colorHex,
            transactionCount = transactionCount,
            trend = TrendDirection.NEUTRAL, // Can be enhanced with trend analysis
        )

        _selectedChartDetails.value = details
        logDebug("Selected chart category: $categoryName (${abs(amount)}, $percentage%)")
    }

    /**
     * Clear the currently selected chart details.
     */
    private fun clearChartSelection() {
        _selectedChartDetails.value = null
        logDebug("Cleared chart selection")
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
