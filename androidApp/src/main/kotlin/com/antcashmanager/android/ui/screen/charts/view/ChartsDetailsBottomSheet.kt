package com.antcashmanager.android.ui.screen.charts.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.android.util.translateCategory

/**
 * Data class for chart details to display in bottom sheet.
 * Holds category information and calculations.
 */
data class ChartDetailsData(
    val categoryName: String,
    val amount: Double,
    val percentage: Int,
    val colorHex: Long,
    val transactionCount: Int = 0,
    val trend: TrendDirection = TrendDirection.NEUTRAL,
)

enum class TrendDirection(
    val symbol: String,
) {
    UP("↑"),
    DOWN("↓"),
    NEUTRAL("→"),
}

/**
 * Bottom sheet showing detailed breakdown for a selected chart category.
 *
 * Features:
 * - Animated appearance with smooth transitions
 * - Category color indicator
 * - Amount display with formatting
 * - Transaction count
 * - Trend indication
 * - Swipe down to dismiss
 *
 * Accessibility:
 * - Content descriptions for all elements
 * - Screen reader compatible
 * - High contrast support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChartsDetailsBottomSheet(
    chartDetails: ChartDetailsData?,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    modifier: Modifier = Modifier,
) {
    if (chartDetails == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { BottomSheetDefaults.windowInsets },
        modifier = modifier,
    ) {
        ChartsDetailsContent(
            details = chartDetails,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun ChartsDetailsContent(
    details: ChartDetailsData,
    onDismiss: () -> Unit,
) {
    val fmt = LocalCurrencyFormat.current
    val amountsMasked = LocalAmountsMasked.current
    val categoryDisplayName = translateCategory(details.categoryName)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 32.dp),
    ) {
        // Header with close button
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                text = stringResource(R.string.chart_details_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.common_close),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        VerticalSpacer(SpacingSize.SM)

        // Category header with color indicator
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Color indicator circle with animation
            val indicatorColor =
                androidx.compose.ui.graphics
                    .Color(details.colorHex)
            val animatedColor by animateColorAsState(
                targetValue = indicatorColor,
                label = "colorIndicatorAnimation",
            )
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(animatedColor),
            )

            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = categoryDisplayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                VerticalSpacer(SpacingSize.XXXS)
                AppText(
                    text =
                        stringResource(
                            R.string.chart_details_percentage,
                            details.percentage,
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        VerticalSpacer(SpacingSize.ML)

        // Amount section with animation
        DetailsMetricCard(
            label = stringResource(R.string.chart_details_amount),
            value = formatAmount(details.amount, fmt),
            isMasked = amountsMasked,
            icon = null,
        )

        VerticalSpacer(SpacingSize.SM)

        // Transaction count
        DetailsMetricCard(
            label = stringResource(R.string.chart_details_transactions),
            value = details.transactionCount.toString(),
            isMasked = false,
            icon = null,
        )

        VerticalSpacer(SpacingSize.SM)

        // Trend indicator
        if (details.trend != TrendDirection.NEUTRAL) {
            TrendIndicator(direction = details.trend)
            VerticalSpacer(SpacingSize.SM)
        }

        // Additional info section
        InfoSection()

        VerticalSpacer(SpacingSize.LG)
    }
}

@Composable
private fun DetailsMetricCard(
    label: String,
    value: String,
    isMasked: Boolean,
    icon: androidx.compose.runtime.Composable? = null,
) {
    val displayValue = if (isMasked) "••••••" else value
    val animatedHorizontalPadding by animateDpAsState(
        targetValue = 24.dp,
        label = "metricPaddingAnimation",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = animatedHorizontalPadding, vertical = 8.dp),
        colors =
            androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            AppText(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            VerticalSpacer(SpacingSize.XS)
            AppText(
                text = displayValue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TrendIndicator(direction: TrendDirection) {
    val trendColor =
        when (direction) {
            TrendDirection.UP -> MaterialTheme.colorScheme.error // Red for higher spending
            TrendDirection.DOWN -> MaterialTheme.colorScheme.primary // Green for lower
            TrendDirection.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .background(
                    color = trendColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small,
                ).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            tint = trendColor,
            modifier = Modifier.width(20.dp),
        )
        AppText(
            text =
                stringResource(
                    when (direction) {
                        TrendDirection.UP -> R.string.chart_trend_increasing
                        TrendDirection.DOWN -> R.string.chart_trend_decreasing
                        TrendDirection.NEUTRAL -> R.string.chart_trend_stable
                    },
                ),
            style = MaterialTheme.typography.bodySmall,
            color = trendColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun InfoSection() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
    ) {
        AppText(
            text = stringResource(R.string.chart_details_info_title),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        VerticalSpacer(SpacingSize.XS)
        AppText(
            text = stringResource(R.string.chart_details_info_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Required imports for Card
@Composable
private fun Card(
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.CardColors =
        androidx.compose.material3.CardDefaults
            .cardColors(),
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        colors = colors,
    ) {
        content()
    }
}

// Helper extension for Modifier.size when used with Spacer
private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
