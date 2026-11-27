package com.antcashmanager.android.ui.screen.charts

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.R
import android.os.Bundle
import com.antcashmanager.android.analytics.AnalyticsConstants.Events
import com.antcashmanager.android.analytics.AnalyticsConstants.Params
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.analytics.tracker.SegmentationTracker
import com.antcashmanager.android.ui.base.AndroidBaseViewModel
import com.antcashmanager.android.ui.screen.charts.view.ChartDetailsData
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.antcashmanager.architecture.ErrorState
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
    private val analyticsManager: AnalyticsManager,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AndroidBaseViewModel<ChartEvent, ChartsState>(ChartsState(), dispatcher) {
    constructor(
        transactionRepository: TransactionRepository,
        settingsRepository: SettingsRepository,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        performanceTracker: PerformanceTracker,
        segmentationTracker: SegmentationTracker,
        analyticsManager: AnalyticsManager,
    ) : this(
        getTransactionsByDateRangeUseCase =
            GetTransactionsByDateRangeUseCase(
                transactionRepository = transactionRepository,
                dispatcher = dispatcher,
            ),
        getChartsDateFilterStateUseCase = GetChartsDateFilterStateUseCase(settingsRepository),
        setChartsDateFilterStateUseCase =
            SetChartsDateFilterStateUseCase(
                settingsRepository = settingsRepository,
                dispatcher = dispatcher,
            ),
        performanceTracker = performanceTracker,
        segmentationTracker = segmentationTracker,
        settingsRepository = settingsRepository,
        analyticsManager = analyticsManager,
        dispatcher = dispatcher,
    )

    private val _dateRange = MutableStateFlow(getDefaultDateRange())
    val dateRange: StateFlow<DateRange> = _dateRange.asStateFlow()
    private val _selectedPresetIndex = MutableStateFlow(1)
    val selectedPresetIndex: StateFlow<Int> = _selectedPresetIndex.asStateFlow()

    // Chart details for tap-to-details interaction
    private val _selectedChartDetails = MutableStateFlow<ChartDetailsData?>(null)
    val selectedChartDetails: StateFlow<ChartDetailsData?> = _selectedChartDetails.asStateFlow()

    // Locale tag supplied by the Screen via UpdateLocale event; avoids JVM-default locale bug.
    private val _localeTag = MutableStateFlow(java.util.Locale.getDefault().toLanguageTag())

    val chartData: StateFlow<ChartData> =
        kotlinx.coroutines.flow.combine(_dateRange, _localeTag) { range, localeTag ->
            range to localeTag
        }
            .flatMapLatest { (range, localeTag) ->
                val prevRange = previousPeriodRange(range)
                combine(
                    getTransactionsByDateRangeUseCase(range),
                    getTransactionsByDateRangeUseCase(prevRange),
                ) { currentResult, prevResult ->
                    val current  = currentResult.getOrElse { emptyList() }.withCorrectAmounts()
                    val previous = prevResult.getOrElse { emptyList() }.withCorrectAmounts()
                    buildChartData(current, previous, localeTag)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ChartData(),
            )

    val chartsZoomEnabled: StateFlow<Boolean> =
        settingsRepository
            .getChartsZoomEnabled()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

    val chartsCardOrder: StateFlow<String> =
        settingsRepository
            .getChartCardsOrder()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ChartsConstant.DEFAULT_CHARTS_CARDS_ORDER,
            )

    // ── Unified State (FASE 5: Error Feedback UX) ──
    override val state: StateFlow<ChartsState> =
        kotlinx.coroutines.flow.combine(
            _dateRange,
            _selectedPresetIndex,
            _selectedChartDetails,
            chartData,
            chartsZoomEnabled,
            chartsCardOrder,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val values = args
            ChartsState(
                dateRange = values[0] as DateRange,
                selectedPresetIndex = values[1] as Int,
                selectedChartDetails = values[2] as ChartDetailsData?,
                chartData = values[3] as ChartData,
                errorState = ErrorState(),
                isLoading = false,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ChartsState(
                dateRange = getDefaultDateRange(),
                selectedPresetIndex = 1,
                isLoading = true,
            ),
        )

    init {
        observeSavedDateFilter()
    }

    override fun handleEvent(event: ChartEvent) {
        logDebug("Event: $event")
        when (event) {
            is ChartEvent.SetDateRangeEvent -> setDateRange(event.from, event.to)
            is ChartEvent.SetPresetRangeEvent -> setPresetRange(event.preset)
            is ChartEvent.SelectChartCategoryEvent ->
                selectChartCategory(
                    event.categoryName,
                    event.amount,
                    event.colorHex,
                    event.isExpense,
                )

            is ChartEvent.ClearChartSelectionEvent -> clearChartSelection()
            is ChartEvent.RetryLastOperationEvent -> logInfo("Retry requested")
            is ChartEvent.SetChartCardsOrderEvent -> setChartCardsOrder(event.order)
            is ChartEvent.UpdateLocaleEvent -> _localeTag.value = event.languageTag
            is ChartEvent.LogChartLoadingCompletedEvent -> analyticsManager.logEvent(
                Events.CHART_LOADING_COMPLETED,
                Bundle().apply { putInt(Params.DATA_POINTS, event.dataPoints) },
            )
            is ChartEvent.LogChartItemClickedEvent -> analyticsManager.logEvent(
                Events.CHART_ITEM_CLICKED,
                Bundle().apply {
                    putString(Params.CHART_TYPE, event.chartType)
                    putString(Params.CATEGORY, event.category)
                },
            )
            is ChartEvent.LogPresetFilterAppliedEvent -> {
                analyticsManager.logEvent(Events.CHART_DATE_FILTER_CHANGED)
                analyticsManager.logEvent(
                    Events.FILTER_COMBINATION_APPLIED,
                    Bundle().apply {
                        putInt(Params.FILTER_COUNT, 1)
                        putString(Params.TYPES, "date")
                    },
                )
            }
            is ChartEvent.LogChartSharedEvent -> analyticsManager.logEvent(Events.CHART_SHARED)
            is ChartEvent.LogCustomDateFilterAppliedEvent -> {
                analyticsManager.logEvent(Events.CHART_CUSTOM_DATE_RANGE_SET)
                analyticsManager.logEvent(
                    Events.FILTER_COMBINATION_APPLIED,
                    Bundle().apply {
                        putInt(Params.FILTER_COUNT, 1)
                        putString(Params.TYPES, "date")
                    },
                )
            }
        }
    }

    private fun setChartCardsOrder(order: String) {
        viewModelScope.launch {
            settingsRepository.setChartCardsOrder(order)
        }
    }

    fun setDateRange(
        from: Long,
        to: Long,
    ) {
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
            getChartsDateFilterStateUseCase()
                .mapNotNull { result: Result<SavedDateFilter> -> result.getOrNull() }
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

    private fun previousPeriodRange(range: DateRange): DateRange {
        val duration = range.to - range.from
        return DateRange(from = maxOf(0L, range.from - duration), to = range.from - 1L)
    }

    private fun buildChartData(
        transactions: List<Transaction>,
        previousTransactions: List<Transaction> = emptyList(),
        localeTag: String = java.util.Locale.getDefault().toLanguageTag(),
    ): ChartData {
        val startTime = System.currentTimeMillis()
        logDebug("Building chart data from ${transactions.size} transactions")

        val incomeTransactions = transactions.filter { it.type == TransactionType.INCOME }
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

        logDebug("Income transactions: ${incomeTransactions.size}, Expense transactions: ${expenseTransactions.size}")

        val incomeByCategory =
            incomeTransactions
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        val expenseByCategory =
            expenseTransactions
                .groupBy { it.category }
                .mapValues { (_, txs) -> abs(txs.sumOf { it.amount }) } // Use absolute value for pie chart

        val incomeCountByCategory =
            incomeTransactions
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.size }

        val expenseCountByCategory =
            expenseTransactions
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.size }

        val totalIncome = incomeByCategory.values.sum()
        val totalExpense =
            expenseByCategory.values.sum() // Use absolute value (already absolute from map)

        logDebug("Total Income: $totalIncome, Total Expense: $totalExpense")

        // Build monthly aggregation
        val cal = Calendar.getInstance()
        val monthlyMap = mutableMapOf<String, Pair<Double, Double>>()
        // Usa DateFormatSymbols per ottenere i nomi dei mesi localizzati automaticamente
        val monthNames = java.text.DateFormatSymbols(java.util.Locale.forLanguageTag(localeTag)).shortMonths.take(12).toTypedArray()

        transactions.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            val key = "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR) % 100}"
            val current = monthlyMap.getOrDefault(key, 0.0 to 0.0)
            monthlyMap[key] =
                when (tx.type) {
                    TransactionType.INCOME -> (current.first + tx.amount) to current.second
                    TransactionType.EXPENSE -> current.first to (current.second + abs(tx.amount)) // Use absolute value
                }
        }

        val monthlyData =
            monthlyMap.entries
                .sortedBy { entry ->
                    val parts = entry.key.split(" ")
                    val monthIdx = monthNames.indexOf(parts[0])
                    val year = parts[1].toIntOrNull() ?: 0
                    year * 100 + monthIdx
                }.map { (label, amounts) ->
                    MonthlyAmount(label, amounts.first, amounts.second) // Both are positive
                }

        logDebug("Monthly data: ${monthlyData.map { "${it.label}: income=${it.income}, expense=${it.expense}" }}")

        // Build yearly aggregation
        val yearlyMap = mutableMapOf<Int, Pair<Double, Double>>()

        transactions.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            val year = cal.get(Calendar.YEAR)
            val current = yearlyMap.getOrDefault(year, 0.0 to 0.0)
            yearlyMap[year] =
                when (tx.type) {
                    TransactionType.INCOME -> (current.first + tx.amount) to current.second
                    TransactionType.EXPENSE -> current.first to (current.second + abs(tx.amount)) // Use absolute value
                }
        }

        val yearlyData =
            yearlyMap.entries
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
        val paymentTypeBreakdown =
            transactions
                .groupBy { it.paymentType }
                .mapValues { (_, txs) -> abs(txs.sumOf { it.amount }) }
                .filterValues { it != 0.0 }

        // Calculate expense by weekday (1=Mon...7=Sun per EU convention)
        // Returns total expense amount per weekday, not average
        val expenseByWeekday =
            expenseTransactions
                .groupBy { tx ->
                    cal.timeInMillis = tx.timestamp
                    // Calendar.DAY_OF_WEEK: 1=Sun, 2=Mon, ..., 7=Sat
                    // Convert to EU convention: 1=Mon, 2=Tue, ..., 7=Sun
                    val calendarDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    if (calendarDayOfWeek == 1) 7 else calendarDayOfWeek - 1
                }.mapValues { (_, txs) ->
                    if (txs.isNotEmpty()) {
                        abs(txs.sumOf { it.amount })
                    } else {
                        0.0
                    }
                }

        // Calculate daily timeline (sorted by date, only expenses)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val dailyMap = mutableMapOf<String, Double>()
        expenseTransactions.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            val dateLabel = dateFormat.format(cal.time)
            val current = dailyMap.getOrDefault(dateLabel, 0.0)
            dailyMap[dateLabel] = current + abs(tx.amount)
        }

        val dailyTimeline =
            dailyMap.entries
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
                val avgExpense =
                    maxExpenseCategory.value /
                        kotlin.math.max(1, expenseCountByCategory[maxExpenseCategory.key] ?: 1)
                segmentationTracker.trackSpendingPatternDetected("category_focus", maxExpenseCategory.key, avgExpense)
            }
        }

        // Track income sources
        if (incomeByCategory.isNotEmpty()) {
            val maxIncomeCategory = incomeByCategory.maxByOrNull { it.value }
            if (maxIncomeCategory != null) {
                val avgIncome =
                    maxIncomeCategory.value /
                        kotlin.math.max(1, incomeCountByCategory[maxIncomeCategory.key] ?: 1)
                segmentationTracker.trackIncomeSourceTracking(maxIncomeCategory.key, "monthly", avgIncome)
            }
        }

        val prevIncome = previousTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val prevExpense = previousTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { abs(it.amount) }

        return ChartData(
            incomeByCategory = incomeByCategory,
            expenseByCategory = expenseByCategory,
            incomeCountByCategory = incomeCountByCategory,
            expenseCountByCategory = expenseCountByCategory,
            totalIncome = totalIncome,
            totalExpense = totalExpense, // Positive absolute value (sum of |expense| per category)
            monthlyData = monthlyData,
            yearlyData = yearlyData,
            paymentTypeBreakdown = paymentTypeBreakdown,
            dailyTimeline = dailyTimeline,
            expenseByWeekday = expenseByWeekday,
            previousPeriodIncome = prevIncome,
            previousPeriodExpense = prevExpense,
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

        val percentage =
            if (totalAmount != 0.0) {
                ((abs(amount) / totalAmount) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

        // Calculate trend based on transaction count for this category
        val transactionCount =
            when {
                isExpense -> currentData.expenseCountByCategory[categoryName] ?: 0
                else -> currentData.incomeCountByCategory[categoryName] ?: 0
            }

        val details =
            ChartDetailsData(
                categoryName = categoryName,
                amount = amount,
                percentage = percentage,
                colorHex = colorHex,
                transactionCount = transactionCount,
                trend = if (isExpense) chartData.value.expenseTrend else chartData.value.incomeTrend,
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

enum class RangePreset(
    @StringRes val labelResId: Int,
) {
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
