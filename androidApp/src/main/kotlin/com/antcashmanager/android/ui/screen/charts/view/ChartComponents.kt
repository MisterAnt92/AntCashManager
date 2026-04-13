package com.antcashmanager.android.ui.screen.charts.view

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
    val animDuration = if (reduceMotion) 0 else 800
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "pie"
    )
    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height) * 0.75f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f
        data.entries.forEachIndexed { index, (_, value) ->
            val sweep = (value / total * 360f * animatedProgress).toFloat()
            drawArc(
                color = pieColors[index % pieColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )
            startAngle += sweep
        }
    }
}

@Composable
internal fun PieLegend(data: Map<String, Double>) {
    val total = data.values.sum()
    val fmt = LocalCurrencyFormat.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        data.entries.forEachIndexed { index, (category, value) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(pieColors[index % pieColors.size])
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${formatAmount(value, fmt)} (%.0f%%)".format(value / total * 100),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(incomeColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.charts_income),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.width(20.dp))
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(expenseColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.charts_expenses),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun BarChart(data: List<MonthlyAmount>, modifier: Modifier = Modifier) {
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 800
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurface

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
        val maxHeight = size.height * 0.8f
        val baseY = size.height * 0.85f

        data.forEachIndexed { index, month ->

            val incomeHeight = (month.income / maxValue * maxHeight * animatedProgress).toFloat()
            val expenseHeight = (month.expense / maxValue * maxHeight * animatedProgress).toFloat()
            val x = spacing + index * (barWidth * 2 + spacing * 1.5f)

            // Income bar
            drawRoundRect(
                color = incomeColor,
                topLeft = Offset(x, baseY - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Expense bar
            drawRoundRect(
                color = expenseColor,
                topLeft = Offset(x + barWidth + spacing * 0.5f, baseY - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Month label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    month.label,
                    x + barWidth,
                    baseY + 30f,
                    Paint().apply {
                        color = textColor.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
internal fun YearlyBarChart(data: List<YearlyAmount>, modifier: Modifier = Modifier) {
    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 800
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurface

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "yearlyAnim"
    )

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val maxValue = data.maxOf { maxOf(it.income, it.expense) }.takeIf { it > 0 } ?: 1.0
        val barWidth = size.width / (data.size * 2.5f)
        val spacing = barWidth * 0.5f
        val maxHeight = size.height * 0.8f
        val baseY = size.height * 0.85f

        data.forEachIndexed { index, year ->

            val incomeHeight = (year.income / maxValue * maxHeight * animatedProgress).toFloat()
            val expenseHeight = (year.expense / maxValue * maxHeight * animatedProgress).toFloat()
            val x = spacing + index * (barWidth * 2 + spacing * 1.5f)

            // Income bar
            drawRoundRect(
                color = incomeColor,
                topLeft = Offset(x, baseY - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Expense bar
            drawRoundRect(
                color = expenseColor,
                topLeft = Offset(x + barWidth + spacing * 0.5f, baseY - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Year label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    year.label,
                    x + barWidth,
                    baseY + 30f,
                    Paint().apply {
                        color = textColor.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

