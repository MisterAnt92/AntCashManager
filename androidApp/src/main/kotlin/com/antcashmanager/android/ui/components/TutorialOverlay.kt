package com.antcashmanager.android.ui.components

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
fun TutorialOverlay(
    onDismiss: () -> Unit,
) {
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf(
        TutorialStep(
            titleRes = R.string.tutorial_welcome_title,
            descRes = R.string.tutorial_welcome_desc,
            imageRes = null,
        ),
        TutorialStep(
            titleRes = R.string.tutorial_dashboard_title,
            descRes = R.string.tutorial_dashboard_desc,
            imageRes = R.drawable.main,
        ),
        TutorialStep(
            titleRes = R.string.tutorial_charts_title,
            descRes = R.string.tutorial_charts_desc,
            imageRes = R.drawable.charts,
        ),
        TutorialStep(
            titleRes = R.string.tutorial_transactions_title,
            descRes = R.string.tutorial_transactions_desc,
            imageRes = R.drawable.transactions,
        ),
        TutorialStep(
            titleRes = R.string.tutorial_categories_title,
            descRes = R.string.tutorial_categories_desc,
            imageRes = R.drawable.categories,
        ),
        TutorialStep(
            titleRes = R.string.tutorial_settings_title,
            descRes = R.string.tutorial_settings_desc,
            imageRes = R.drawable.settings,
        ),
        TutorialStep(
            titleRes = R.string.tutorial_finish_title,
            descRes = R.string.tutorial_finish_desc,
            imageRes = R.drawable.final_step,
        ),
    )

    var welcomeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { welcomeVisible = true }

    // Il tasto/gesture back di sistema deve muoversi indietro nel tutorial invece di
    // uscire dalla Home sottostante (di cui il tutorial è un overlay a schermo intero).
    BackHandler {
        if (currentStep > 0) {
            currentStep -= 1
        } else {
            onDismiss()
        }
    }

    val isLastStep = currentStep == steps.lastIndex
    val isFirstStep = currentStep == 0
    val progressDescription = stringResource(
        R.string.tutorial_progress_cd,
        currentStep + 1,
        steps.size,
    )
    val welcomeHighlights = listOf(
        R.string.tutorial_welcome_point_track,
        R.string.tutorial_welcome_point_insights,
        R.string.tutorial_welcome_point_categories,
        R.string.tutorial_welcome_point_customize,
    )
    val isFoldablePortrait = adaptiveLayoutInfo.isFoldableDevice && !adaptiveLayoutInfo.isLandscape
    val horizontalPadding = when {
        adaptiveLayoutInfo.isExpanded -> 32.dp
        isFoldablePortrait -> 16.dp
        else -> 12.dp
    }
    val verticalPadding = when {
        adaptiveLayoutInfo.isExpanded -> 24.dp
        isFoldablePortrait -> 16.dp
        else -> 12.dp
    }
    val titleWidthFraction = when {
        adaptiveLayoutInfo.isCompact -> 1f
        isFoldablePortrait -> 0.9f
        else -> 0.85f
    }
    val descriptionWidthFraction = when {
        adaptiveLayoutInfo.isExpanded -> 0.85f
        isFoldablePortrait -> 0.9f
        else -> 1f
    }
    val imageWidthFraction = when {
        adaptiveLayoutInfo.isExpanded -> 0.85f
        isFoldablePortrait -> 0.95f
        else -> 1f
    }
    val controlsWidthFraction = 1f
    val welcomeCardPadding = if (adaptiveLayoutInfo.isExpanded) 20.dp else 12.dp

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Contenuto per-step (titolo, descrizione, immagine/highlights) con transizione
            // slide+fade orizzontale, la cui direzione segue il verso di navigazione
            // (avanti → scorre da destra, indietro → scorre da sinistra). Il primo step
            // (welcome) mantiene inoltre la sua animazione di ingresso dedicata via
            // [welcomeVisible], indipendente da questa transizione tra step.
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val forward = targetState > initialState
                    val enter = slideInHorizontally(tween(350)) { width ->
                        if (forward) width / 4 else -width / 4
                    } + fadeIn(tween(350))
                    val exit = slideOutHorizontally(tween(350)) { width ->
                        if (forward) -width / 4 else width / 4
                    } + fadeOut(tween(200))
                    enter togetherWith exit
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "tutorial_step_content",
            ) { stepIndex ->
                val animatedStep = steps[stepIndex]
                val isAnimatedWelcomeStep = stepIndex == 0

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val contentVisible = !isAnimatedWelcomeStep || welcomeVisible
                    val enterTransition = if (isAnimatedWelcomeStep) {
                        fadeIn(tween(durationMillis = 600)) +
                            slideInVertically(tween(durationMillis = 600)) { it / 4 }
                    } else {
                        fadeIn(tween(durationMillis = 0))
                    }

                    AnimatedVisibility(visible = contentVisible, enter = enterTransition) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AppText(
                                text = stringResource(animatedStep.titleRes),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth(titleWidthFraction)
                                    .semantics { heading() },
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            AppText(
                                text = stringResource(animatedStep.descRes),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(descriptionWidthFraction),
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    if (animatedStep.imageRes != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(imageWidthFraction)
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(id = animatedStep.imageRes),
                                contentDescription = stringResource(animatedStep.titleRes),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    } else if (isAnimatedWelcomeStep) {
                        AnimatedVisibility(
                            visible = welcomeVisible,
                            enter = fadeIn(tween(durationMillis = 700, delayMillis = 300)) +
                                slideInVertically(tween(durationMillis = 700, delayMillis = 300)) { it / 3 },
                            modifier = Modifier.weight(1f)
                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(descriptionWidthFraction)
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                tonalElevation = 3.dp,
                                shadowElevation = 2.dp,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(welcomeCardPadding),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    welcomeHighlights.forEachIndexed { index, textRes ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    AppText(
                                                        text = "${index + 1}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                                AppText(
                                                    text = stringResource(textRes),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Start,
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        } // end AnimatedVisibility welcome highlights
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth(controlsWidthFraction)
                    .padding(vertical = 6.dp, horizontal = 6.dp)
                    .clearAndSetSemantics {
                        contentDescription = progressDescription
                    },
            ) {
                steps.forEachIndexed { index, _ ->
                    val isActiveDot = index == currentStep
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActiveDot) 24.dp else 8.dp,
                        label = "tutorial_dot_width",
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isActiveDot) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            )
                            .clickable(enabled = !isActiveDot) { currentStep = index },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(controlsWidthFraction),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Riservato sempre lo stesso spazio per il tasto Indietro (nascosto ma non
                // rimosso al primo step) così Skip/Next/Finish non si spostano orizzontalmente
                // passando da uno step all'altro.
                TextButton(
                    onClick = { currentStep -= 1 },
                    enabled = !isFirstStep,
                    modifier = Modifier.alpha(if (isFirstStep) 0f else 1f),
                ) {
                    AppText(text = stringResource(R.string.common_back))
                }

                if (!isLastStep) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            AppText(text = stringResource(R.string.tutorial_skip))
                        }
                        AppButton(onClick = { currentStep += 1 }) {
                            AppText(text = stringResource(R.string.tutorial_next))
                        }
                    }
                } else {
                    AppButton(onClick = onDismiss) {
                        AppText(text = stringResource(R.string.tutorial_finish))
                    }
                }
            }
        }
    }
}

private data class TutorialStep(
    val titleRes: Int,
    val descRes: Int,
    val imageRes: Int?,
)

@Preview(name = "TutorialOverlay - Light", showBackground = true)
@Composable
private fun TutorialOverlayPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        TutorialOverlay(onDismiss = {})
    }
}

@Preview(
    name = "TutorialOverlay - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TutorialOverlayPreviewDark() {
    TutorialOverlayPreviewLight()
}

@Preview(name = "TutorialOverlay - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun TutorialOverlayPreviewAccessibility() {
    TutorialOverlayPreviewLight()
}
