package com.antcashmanager.android.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.theme.LocalReduceMotion

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
) {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) { visible = true }

    val enterDuration = if (reduceMotion) 0 else 500

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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (retryLabel != null && onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onRetry) {
                    Text(retryLabel)
                }
            }
        }
    }
}

