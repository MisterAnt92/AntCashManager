package com.antcashmanager.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo

/**
 * Responsive typography system that scales font sizes based on screen size.
 *
 * - Compact (phones < 600dp): 1.0x scale (baseline)
 * - Medium (7" tablets 600-839dp): 1.1x scale (10% larger)
 * - Expanded (10"+ tablets >= 840dp): 1.2x scale (20% larger)
 *
 * This ensures that text remains readable on all screen sizes while maintaining
 * proper hierarchy and relationships between different text styles.
 *
 * Usage:
 * ```kotlin
 * val typography = LocalResponsiveTypography.current
 * Text("Hello", style = typography.bodyLarge)
 * ```
 */
@Composable
fun rememberResponsiveTypography(): Typography {
    val adaptiveInfo = rememberAdaptiveLayoutInfo()

    val scale = when {
        adaptiveInfo.isExpanded -> 1.2f  // 10"+ tablets: +20%
        adaptiveInfo.isMedium -> 1.1f    // 7" tablets: +10%
        else -> 1.0f                      // Phones: baseline
    }

    // Helper function to scale text style
    fun scaleTextStyle(base: TextStyle): TextStyle {
        return base.copy(
            fontSize = base.fontSize * scale,
            lineHeight = base.lineHeight * scale,
            letterSpacing = base.letterSpacing * scale
        )
    }

    // Get base typography
    val baseTypography = Typography()

    // Scale all styles
    return baseTypography.copy(
        // Display styles (large headlines)
        displayLarge = scaleTextStyle(baseTypography.displayLarge),
        displayMedium = scaleTextStyle(baseTypography.displayMedium),
        displaySmall = scaleTextStyle(baseTypography.displaySmall),

        // Headline styles
        headlineLarge = scaleTextStyle(baseTypography.headlineLarge),
        headlineMedium = scaleTextStyle(baseTypography.headlineMedium),
        headlineSmall = scaleTextStyle(baseTypography.headlineSmall),

        // Title styles
        titleLarge = scaleTextStyle(baseTypography.titleLarge),
        titleMedium = scaleTextStyle(baseTypography.titleMedium),
        titleSmall = scaleTextStyle(baseTypography.titleSmall),

        // Body styles
        bodyLarge = scaleTextStyle(baseTypography.bodyLarge),
        bodyMedium = scaleTextStyle(baseTypography.bodyMedium),
        bodySmall = scaleTextStyle(baseTypography.bodySmall),

        // Label styles
        labelLarge = scaleTextStyle(baseTypography.labelLarge),
        labelMedium = scaleTextStyle(baseTypography.labelMedium),
        labelSmall = scaleTextStyle(baseTypography.labelSmall),
    )
}

/**
 * CompositionLocal for accessing responsive typography from any Composable.
 *
 * Provides font sizes that scale automatically based on screen size,
 * without having to pass them through multiple composition levels.
 *
 * Usage:
 * ```kotlin
 * val typography = LocalResponsiveTypography.current
 * Text(text, style = typography.bodyLarge)
 * ```
 */
val LocalResponsiveTypography = compositionLocalOf { Typography() }
