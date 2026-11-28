package com.antcashmanager.android.ui.screen.tutorial

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.base.AndroidBaseViewModel
import com.antcashmanager.domain.usecase.settings.SetTutorialCompletedUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Manages reveal-based tutorial progression.
 *
 * On [TutorialEvent.StartTutorial]: checks whether the user already has transactions
 * (upgrade safeguard). If they do, the tutorial is silently marked completed.
 * Otherwise, the tutorial starts at step 0.
 */
class TutorialViewModel(
    private val setTutorialCompletedUseCase: SetTutorialCompletedUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val analyticsManager: AnalyticsManager,
) : AndroidBaseViewModel<TutorialEvent, TutorialState>(TutorialState()) {

    override fun handleEvent(event: TutorialEvent) {
        when (event) {
            TutorialEvent.StartTutorial -> startTutorial(forceReplay = false)
            TutorialEvent.ForceStartTutorial -> startTutorial(forceReplay = true)
            TutorialEvent.NextStep -> nextStep()
            TutorialEvent.PreviousStep -> previousStep()
            TutorialEvent.SkipTutorial -> skipTutorial()
            TutorialEvent.CompleteTutorial -> completeTutorial()
        }
    }

    private fun startTutorial(forceReplay: Boolean = false) {
        viewModelScope.launch(dispatcher) {
            if (!forceReplay) {
                val hasTransactions = getTransactionsUseCase()
                    .map { it.getOrElse { emptyList() } }
                    .firstOrNull()
                    ?.isNotEmpty() == true

                if (hasTransactions) {
                    // Existing user upgrading from an older version — skip silently
                    setTutorialCompletedUseCase(true)
                    logDebug("StartTutorial: existing user, marking completed without showing tutorial")
                    return@launch
                }
            }
            _state.value = TutorialState(isActive = true, currentStep = 0)
            analyticsManager.logEvent("tutorial_started")
            logDebug("StartTutorial: starting tutorial (forceReplay=$forceReplay)")
        }
    }

    private fun nextStep() {
        val current = _state.value
        if (!current.isActive) return
        val nextStep = current.currentStep + 1
        if (nextStep >= TUTORIAL_TOTAL_STEPS) {
            _state.value = current.copy(isFinishing = true)
        } else {
            val params = Bundle().apply { putInt("step", current.currentStep) }
            analyticsManager.logEvent("tutorial_step_completed", params)
            _state.value = current.copy(currentStep = nextStep)
        }
    }

    private fun previousStep() {
        val current = _state.value
        if (!current.isActive) return
        if (current.isFinishing) {
            _state.value = current.copy(isFinishing = false)
            return
        }
        if (current.currentStep == 0) {
            skipTutorial()
        } else {
            _state.value = current.copy(currentStep = current.currentStep - 1)
        }
    }

    private fun skipTutorial() {
        val step = _state.value.currentStep
        viewModelScope.launch(dispatcher) {
            val params = Bundle().apply { putInt("step", step) }
            analyticsManager.logEvent("tutorial_step_skipped", params)
            setTutorialCompletedUseCase(true)
            _state.value = TutorialState(isActive = false)
            logDebug("Tutorial skipped at step $step")
        }
    }

    private fun completeTutorial() {
        viewModelScope.launch(dispatcher) {
            analyticsManager.logEvent("tutorial_completed")
            setTutorialCompletedUseCase(true)
            _state.value = TutorialState(isActive = false)
            logDebug("Tutorial completed")
        }
    }
}
