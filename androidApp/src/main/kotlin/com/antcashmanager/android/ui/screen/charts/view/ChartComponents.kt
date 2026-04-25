package com.antcashmanager.android.ui.screen.charts.view

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.screen.charts.MonthlyAmount
import com.antcashmanager.android.ui.screen.charts.YearlyAmount
import com.antcashmanager.android.ui.theme.LocalReduceMotion
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.graphics.PathEffect as ComposePathEffect

// Utility functions for safe float operations and crash prevention
private fun Float.safeValue(fallback: Float = 0f): Float =
    if (isFinite() && !isNaN()) this else fallback

private fun Double.safeValue(fallback: Double = 0.0): Double =
    if (isFinite() && !isNaN()) this else fallback

private fun safeCalculation(calculation: () -> Float): Float {
    return try {
        calculation().takeIf { it.isFinite() } ?: 0f
    } catch (_: Exception) {
        0f
    }
}

private fun safeCalculationDouble(calculation: () -> Double): Double {
    return try {
        calculation().takeIf { it.isFinite() } ?: 0.0
    } catch (_: Exception) {
        0.0
    }
}

private val pieColors = listOf(
    Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
    Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4FC3F7),
    Color(0xFFF06292), Color(0xFFDCE775), Color(0xFF4DB6AC),
    Color(0xFF7986CB), Color(0xFFA1887F), Color(0xFF90A4AE),
)

@Composable
internal fun PieChart(data: Map<String, Double>, modifier: Modifier = Modifier) {
    val total = data.values.sum()
    if (total == 0.0) return
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 1200
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "pie"
    )

    Canvas(modifier = modifier) {
        val outerRadius = minOf(size.width, size.height) * 0.38f
        val innerRadius = outerRadius * 0.5f // Creates donut effect
        val center = Offset(size.width / 2f, size.height / 2f)

        // Shadow layer
        val shadowRadius = outerRadius + 8f
        drawCircle(
            color = Color.Black.copy(alpha = 0.1f),
            radius = shadowRadius,
            center = center.copy(x = center.x + 4f, y = center.y + 6f)
        )

        var startAngle = -90f
        data.entries.forEachIndexed { index, (_, value) ->
            val sweep = (value / total * 360f * animatedProgress).toFloat()
            val baseColor = pieColors[index % pieColors.size]

            // Outer arc with gradient
            val outerBrush = Brush.radialGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.9f),
                    baseColor.copy(alpha = 0.7f),
                    baseColor.copy(alpha = 0.8f)
                ),
                center = center,
                radius = outerRadius
            )

            // Draw outer arc
            drawArc(
                brush = outerBrush,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = outerRadius - innerRadius)
            )

            // Highlight effect on the outer edge
            drawArc(
                color = baseColor.copy(alpha = 0.3f),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius - 2f, center.y - outerRadius - 2f),
                size = Size((outerRadius + 2f) * 2, (outerRadius + 2f) * 2),
                style = Stroke(width = 2f)
            )

            startAngle += sweep
        }

        // Center circle with gradient
        val centerBrush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.8f),
                Color.Gray.copy(alpha = 0.1f)
            ),
            center = center,
            radius = innerRadius
        )
        drawCircle(
            brush = centerBrush,
            radius = innerRadius,
            center = center
        )

        // Center border
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = innerRadius,
            center = center,
            style = Stroke(width = 1f)
        )
    }
}

@Composable
internal fun PieLegend(data: Map<String, Double>) {
    val total = data.values.sum()
    val fmt = LocalCurrencyFormat.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.entries.forEachIndexed { index, (category, value) ->
            val percentage = (value / total * 100)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Enhanced color indicator with shadow
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    pieColors[index % pieColors.size],
                                    pieColors[index % pieColors.size].copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                            .align(Alignment.TopStart)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${formatAmount(value, fmt)} • %.1f%%".format(percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Progress bar for visual representation
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (percentage / 100f).toFloat().coerceIn(0f, 1f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        pieColors[index % pieColors.size].copy(alpha = 0.8f),
                                        pieColors[index % pieColors.size]
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
internal fun BarChartLegend() {
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Income legend
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            incomeColor.copy(alpha = 0.1f),
                            incomeColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                incomeColor.copy(alpha = 0.9f),
                                incomeColor
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                        .align(Alignment.TopStart)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.charts_income),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = incomeColor
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Expense legend
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            expenseColor.copy(alpha = 0.1f),
                            expenseColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                expenseColor.copy(alpha = 0.9f),
                                expenseColor
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                        .align(Alignment.TopStart)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.charts_expenses),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = expenseColor
            )
        }
    }
}

