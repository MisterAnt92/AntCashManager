package com.antcashmanager.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.antcashmanager.android.R

@Composable
fun TutorialOverlay(
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf(
        TutorialStep(
            titleRes = R.string.tutorial_welcome_title,
            descRes = R.string.tutorial_welcome_desc
        ),
        TutorialStep(
            titleRes = R.string.tutorial_dashboard_title,
            descRes = R.string.tutorial_dashboard_desc
        ),
        TutorialStep(
            titleRes = R.string.tutorial_transactions_title,
            descRes = R.string.tutorial_transactions_desc
        ),
        TutorialStep(
            titleRes = R.string.tutorial_charts_title,
            descRes = R.string.tutorial_charts_desc
        ),
        TutorialStep(
            titleRes = R.string.tutorial_categories_title,
            descRes = R.string.tutorial_categories_desc
        ),
        TutorialStep(
            titleRes = R.string.tutorial_settings_title,
            descRes = R.string.tutorial_settings_desc
        ),
        TutorialStep(
            titleRes = R.string.tutorial_finish_title,
            descRes = R.string.tutorial_finish_desc
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(steps[currentStep].titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(steps[currentStep].descRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Progress indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (index == currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep < steps.size - 1) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.tutorial_skip))
                        }
                        
                        Button(
                            onClick = { currentStep++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.tutorial_next))
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.tutorial_finish))
                        }
                    }
                }
            }
        }
    }
}

private data class TutorialStep(
    val titleRes: Int,
    val descRes: Int
)
