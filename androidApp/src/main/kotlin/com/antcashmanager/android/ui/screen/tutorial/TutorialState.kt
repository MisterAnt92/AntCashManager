package com.antcashmanager.android.ui.screen.tutorial

import com.antcashmanager.architecture.UIState

/**
 * UI state for the reveal-based tutorial overlay.
 *
 * @param currentStep Active step index (0 = welcome … 5 = settings nav).
 * @param isActive Whether the reveal tutorial is currently visible.
 * @param isFinishing True when the last step was confirmed and the finish card is shown.
 */
data class TutorialState(
    val currentStep: Int = 0,
    val isActive: Boolean = false,
    val isFinishing: Boolean = false,
) : UIState()
