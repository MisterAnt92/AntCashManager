package com.antcashmanager.android.ui.components.overlay

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.tutorial.TUTORIAL_TOTAL_STEPS
import com.antcashmanager.android.ui.screen.tutorial.TutorialEvent
import com.antcashmanager.android.ui.screen.tutorial.TutorialKey
import com.antcashmanager.android.ui.screen.tutorial.TutorialState
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Overlay tooltip card shown by the Reveal library for each tutorial step.
 *
 * Renders a card with title/description derived from [key], dot-pagination,
 * and Back/Next/Skip/Finish action buttons. When [state.isFinishing] is true
 * a completion card is shown instead.
 */
@Composable
fun TutorialTooltipContent(
    key: TutorialKey?,
    state: TutorialState,
    onEvent: (TutorialEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFinishing = state.isFinishing

    val titleRes = if (isFinishing) {
        R.string.tutorial_finish_title
    } else {
        when (key) {
            TutorialKey.HOME_TRANSACTIONS -> R.string.tutorial_transactions_title
            TutorialKey.CHARTS_NAV        -> R.string.tutorial_charts_title
            TutorialKey.TRANSACTIONS_NAV  -> R.string.tutorial_add_transaction_title
            TutorialKey.CATEGORIES_NAV    -> R.string.tutorial_categories_title
            null                          -> R.string.tutorial_finish_title
        }
    }

    val descRes = if (isFinishing) {
        R.string.tutorial_finish_desc
    } else {
        when (key) {
            TutorialKey.HOME_TRANSACTIONS -> R.string.tutorial_transactions_desc
            TutorialKey.CHARTS_NAV        -> R.string.tutorial_charts_desc
            TutorialKey.TRANSACTIONS_NAV  -> R.string.tutorial_add_transaction_desc
            TutorialKey.CATEGORIES_NAV    -> R.string.tutorial_categories_desc
            null                          -> R.string.tutorial_finish_desc
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Column {
            AppText(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            AppText(
                text = stringResource(descRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            if (!isFinishing) {
                TutorialDots(
                    currentStep = state.currentStep,
                    totalSteps = TUTORIAL_TOTAL_STEPS,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!isFinishing && state.currentStep > 0) {
                    OutlinedButton(
                        onClick = { onEvent(TutorialEvent.SkipTutorial) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.tutorial_skip))
                    }
                }
                AppButton(
                    text = if (isFinishing) {
                        stringResource(R.string.tutorial_finish)
                    } else {
                        stringResource(R.string.tutorial_next)
                    },
                    onClick = {
                        if (isFinishing) onEvent(TutorialEvent.CompleteTutorial)
                        else onEvent(TutorialEvent.NextStep)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TutorialDots(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            Box(
                modifier = Modifier
                    .size(if (isActive) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}

@Preview(name = "TutorialTooltipContent - Light", showBackground = true)
@Composable
private fun TutorialTooltipContentPreviewLight() {
    AntCashManagerTheme {
        TutorialTooltipContent(
            key = TutorialKey.HOME_TRANSACTIONS,
            state = TutorialState(currentStep = 0, isActive = true),
            onEvent = {},
        )
    }
}

@Preview(
    name = "TutorialTooltipContent - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TutorialTooltipContentPreviewDark() {
    AntCashManagerTheme(darkTheme = true) {
        TutorialTooltipContent(
            key = TutorialKey.CHARTS_NAV,
            state = TutorialState(currentStep = 1, isActive = true),
            onEvent = {},
        )
    }
}

@Preview(name = "TutorialTooltipContent - 2x Font", showBackground = true, fontScale = 2.0f)
@Composable
private fun TutorialTooltipContentPreview2xFont() {
    AntCashManagerTheme {
        TutorialTooltipContent(
            key = TutorialKey.CATEGORIES_NAV,
            state = TutorialState(currentStep = 3, isActive = true),
            onEvent = {},
        )
    }
}
