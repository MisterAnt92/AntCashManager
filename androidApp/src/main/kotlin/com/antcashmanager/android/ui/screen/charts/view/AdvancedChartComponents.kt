package com.antcashmanager.android.ui.screen.charts.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.screen.charts.ChartsConstant
import kotlin.math.cos
import kotlin.math.sin

/**
 * Arc-based gauge showing savings rate (0-100%) with color coding.
 * Green for good savings, orange for moderate, red for low.
 */
@Composable
internal fun SavingsRateGauge(
    savingsRatePercent: Int,
    modifier: Modifier = Modifier,
) {
    val gaugeColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val strokeWidth = ChartsConstant.SAVINGS_GAUGE_STROKE_DP.dp

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val centerX = size.width / 2f
            val centerY = size.height * 0.6f
            val radius = minOf(size.width, size.height * 1.5f) * 0.35f

            // Background arc (0-180 degrees)
            drawArc(
                color = backgroundColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Foreground arc (0 to percentage)
            val sweepAngle = (savingsRatePercent / 100f) * 180f
            val arcColor = when {
                savingsRatePercent >= 20 -> Color(0xFF4CAF50)  // Green
                savingsRatePercent >= 10 -> Color(0xFFFFA726)  // Orange
                else -> Color(0xFFF44336)  // Red
            }

            drawArc(
                color = arcColor,
                startAngle = 180f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Center indicator dot
            drawCircle(
                color = gaugeColor,
                radius = 6.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }
    }
}

/**
 * Line chart with points showing daily expense trends.
 * Uses simplified rendering for better performance.
 */
@Composable
internal fun ExpenseLineChart(
    dailyExpenses: List<Double>,
    modifier: Modifier = Modifier,
) {
    if (dailyExpenses.isEmpty()) {
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val padding = 32f
            val chartWidth = size.width - (padding * 2)
            val chartHeight = size.height - (padding * 2)

            if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

            // Find max value for scaling
            val maxValue = dailyExpenses.maxOrNull() ?: 1.0
            if (maxValue <= 0.0) return@Canvas

            val pointSpacing = chartWidth / (dailyExpenses.size - 1).coerceAtLeast(1)

            // Draw grid lines
            repeat(4) { i ->
                val y = padding + (chartHeight / 3f) * i
                drawLine(
                    color = gridColor.copy(alpha = 0.2f),
                    start = Offset(padding, y),
                    end = Offset(size.width - padding, y),
                    strokeWidth = 1f
                )
            }

            // Draw line and points
            val points = mutableListOf<Offset>()
            dailyExpenses.forEachIndexed { index, value ->
                val x = padding + (index * pointSpacing)
                val normalizedValue = (value / maxValue).coerceIn(0.0, 1.0).toFloat()
                val y = size.height - padding - (normalizedValue * chartHeight)
                points.add(Offset(x, y))
            }

            // Draw line connecting points
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = pointColor,
                    radius = 4.5f,
                    center = point
                )
                drawCircle(
                    color = pointColor.copy(alpha = 0.3f),
                    radius = 8f,
                    center = point
                )
            }
        }
    }
}

/**
 * Bar chart showing average expense distribution across days of week (1=Mon...7=Sun).
 * Simplified rendering for performance and type safety.
 */
@Composable
internal fun WeekdayBarChart(
    expenseByWeekday: Map<Int, Double>,
    modifier: Modifier = Modifier,
) {
    if (expenseByWeekday.isEmpty()) {
        return
    }

    val barColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val padding = 32f
            val chartWidth = size.width - (padding * 2)
            val chartHeight = size.height - (padding * 2)

            if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

            // Find max value for scaling
            val maxValue = expenseByWeekday.values.maxOrNull() ?: 1.0
            if (maxValue <= 0.0) return@Canvas

            val barWidth = chartWidth / 7f * 0.7f
            val barSpacing = chartWidth / 7f

            // Draw grid lines
            repeat(4) { i ->
                val y = padding + (chartHeight / 3f) * i
                drawLine(
                    color = gridColor.copy(alpha = 0.2f),
                    start = Offset(padding, y),
                    end = Offset(size.width - padding, y),
                    strokeWidth = 1f
                )
            }

            // Draw bars for each day (1-7)
            (1..7).forEach { day ->
                val expense = expenseByWeekday[day] ?: 0.0
                if (expense > 0.0) {
                    val normalizedHeight = (expense / maxValue).coerceIn(0.0, 1.0).toFloat()
                    val x = padding + (day - 1) * barSpacing + (barSpacing - barWidth) / 2f
                    val barHeight = normalizedHeight * chartHeight
                    val y = size.height - padding - barHeight

                    // Determine color based on expense level
                    val color = when {
                        expense > maxValue * 0.7 -> errorColor
                        expense > maxValue * 0.4 -> tertiaryColor
                        else -> barColor
                    }

                    // Draw bar
                    drawRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )

                    // Highlight for better visibility
                    drawRect(
                        color = Color.White.copy(alpha = 0.2f),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth * 0.3f, barHeight)
                    )
                }
            }
        }
    }
}
