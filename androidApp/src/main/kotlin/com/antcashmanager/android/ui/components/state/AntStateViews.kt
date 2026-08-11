package com.antcashmanager.android.ui.components.state

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.os.Bundle
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.LocalReduceMotion
import org.koin.compose.koinInject

/**
 * Reusable empty-state composable with ant mascot image,
 * title and optional subtitle. Shows an entrance animation
 * unless reduce-motion is enabled.
 */
@Composable
fun AntEmptyState(
    @DrawableRes mascotRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    mascotSize: Dp = 96.dp,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionAnalyticsLabel: String? = null,
) {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) { visible = true }

    val enterDuration = if (reduceMotion) 0 else 500
    val analyticsManager: com.antcashmanager.android.analytics.AnalyticsManager = koinInject()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(enterDuration)) +
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(enterDuration),
                ),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = mascotRes),
                contentDescription = null,
                modifier = Modifier.size(mascotSize),
            )
            VerticalSpacer(SpacingSize.MD)
            AppText(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                VerticalSpacer(SpacingSize.XXS)
                AppText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
            if (actionLabel != null && onAction != null) {
                VerticalSpacer(SpacingSize.MD)
                OutlinedButton(
                    onClick = {
                        // Track empty state action
                        if (actionAnalyticsLabel != null) {
                            val params = Bundle().apply {
                                putString("action", actionAnalyticsLabel)
                            }
                            analyticsManager.logEvent("empty_state_action_taken", params)
                        }
                        onAction()
                    }
                ) {
                    AppText(actionLabel)
                }
            }
        }
    }
}

/**
 * Reusable error-state composable with error icon overlay on mascot,
 * title, subtitle, and an optional retry button.
 */
@Composable
fun AntErrorState(
    @DrawableRes mascotRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
    mascotSize: Dp = 96.dp,
) {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) { visible = true }

    val enterDuration = if (reduceMotion) 0 else 500

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(enterDuration)),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Mascot with error overlay
            androidx.compose.foundation.layout.Box(
                contentAlignment = Alignment.BottomEnd,
            ) {
                Image(
                    painter = painterResource(id = mascotRes),
                    contentDescription = null,
                    modifier = Modifier.size(mascotSize),
                )
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp),
                )
            }
            VerticalSpacer(SpacingSize.MD)
            AppText(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                VerticalSpacer(SpacingSize.XXS)
                AppText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (retryLabel != null && onRetry != null) {
                VerticalSpacer(SpacingSize.MD)
                OutlinedButton(onClick = onRetry) {
                    AppText(retryLabel)
                }
            }
        }
    }
}

@Preview(name = "AntStateViews - Light", showBackground = true)
@Composable
private fun AntStateViewsPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AntEmptyState(
                mascotRes = R.drawable.ic_piggy_bank,
                title = stringResource(R.string.empty_state_no_transactions),
                subtitle = stringResource(R.string.empty_state_no_transactions_subtitle),
            )
            AntErrorState(
                mascotRes = R.drawable.ic_piggy_bank,
                title = stringResource(R.string.restore_error_title),
                subtitle = stringResource(R.string.restore_error_message, "backup.json"),
                retryLabel = stringResource(R.string.common_confirm),
                onRetry = {},
            )
        }
    }
}

@Preview(
    name = "AntStateViews - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AntStateViewsPreviewDark() {
    AntStateViewsPreviewLight()
}

@Preview(name = "AntStateViews - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun AntStateViewsPreviewAccessibility() {
    AntStateViewsPreviewLight()
}
