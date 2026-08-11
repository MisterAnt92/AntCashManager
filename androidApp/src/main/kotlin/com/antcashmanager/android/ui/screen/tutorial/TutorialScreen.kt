package com.antcashmanager.android.ui.screen.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.antcashmanager.android.ui.components.overlay.TutorialOverlay
import org.koin.compose.koinInject

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
    val analyticsManager: com.antcashmanager.android.analytics.AnalyticsManager = koinInject()

    // Track tutorial replay/start
    LaunchedEffect(Unit) {
        analyticsManager.logEvent("tutorial_replay_requested")
    }

    TutorialOverlay(
        onDismiss = onNavigateBack,
    )
}
