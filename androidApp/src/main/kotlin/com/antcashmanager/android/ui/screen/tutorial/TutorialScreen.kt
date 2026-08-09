package com.antcashmanager.android.ui.screen.tutorial

import androidx.compose.runtime.Composable
import com.antcashmanager.android.ui.components.overlay.TutorialOverlay

/**
 * Schermata che mostra il tutorial interattivo.
 *
 * Può essere accessibile:
 * - Da phone: tramite item Tutorial nella sidebar
 * - Da tablet: tramite item Tutorial nel rail navigation
 *
 * Al completamento del tutorial, naviga indietro.
 */
@Composable
fun TutorialScreen(
    onNavigateBack: () -> Unit,
) {
    TutorialOverlay(
        onDismiss = onNavigateBack,
    )
}
