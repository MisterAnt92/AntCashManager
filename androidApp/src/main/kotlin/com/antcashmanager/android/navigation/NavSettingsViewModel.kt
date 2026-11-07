package com.antcashmanager.android.navigation

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Espone come [state] le impostazioni globali che il NavHost consuma (formato valuta, schede grafici,
 * tutorial completato, mascheramento importi, consenso analytics).
 * Unico punto di accesso per settare il consenso analytics tramite [onEvent].
 */
class NavSettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel<NavSettingsEvent>() {

    val state: StateFlow<NavSettingsState> = combine(
        combine(
            settingsRepository.getShowCharts(),
            settingsRepository.getIsTutorialCompleted().map { it as Boolean? }.onStart { emit(null) },
            settingsRepository.getCurrencySymbol(),
        ) { showCharts, tutorialDone, currency ->
            Triple(showCharts, tutorialDone, currency)
        },
        combine(
            settingsRepository.getDecimalDigits(),
            settingsRepository.getDecimalSeparator(),
            settingsRepository.getThousandsSeparator(),
        ) { digits, decSep, thoSep ->
            Triple(digits, decSep, thoSep)
        },
        combine(
            settingsRepository.getMaskAmounts(),
            settingsRepository.getAnalyticsConsent(),
        ) { mask, consent ->
            Pair(mask, consent)
        },
    ) { a, b, c ->
        NavSettingsState(
            showCharts = a.first,
            isTutorialCompleted = a.second,
            currencySymbol = a.third,
            decimalDigits = b.first,
            decimalSeparator = b.second,
            thousandsSeparator = b.third,
            maskAmounts = c.first,
            analyticsConsent = c.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NavSettingsState())

    override fun onEvent(event: NavSettingsEvent) {
        when (event) {
            is NavSettingsEvent.SetAnalyticsConsent -> setAnalyticsConsent(event.granted)
        }
    }

    private fun setAnalyticsConsent(granted: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAnalyticsConsent(granted)
        }
    }
}