/**
 * Draws a professional grid overlay on charts
 */
private fun DrawScope.drawGrid(
    maxValue: Double,
    chartWidth: Float,
    chartHeight: Float,
    baseY: Float,
    gridColor: Color
) {
    val gridLineCount = 5
    val heightStep = chartHeight / gridLineCount

    // Create dashed line effect
    val dashPathEffect = ComposePathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

    // Draw horizontal grid lines
    for (i in 1..gridLineCount) {
        val y = baseY - (heightStep * i)
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(chartWidth, y),
            strokeWidth = 1f,
            pathEffect = dashPathEffect,
            cap = StrokeCap.Round
        )
    }

    // Draw vertical grid lines (optional, subtle)
    val verticalLines = 4
    val widthStep = chartWidth / verticalLines
    for (i in 1 until verticalLines) {
        val x = widthStep * i
        drawLine(
            color = gridColor.copy(alpha = 0.3f),
            start = Offset(x, baseY - chartHeight),
            end = Offset(x, baseY),
            strokeWidth = 1f,
            pathEffect = dashPathEffect,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draws a professional grid overlay on charts with zoom awareness
 * Safe implementation with crash prevention and validation
 */
private fun DrawScope.drawZoomAwareGrid(
    maxValue: Double,
    chartWidth: Float,
    chartHeight: Float,
    baseY: Float,
    gridColor: Color,
    scale: Float = 1f
) {
    // Safety checks to prevent crashes
    if (!scale.isFinite() || scale <= 0f || !chartWidth.isFinite() || !chartHeight.isFinite() ||
        chartWidth <= 0f || chartHeight <= 0f
    ) {
        return
    }

    // Safe scale clamping to prevent extreme values
    val safeScale = scale.coerceIn(0.1f, 10f)

    val gridLineCount = max(3, min(8, (5 * safeScale).toInt())).takeIf { it > 0 } ?: 5
    val heightStep = if (gridLineCount > 0) chartHeight / gridLineCount else chartHeight / 5

    // Create dashed line effect with zoom-aware dash size
    val dashSize = (8f * safeScale).coerceIn(2f, 32f)
    val gapSize = (6f * safeScale).coerceIn(1f, 24f)
    val dashPathEffect = ComposePathEffect.dashPathEffect(floatArrayOf(dashSize, gapSize), 0f)
    val strokeWidth = (1f * safeScale).coerceIn(0.5f, 8f)

    // Draw horizontal grid lines with bounds checking
    for (i in 1..gridLineCount) {
        val y = baseY - (heightStep * i)
        if (y.isFinite() && y >= 0f && y <= baseY) {
            try {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = strokeWidth,
                    pathEffect = dashPathEffect,
                    cap = StrokeCap.Round
                )
            } catch (_: Exception) {
                // Silently ignore drawing errors to prevent crash
            }
        }
    }

    // Draw vertical grid lines (optional, subtle) with safety
    val verticalLines = max(2, min(6, (4 * safeScale).toInt())).takeIf { it > 0 } ?: 4
    if (verticalLines > 0) {
        val widthStep = chartWidth / verticalLines
        for (i in 1 until verticalLines) {
            val x = widthStep * i
            if (x.isFinite() && x >= 0f && x <= chartWidth) {
                try {
                    drawLine(
                        color = gridColor.copy(alpha = 0.3f),
                        start = Offset(x, baseY - chartHeight),
                        end = Offset(x, baseY),
                        strokeWidth = strokeWidth,
                        pathEffect = dashPathEffect,
                        cap = StrokeCap.Round
                    )
                } catch (_: Exception) {
                    // Silently ignore drawing errors to prevent crash
                }
            }
        }
    }
}

@Composable
internal fun BarChart(data: List<MonthlyAmount>, modifier: Modifier = Modifier) {
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 1000
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "barAnim"
    )

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val maxValue = data.maxOf { maxOf(it.income, it.expense) }.takeIf { it > 0 } ?: 1.0
        val barWidth = size.width / (data.size * 2.5f)
        val spacing = barWidth * 0.5f
        val maxHeight = size.height * 0.75f
        val baseY = size.height * 0.8f

        // Draw professional grid
        drawGrid(maxValue, size.width, maxHeight, baseY, gridColor)

        data.forEachIndexed { index, month ->
            val incomeHeight = (month.income / maxValue * maxHeight * animatedProgress).toFloat()
            val expenseHeight = (month.expense / maxValue * maxHeight * animatedProgress).toFloat()
            val x = spacing + index * (barWidth * 2 + spacing * 1.5f)

            // Income bar with gradient
            val incomeBrush = Brush.verticalGradient(
                colors = listOf(
                    incomeColor.copy(alpha = 0.9f),
                    incomeColor.copy(alpha = 0.7f),
                    incomeColor
                )
            )
            drawRoundRect(
                brush = incomeBrush,
                topLeft = Offset(x, baseY - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Income bar highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x + 2f, baseY - incomeHeight + 2f),
                size = Size(barWidth - 4f, incomeHeight * 0.3f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Expense bar with gradient
            val expenseBrush = Brush.verticalGradient(
                colors = listOf(
                    expenseColor.copy(alpha = 0.9f),
                    expenseColor.copy(alpha = 0.7f),
                    expenseColor
                )
            )
            drawRoundRect(
                brush = expenseBrush,
                topLeft = Offset(x + barWidth + spacing * 0.5f, baseY - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Expense bar highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x + barWidth + spacing * 0.5f + 2f, baseY - expenseHeight + 2f),
                size = Size(barWidth - 4f, expenseHeight * 0.3f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Month label with improved styling
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    month.label,
                    x + barWidth,
                    baseY + 35f,
                    Paint().apply {
                        color = textColor.toArgb()
                        textSize = 11.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        setShadowLayer(2f, 1f, 1f, Color.Black.copy(alpha = 0.1f).toArgb())
                    }
                )
            }
        }
    }
}

@Composable
internal fun YearlyBarChart(data: List<YearlyAmount>, modifier: Modifier = Modifier) {
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 1200
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "yearlyAnim"
    )

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val maxValue = data.maxOf { maxOf(it.income, it.expense) }.takeIf { it > 0 } ?: 1.0
        val barWidth = size.width / (data.size * 2.8f)
        val spacing = barWidth * 0.6f
        val maxHeight = size.height * 0.75f
        val baseY = size.height * 0.8f

        // Draw professional grid
        drawGrid(maxValue, size.width, maxHeight, baseY, gridColor)

        data.forEachIndexed { index, year ->
            val incomeHeight = (year.income / maxValue * maxHeight * animatedProgress).toFloat()
            val expenseHeight = (year.expense / maxValue * maxHeight * animatedProgress).toFloat()
            val x = spacing + index * (barWidth * 2 + spacing * 1.8f)

            // Income bar with gradient and shadow
            val incomeBrush = Brush.verticalGradient(
                colors = listOf(
                    incomeColor.copy(alpha = 0.95f),
                    incomeColor.copy(alpha = 0.8f),
                    incomeColor
                )
            )

            // Shadow for income bar
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.08f),
                topLeft = Offset(x + 3f, baseY - incomeHeight + 3f),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Income bar
            drawRoundRect(
                brush = incomeBrush,
                topLeft = Offset(x, baseY - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Income bar top highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(x + 3f, baseY - incomeHeight + 3f),
                size = Size(barWidth - 6f, incomeHeight * 0.25f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Expense bar with gradient and shadow
            val expenseBrush = Brush.verticalGradient(
                colors = listOf(
                    expenseColor.copy(alpha = 0.95f),
                    expenseColor.copy(alpha = 0.8f),
                    expenseColor
                )
            )

            // Shadow for expense bar
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.08f),
                topLeft = Offset(x + barWidth + spacing * 0.7f + 3f, baseY - expenseHeight + 3f),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Expense bar
            drawRoundRect(
                brush = expenseBrush,
                topLeft = Offset(x + barWidth + spacing * 0.7f, baseY - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Expense bar top highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(x + barWidth + spacing * 0.7f + 3f, baseY - expenseHeight + 3f),
                size = Size(barWidth - 6f, expenseHeight * 0.25f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Year label with enhanced styling
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    year.label,
                    x + barWidth + spacing * 0.35f,
                    baseY + 40f,
                    Paint().apply {
                        color = textColor.toArgb()
                        textSize = 13.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                        setShadowLayer(3f, 1f, 2f, Color.Black.copy(alpha = 0.15f).toArgb())
                    }
                )
            }
        }

        // Draw subtle chart border
        drawLine(
            color = gridColor.copy(alpha = 0.3f),
            start = Offset(0f, baseY),
            end = Offset(size.width, baseY),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Enhanced Zoomable Pie Chart with donut style and gesture support
 * Safe implementation with crash prevention
 */
@Composable
internal fun ZoomablePieChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier,
    zoomEnabled: Boolean = true,
) {
    val total = data.values.sum()
    if (total <= 0.0) return

    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 1200
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "pie"
    )

    // Zoom and pan state with safe initial values
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(zoomEnabled) {
                    if (!zoomEnabled) return@pointerInput
                    detectTransformGestures { _, pan, zoom, _ ->
                        try {
                            // Safe zoom calculation with validation
                            val newScale = (scale * zoom).takeIf { it.isFinite() } ?: scale
                            scale = newScale.coerceIn(0.5f, 3f)

                            // Safe pan calculation with validation
                            val newOffsetX = (offsetX + pan.x).takeIf { it.isFinite() } ?: offsetX
                            val newOffsetY = (offsetY + pan.y).takeIf { it.isFinite() } ?: offsetY

                            // Constrain pan to reasonable bounds
                            val maxOffset = (size.width * 0.3f).takeIf { it.isFinite() } ?: 100f
                            offsetX = newOffsetX.coerceIn(-maxOffset, maxOffset)
                            offsetY = newOffsetY.coerceIn(-maxOffset, maxOffset)
                        } catch (_: Exception) {
                            // Reset to safe values on any calculation error
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
                .graphicsLayer(
                    scaleX = scale.takeIf { it.isFinite() && it > 0f } ?: 1f,
                    scaleY = scale.takeIf { it.isFinite() && it > 0f } ?: 1f,
                    translationX = offsetX.takeIf { it.isFinite() } ?: 0f,
                    translationY = offsetY.takeIf { it.isFinite() } ?: 0f
                )
        ) {
            if (size.width <= 0f || size.height <= 0f) return@Canvas

            val outerRadius = minOf(size.width, size.height) * 0.38f
            if (!outerRadius.isFinite() || outerRadius <= 0f) return@Canvas

            val innerRadius = outerRadius * 0.5f // Creates donut effect
            val center = Offset(size.width / 2f, size.height / 2f)

            // Shadow layer - enhanced for zoom
            val shadowRadius = outerRadius + 8f
            if (shadowRadius.isFinite() && shadowRadius > 0f) {
                try {
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.1f),
                        radius = shadowRadius,
                        center = center.copy(x = center.x + 4f, y = center.y + 6f)
                    )
                } catch (_: Exception) {
                    // Skip shadow on error
                }
            }

            var startAngle = -90f
            data.entries.forEachIndexed { index, (_, value) ->
                try {
                    val sweep = ((value / total * 360f * animatedProgress).toFloat())
                        .takeIf { it.isFinite() } ?: 0f
                    if (sweep <= 0f) return@forEachIndexed

                    val baseColor = pieColors[index % pieColors.size]

                    // Outer arc with gradient
                    val outerBrush = Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.9f),
                            baseColor.copy(alpha = 0.7f),
                            baseColor.copy(alpha = 0.8f)
                        ),
                        center = center,
                        radius = outerRadius
                    )

                    // Draw outer arc with safe parameters
                    drawArc(
                        brush = outerBrush,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(outerRadius * 2, outerRadius * 2),
                        style = Stroke(width = (outerRadius - innerRadius).coerceAtLeast(1f))
                    )

                    // Highlight effect on the outer edge
                    drawArc(
                        color = baseColor.copy(alpha = 0.3f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius - 2f, center.y - outerRadius - 2f),
                        size = Size((outerRadius + 2f) * 2, (outerRadius + 2f) * 2),
                        style = Stroke(width = 2f)
                    )

                    startAngle += sweep
                } catch (_: Exception) {
                    // Skip this arc segment on error
                }
            }

            // Center circle with gradient
            if (innerRadius > 0f) {
                try {
                    val centerBrush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.8f),
                            Color.Gray.copy(alpha = 0.1f)
                        ),
                        center = center,
                        radius = innerRadius
                    )
                    drawCircle(
                        brush = centerBrush,
                        radius = innerRadius,
                        center = center
                    )

                    // Center border
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.2f),
                        radius = innerRadius,
                        center = center,
                        style = Stroke(width = 1f)
                    )
                } catch (_: Exception) {
                    // Skip center circle on error
                }
            }
        }

        // Reset zoom button with validation
        if ((scale != 1f || offsetX != 0f || offsetY != 0f) &&
            scale.isFinite() && offsetX.isFinite() && offsetY.isFinite()
        ) {
            FloatingActionButton(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Reset Zoom",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Enhanced Zoomable Bar Chart with zoom and pan gestures
 * Safe implementation with crash prevention
 */
@Composable
internal fun ZoomableBarChart(
    data: List<MonthlyAmount>,
    modifier: Modifier = Modifier,
    zoomEnabled: Boolean = true,
) {
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 1000
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "barAnim"
    )

    // Zoom and pan state with safe initial values
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(zoomEnabled) {
                    if (!zoomEnabled) return@pointerInput
                    detectTransformGestures { _, pan, zoom, _ ->
                        try {
                            // Safe zoom calculation with validation
                            val newScale = (scale * zoom).takeIf { it.isFinite() } ?: scale
                            scale = newScale.coerceIn(0.3f, 4f)

                            // Safe pan calculation with validation and scale division protection
                            val safeDivisor = scale.takeIf { it > 0.01f } ?: 1f
                            val newOffsetX =
                                (offsetX + pan.x / safeDivisor).takeIf { it.isFinite() } ?: offsetX
                            val newOffsetY =
                                (offsetY + pan.y / safeDivisor).takeIf { it.isFinite() } ?: offsetY

                            // Constrain pan to chart bounds with safety checks
                            val maxOffsetX =
                                (size.width * (scale - 1) / 2).takeIf { it.isFinite() } ?: 0f
                            val maxOffsetY = (size.height * 0.2f).takeIf { it.isFinite() } ?: 100f
                            offsetX = newOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                        } catch (_: Exception) {
                            // Reset to safe values on any calculation error
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
        ) {
            try {
                val safeScale = scale.takeIf { it.isFinite() && it > 0f } ?: 1f
                val safeOffsetX = offsetX.takeIf { it.isFinite() } ?: 0f
                val safeOffsetY = offsetY.takeIf { it.isFinite() } ?: 0f
                val pivot = Offset(size.width / 2f, size.height / 2f)

                if (!pivot.x.isFinite() || !pivot.y.isFinite()) return@Canvas

                scale(safeScale, pivot = pivot) {
                    translate(safeOffsetX, safeOffsetY) {
                        if (data.isEmpty()) return@translate

                        val maxValue = data.maxOfOrNull { maxOf(it.income, it.expense) }
                            ?.takeIf { it > 0 } ?: 1.0

                        if (data.size <= 0) return@translate
                        val barWidth =
                            (size.width / (data.size * 2.5f)).takeIf { it.isFinite() && it > 0f }
                                ?: 10f
                        val spacing = barWidth * 0.5f
                        val maxHeight = size.height * 0.75f
                        val baseY = size.height * 0.8f

                        // Draw professional grid with zoom awareness
                        drawZoomAwareGrid(
                            maxValue,
                            size.width,
                            maxHeight,
                            baseY,
                            gridColor,
                            safeScale
                        )

                        data.forEachIndexed { index, month ->
                            try {
                                val incomeHeight =
                                    ((month.income / maxValue * maxHeight * animatedProgress).toFloat())
                                        .takeIf { it.isFinite() && it >= 0f } ?: 0f
                                val expenseHeight =
                                    ((month.expense / maxValue * maxHeight * animatedProgress).toFloat())
                                        .takeIf { it.isFinite() && it >= 0f } ?: 0f
                                val x = spacing + index * (barWidth * 2 + spacing * 1.5f)

                                if (!x.isFinite() || x < 0f) return@forEachIndexed

                                // Income bar with gradient
                                if (incomeHeight > 0f) {
                                    val incomeBrush = Brush.verticalGradient(
                                        colors = listOf(
                                            incomeColor.copy(alpha = 0.9f),
                                            incomeColor.copy(alpha = 0.7f),
                                            incomeColor
                                        )
                                    )
                                    drawRoundRect(
                                        brush = incomeBrush,
                                        topLeft = Offset(x, baseY - incomeHeight),
                                        size = Size(barWidth, incomeHeight),
                                        cornerRadius = CornerRadius(6f, 6f)
                                    )

                                    // Income bar highlight
                                    if (incomeHeight > 6f) {
                                        drawRoundRect(
                                            color = Color.White.copy(alpha = 0.3f),
                                            topLeft = Offset(x + 2f, baseY - incomeHeight + 2f),
                                            size = Size(
                                                (barWidth - 4f).coerceAtLeast(0f),
                                                incomeHeight * 0.3f
                                            ),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )
                                    }
                                }

                                // Expense bar with gradient
                                if (expenseHeight > 0f) {
                                    val expenseBrush = Brush.verticalGradient(
                                        colors = listOf(
                                            expenseColor.copy(alpha = 0.9f),
                                            expenseColor.copy(alpha = 0.7f),
                                            expenseColor
                                        )
                                    )
                                    drawRoundRect(
                                        brush = expenseBrush,
                                        topLeft = Offset(
                                            x + barWidth + spacing * 0.5f,
                                            baseY - expenseHeight
                                        ),
                                        size = Size(barWidth, expenseHeight),
                                        cornerRadius = CornerRadius(6f, 6f)
                                    )

                                    // Expense bar highlight
                                    if (expenseHeight > 6f) {
                                        drawRoundRect(
                                            color = Color.White.copy(alpha = 0.3f),
                                            topLeft = Offset(
                                                x + barWidth + spacing * 0.5f + 2f,
                                                baseY - expenseHeight + 2f
                                            ),
                                            size = Size(
                                                (barWidth - 4f).coerceAtLeast(0f),
                                                expenseHeight * 0.3f
                                            ),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )
                                    }
                                }

                                // Month label with zoom-aware sizing
                                val scaledTextSize = (11.sp.toPx() * min(safeScale, 1.5f))
                                    .takeIf { it.isFinite() && it > 0f } ?: 11.sp.toPx()

                                if (month.label.isNotBlank()) {
                                    try {
                                        drawContext.canvas.nativeCanvas.apply {
                                            drawText(
                                                month.label,
                                                x + barWidth,
                                                baseY + 35f,
                                                Paint().apply {
                                                    color = textColor.toArgb()
                                                    textSize = scaledTextSize
                                                    textAlign = Paint.Align.CENTER
                                                    isAntiAlias = true
                                                    setShadowLayer(
                                                        2f,
                                                        1f,
                                                        1f,
                                                        Color.Black.copy(alpha = 0.1f).toArgb()
                                                    )
                                                }
                                            )
                                        }
                                    } catch (_: Exception) {
                                        // Skip label drawing on error
                                    }
                                }
                            } catch (_: Exception) {
                                // Skip this bar on calculation error
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Draw fallback or skip on major error
            }
        }

        // Reset zoom button with validation
        if ((scale != 1f || offsetX != 0f || offsetY != 0f) &&
            scale.isFinite() && offsetX.isFinite() && offsetY.isFinite()
        ) {
            FloatingActionButton(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Reset Zoom",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Enhanced Zoomable Yearly Bar Chart with advanced zoom and pan
 * Safe implementation with crash prevention
 */
@Composable
internal fun ZoomableYearlyBarChart(
    data: List<YearlyAmount>,
    modifier: Modifier = Modifier,
    zoomEnabled: Boolean = true,
) {
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 1200
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "yearlyAnim"
    )

    // Zoom and pan state with safe initial values
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(zoomEnabled) {
                    if (!zoomEnabled) return@pointerInput
                    detectTransformGestures { _, pan, zoom, _ ->
                        try {
                            // Safe zoom calculation with validation
                            val newScale = (scale * zoom).takeIf { it.isFinite() } ?: scale
                            scale = newScale.coerceIn(0.3f, 5f)

                            // Safe pan calculation with validation and scale division protection
                            val safeDivisor = scale.takeIf { it > 0.01f } ?: 1f
                            val newOffsetX =
                                (offsetX + pan.x / safeDivisor).takeIf { it.isFinite() } ?: offsetX
                            val newOffsetY =
                                (offsetY + pan.y / safeDivisor).takeIf { it.isFinite() } ?: offsetY

                            // Constrain pan to chart bounds with safety checks
                            val maxOffsetX =
                                (size.width * (scale - 1) / 2).takeIf { it.isFinite() } ?: 0f
                            val maxOffsetY = (size.height * 0.2f).takeIf { it.isFinite() } ?: 100f
                            offsetX = newOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                        } catch (_: Exception) {
                            // Reset to safe values on any calculation error
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
        ) {
            try {
                val safeScale = scale.takeIf { it.isFinite() && it > 0f } ?: 1f
                val safeOffsetX = offsetX.takeIf { it.isFinite() } ?: 0f
                val safeOffsetY = offsetY.takeIf { it.isFinite() } ?: 0f
                val pivot = Offset(size.width / 2f, size.height / 2f)

                if (!pivot.x.isFinite() || !pivot.y.isFinite()) return@Canvas

                scale(safeScale, pivot = pivot) {
                    translate(safeOffsetX, safeOffsetY) {
                        if (data.isEmpty()) return@translate

                        val maxValue = data.maxOfOrNull { maxOf(it.income, it.expense) }
                            ?.takeIf { it > 0 } ?: 1.0

                        if (data.size <= 0) return@translate
                        val barWidth =
                            (size.width / (data.size * 2.8f)).takeIf { it.isFinite() && it > 0f }
                                ?: 10f
                        val spacing = barWidth * 0.6f
                        val maxHeight = size.height * 0.75f
                        val baseY = size.height * 0.8f

                        // Draw professional grid with zoom awareness
                        drawZoomAwareGrid(
                            maxValue,
                            size.width,
                            maxHeight,
                            baseY,
                            gridColor,
                            safeScale
                        )

                        data.forEachIndexed { index, year ->
                            try {
                                val incomeHeight =
                                    ((year.income / maxValue * maxHeight * animatedProgress).toFloat())
                                        .takeIf { it.isFinite() && it >= 0f } ?: 0f
                                val expenseHeight =
                                    ((year.expense / maxValue * maxHeight * animatedProgress).toFloat())
                                        .takeIf { it.isFinite() && it >= 0f } ?: 0f
                                val x = spacing + index * (barWidth * 2 + spacing * 1.8f)

                                if (!x.isFinite() || x < 0f) return@forEachIndexed

                                // Income bar with gradient and shadow
                                if (incomeHeight > 0f) {
                                    val incomeBrush = Brush.verticalGradient(
                                        colors = listOf(
                                            incomeColor.copy(alpha = 0.95f),
                                            incomeColor.copy(alpha = 0.8f),
                                            incomeColor
                                        )
                                    )

                                    // Shadow for income bar
                                    if (incomeHeight > 3f) {
                                        try {
                                            drawRoundRect(
                                                color = Color.Black.copy(alpha = 0.08f),
                                                topLeft = Offset(x + 3f, baseY - incomeHeight + 3f),
                                                size = Size(barWidth, incomeHeight),
                                                cornerRadius = CornerRadius(8f, 8f)
                                            )
                                        } catch (_: Exception) {
                                            // Skip shadow on error
                                        }
                                    }

                                    // Income bar
                                    drawRoundRect(
                                        brush = incomeBrush,
                                        topLeft = Offset(x, baseY - incomeHeight),
                                        size = Size(barWidth, incomeHeight),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )

                                    // Income bar top highlight
                                    if (incomeHeight > 12f) {
                                        try {
                                            drawRoundRect(
                                                color = Color.White.copy(alpha = 0.4f),
                                                topLeft = Offset(x + 3f, baseY - incomeHeight + 3f),
                                                size = Size(
                                                    (barWidth - 6f).coerceAtLeast(0f),
                                                    incomeHeight * 0.25f
                                                ),
                                                cornerRadius = CornerRadius(6f, 6f)
                                            )
                                        } catch (_: Exception) {
                                            // Skip highlight on error
                                        }
                                    }
                                }

                                // Expense bar with gradient and shadow
                                if (expenseHeight > 0f) {
                                    val expenseBrush = Brush.verticalGradient(
                                        colors = listOf(
                                            expenseColor.copy(alpha = 0.95f),
                                            expenseColor.copy(alpha = 0.8f),
                                            expenseColor
                                        )
                                    )

                                    // Shadow for expense bar
                                    if (expenseHeight > 3f) {
                                        try {
                                            drawRoundRect(
                                                color = Color.Black.copy(alpha = 0.08f),
                                                topLeft = Offset(
                                                    x + barWidth + spacing * 0.7f + 3f,
                                                    baseY - expenseHeight + 3f
                                                ),
                                                size = Size(barWidth, expenseHeight),
                                                cornerRadius = CornerRadius(8f, 8f)
                                            )
                                        } catch (_: Exception) {
                                            // Skip shadow on error
                                        }
                                    }

                                    // Expense bar
                                    drawRoundRect(
                                        brush = expenseBrush,
                                        topLeft = Offset(
                                            x + barWidth + spacing * 0.7f,
                                            baseY - expenseHeight
                                        ),
                                        size = Size(barWidth, expenseHeight),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )

                                    // Expense bar top highlight
                                    if (expenseHeight > 12f) {
                                        try {
                                            drawRoundRect(
                                                color = Color.White.copy(alpha = 0.4f),
                                                topLeft = Offset(
                                                    x + barWidth + spacing * 0.7f + 3f,
                                                    baseY - expenseHeight + 3f
                                                ),
                                                size = Size(
                                                    (barWidth - 6f).coerceAtLeast(0f),
                                                    expenseHeight * 0.25f
                                                ),
                                                cornerRadius = CornerRadius(6f, 6f)
                                            )
                                        } catch (_: Exception) {
                                            // Skip highlight on error
                                        }
                                    }
                                }

                                // Year label with enhanced zoom-aware styling
                                val scaledTextSize = (13.sp.toPx() * min(safeScale, 2f))
                                    .takeIf { it.isFinite() && it > 0f } ?: 13.sp.toPx()

                                if (year.label.isNotBlank()) {
                                    try {
                                        drawContext.canvas.nativeCanvas.apply {
                                            drawText(
                                                year.label,
                                                x + barWidth + spacing * 0.35f,
                                                baseY + 40f,
                                                Paint().apply {
                                                    color = textColor.toArgb()
                                                    textSize = scaledTextSize
                                                    textAlign = Paint.Align.CENTER
                                                    isAntiAlias = true
                                                    isFakeBoldText = true
                                                    setShadowLayer(
                                                        3f,
                                                        1f,
                                                        2f,
                                                        Color.Black.copy(alpha = 0.15f).toArgb()
                                                    )
                                                }
                                            )
                                        }
                                    } catch (_: Exception) {
                                        // Skip label drawing on error
                                    }
                                }
                            } catch (_: Exception) {
                                // Skip this bar on calculation error
                            }
                        }

                        // Draw subtle chart border with safety
                        try {
                            drawLine(
                                color = gridColor.copy(alpha = 0.3f),
                                start = Offset(0f, baseY),
                                end = Offset(size.width, baseY),
                                strokeWidth = 2f,
                                cap = StrokeCap.Round
                            )
                        } catch (_: Exception) {
                            // Skip border on error
                        }
                    }
                }
            } catch (_: Exception) {
                // Draw fallback or skip on major error
            }
        }

        // Reset zoom button with validation
        if ((scale != 1f || offsetX != 0f || offsetY != 0f) &&
            scale.isFinite() && offsetX.isFinite() && offsetY.isFinite()
        ) {
            FloatingActionButton(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Reset Zoom",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
