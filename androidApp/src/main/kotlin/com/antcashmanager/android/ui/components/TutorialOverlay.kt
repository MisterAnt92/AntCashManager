package com.antcashmanager.android.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

    val step = steps[currentStep]
    val isLastStep = currentStep == steps.lastIndex
    val isWelcomeStep = currentStep == 0
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Animazione fade + slide-up solo per il primo step (welcome)
            val contentVisible = !isWelcomeStep || welcomeVisible
            val enterTransition = if (isWelcomeStep) {
                fadeIn(tween(durationMillis = 600)) +
                    slideInVertically(tween(durationMillis = 600)) { it / 4 }
            } else {
                fadeIn(tween(durationMillis = 0))
            }

            AnimatedVisibility(visible = contentVisible, enter = enterTransition) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppText(
                        text = stringResource(step.titleRes),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .semantics { heading() },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppText(
                        text = stringResource(step.descRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.90f),
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (step.imageRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = step.imageRes),
                        contentDescription = stringResource(step.titleRes),
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        contentScale = ContentScale.Crop,
                    )
                }
            } else if (isWelcomeStep) {
                AnimatedVisibility(
                    visible = welcomeVisible,
                    enter = fadeIn(tween(durationMillis = 700, delayMillis = 300)) +
                        slideInVertically(tween(durationMillis = 700, delayMillis = 300)) { it / 3 },
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .weight(1f),
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
                                .padding(16.dp),
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .clearAndSetSemantics {
                        contentDescription = progressDescription
                    },
            ) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (index == currentStep) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = RoundedCornerShape(50),
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.96f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isLastStep) {
                    TextButton(onClick = onDismiss) {
                        AppText(text = stringResource(R.string.tutorial_skip))
                    }
                    AppButton(onClick = { currentStep += 1 }) {
                        AppText(text = stringResource(R.string.tutorial_next))
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    AppButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
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

