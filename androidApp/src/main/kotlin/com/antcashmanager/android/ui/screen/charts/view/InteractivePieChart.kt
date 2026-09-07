package com.antcashmanager.android.ui.screen.charts.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.antcashmanager.android.ui.theme.LocalReduceMotion
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Interactive pie chart with tap detection for category selection.
 *
 * Features:
 * - Tap detection on individual slices
 * - Callback with selected category and amount
 * - Animated arc drawing
 * - Color-coded segments
 * - Touch feedback through selection callback
 *
 * The chart converts tap coordinates to angles and determines which slice was tapped.
 * The minimum tap area is the outer radius minus stroke width.
 *
 * @param data Map of category names to amounts
 * @param onCategorySelected Callback when a slice is tapped: (categoryName, amount, colorHex)
 * @param modifier Composable modifier
 */
@Composable
internal fun InteractivePieChart(
    data: Map<String, Double>,
    onCategorySelected: (categoryName: String, amount: Double, colorHex: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = data.values.sum()
    if (total == 0.0) return

    val reduceMotion = LocalReduceMotion.current
    val animDuration = if (reduceMotion) 0 else 1200
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animDuration),
        label = "interactivePie",
    )

    // Pre-calculate slice angles and categories for tap detection
    val sliceInfo: List<SliceInfo> =
        run {
            var startAngle = -90f
            data.entries.map { (category, value) ->
                val sweep = (value / total * 360f).toFloat()
                val sliceData =
                    SliceInfo(
                        category = category,
                        amount = value,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        colorIndex = data.keys.indexOf(category),
                    )
                startAngle += sweep
                sliceData
            }
        }

    Box(
        modifier =
            modifier.pointerInput(data) {
                detectTapGestures { tapOffset ->
                    // Calculate canvas size from drag area
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    handlePieChartTap(
                        tapOffset = tapOffset,
                        canvasWidth = canvasWidth,
                        canvasHeight = canvasHeight,
                        slices = sliceInfo,
                        onCategorySelected = onCategorySelected,
                    )
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            val outerRadius = minOf(size.width, size.height) * 0.38f
            val innerRadius = outerRadius * 0.5f // Creates donut effect (hole in center)
            val center = Offset(size.width / 2f, size.height / 2f)

            // Shadow layer
            val shadowRadius = outerRadius + 8f
            drawCircle(
                color = Color.Black.copy(alpha = 0.1f),
                radius = shadowRadius,
                center = center.copy(x = center.x + 4f, y = center.y + 6f),
            )

            // Draw each slice (donut chart style - no divisor lines)
            sliceInfo.forEachIndexed { _, slice ->
                val baseColor = pieColors[slice.colorIndex % pieColors.size]
                val sweep = slice.sweepAngle * animatedProgress

                // Draw outer arc with gradient for visual depth
                val arcBrush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                baseColor.copy(alpha = 0.9f),
                                baseColor.copy(alpha = 0.7f),
                                baseColor.copy(alpha = 0.8f),
                            ),
                        center = center,
                        radius = outerRadius,
                    )

                drawArc(
                    brush = arcBrush,
                    startAngle = slice.startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = outerRadius - innerRadius),
                )
            }

            // Center circle (hole in donut) with gradient
            val centerBrush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.8f),
                            Color.Gray.copy(alpha = 0.1f),
                        ),
                    center = center,
                    radius = innerRadius,
                )
            drawCircle(
                brush = centerBrush,
                radius = innerRadius,
                center = center,
            )

            // Center circle border
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 1f),
            )
        }
    }
}

/**
 * Data class representing a single slice of the pie chart.
 */
private data class SliceInfo(
    val category: String,
    val amount: Double,
    val startAngle: Float,
    val sweepAngle: Float,
    val colorIndex: Int,
) {
    /**
     * Check if a tap at the given angle falls within this slice.
     * Accounts for angle wraparound at -180/+180 degrees.
     */
    fun containsAngle(tapAngle: Float): Boolean {
        val normalizedTapAngle = normalizeAngle(tapAngle)
        val normalizedStart = normalizeAngle(startAngle)
        val endAngle = normalizeAngle(startAngle + sweepAngle)

        return if (normalizedStart <= endAngle) {
            normalizedTapAngle in normalizedStart..endAngle
        } else {
            // Slice wraps around -180/+180
            normalizedTapAngle >= normalizedStart || normalizedTapAngle <= endAngle
        }
    }

    private fun normalizeAngle(angle: Float): Float {
        var normalized = angle % 360f
        if (normalized < 0) normalized += 360f
        return normalized
    }
}

/**
 * Handle tap on pie chart and determine which slice was tapped.
 */
private fun handlePieChartTap(
    tapOffset: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    slices: List<SliceInfo>,
    onCategorySelected: (String, Double, Long) -> Unit,
) {
    val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
    val radius = min(canvasWidth, canvasHeight) * 0.38f

    // Calculate distance from center
    val dx = tapOffset.x - center.x
    val dy = tapOffset.y - center.y
    val distance = sqrt(dx * dx + dy * dy)

    // Check if tap is within the pie chart
    if (distance > radius) {
        return
    }

    // Calculate angle of tap (in degrees, with -90 being the top)
    val tapAngle = (atan2(dy, dx) * 180f / Math.PI).toFloat() - 90f

    // Find which slice was tapped
    for (slice in slices) {
        if (slice.containsAngle(tapAngle)) {
            val colorIndex = slice.colorIndex
            val colorHex =
                pieColors[colorIndex % pieColors.size].let { color ->
                    // Convert Compose Color to hex long
                    val argb = color.value.toLong()
                    argb
                }
            onCategorySelected(slice.category, slice.amount, colorHex)
            return
        }
    }
}

// Color palette used for slices (matches ChartComponents)
private val pieColors =
    listOf(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFFFB74D),
        Color(0xFFBA68C8),
        Color(0xFF4FC3F7),
        Color(0xFFF06292),
        Color(0xFFDCE775),
        Color(0xFF4DB6AC),
        Color(0xFF7986CB),
        Color(0xFFA1887F),
        Color(0xFF90A4AE),
    )
