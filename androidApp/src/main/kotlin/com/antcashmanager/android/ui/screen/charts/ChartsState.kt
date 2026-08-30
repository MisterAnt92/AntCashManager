package com.antcashmanager.android.ui.screen.charts

import com.antcashmanager.android.ui.base.ErrorState
import com.antcashmanager.android.ui.screen.charts.view.ChartDetailsData
import com.antcashmanager.domain.model.SavedDateFilter

/**
 * Stato UI completo per la schermata dei grafici.
 *
 * Contiene:
 * - chartData: aggregazioni income/expense/monthly/yearly/daily
 * - dateRange: range selezionato
 * - selectedChartDetails: categoria selezionata per bottom sheet
 * - errorState: gestione errori centralizzata
 *
 * UDF Pattern:
 * - state: ChartsState (this)
 * - events: ChartEvent (sealed class)
 * - viewModel: ChartsViewModel (onEvent handler)
 */
data class ChartsState(
    val chartData: ChartData = ChartData(),
    val selectedChartDetails: ChartDetailsData? = null,
    val errorState: ErrorState = ErrorState(),
    val isLoading: Boolean = false,
)
