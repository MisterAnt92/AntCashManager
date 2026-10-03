/**
 * Material 3 theme implementation for AntCashManager.
 *
 * Features:
 * - Dynamic color support (Material You on Android 12+)
 * - Dark mode and high contrast modes
 * - Accessibility support (large text, reduced motion)
 * - Runtime theme switching via Composition Locals
 * - 3 theme variants: Default (Light/Dark), Anna theme
 *
 * Usage:
 *   AntCashManagerTheme(darkTheme = isSystemDarkMode) {
 *       MyScreen()
 *   }
 *
 * See: theme/README.md for full documentation
 */
package com.antcashmanager.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = OnPrimaryLight,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = OnPrimaryContainerLight,
        secondary = SecondaryLight,
        onSecondary = OnSecondaryLight,
        secondaryContainer = SecondaryContainerLight,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = TertiaryLight,
        onTertiary = OnTertiaryLight,
        tertiaryContainer = TertiaryContainerLight,
        onTertiaryContainer = OnTertiaryContainerLight,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight,
        scrim = ScrimLight,
        inverseSurface = InverseSurfaceLight,
        inverseOnSurface = InverseOnSurfaceLight,
        inversePrimary = InversePrimaryLight,
        surfaceDim = SurfaceDimLight,
        surfaceBright = SurfaceBrightLight,
        surfaceContainerLowest = SurfaceContainerLowestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = OnPrimaryDark,
        primaryContainer = PrimaryContainerDark,
        onPrimaryContainer = OnPrimaryContainerDark,
        secondary = SecondaryDark,
        onSecondary = OnSecondaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = OnSecondaryContainerDark,
        tertiary = TertiaryDark,
        onTertiary = OnTertiaryDark,
        tertiaryContainer = TertiaryContainerDark,
        onTertiaryContainer = OnTertiaryContainerDark,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
        outlineVariant = OutlineVariantDark,
        scrim = ScrimDark,
        inverseSurface = InverseSurfaceDark,
        inverseOnSurface = InverseOnSurfaceDark,
        inversePrimary = InversePrimaryDark,
        surfaceDim = SurfaceDimDark,
        surfaceBright = SurfaceBrightDark,
        surfaceContainerLowest = SurfaceContainerLowestDark,
        surfaceContainerLow = SurfaceContainerLowDark,
        surfaceContainer = SurfaceContainerDark,
        surfaceContainerHigh = SurfaceContainerHighDark,
        surfaceContainerHighest = SurfaceContainerHighestDark,
    )

private val AnnaLightColorScheme =
    lightColorScheme(
        primary = AnnaPrimaryLight,
        onPrimary = AnnaOnPrimaryLight,
        primaryContainer = AnnaPrimaryContainerLight,
        onPrimaryContainer = AnnaOnPrimaryContainerLight,
        secondary = AnnaSecondaryLight,
        onSecondary = AnnaOnSecondaryLight,
        secondaryContainer = AnnaSecondaryContainerLight,
        onSecondaryContainer = AnnaOnSecondaryContainerLight,
        tertiary = AnnaTertiaryLight,
        onTertiary = AnnaOnTertiaryLight,
        tertiaryContainer = AnnaTertiaryContainerLight,
        onTertiaryContainer = AnnaOnTertiaryContainerLight,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight,
        scrim = ScrimLight,
        inverseSurface = InverseSurfaceLight,
        inverseOnSurface = InverseOnSurfaceLight,
        inversePrimary = AnnaInversePrimaryLight,
        surfaceDim = SurfaceDimLight,
        surfaceBright = SurfaceBrightLight,
        surfaceContainerLowest = SurfaceContainerLowestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
    )

private val AnnaDarkColorScheme =
    darkColorScheme(
        primary = AnnaPrimaryDark,
        onPrimary = AnnaOnPrimaryDark,
        primaryContainer = AnnaPrimaryContainerDark,
        onPrimaryContainer = AnnaOnPrimaryContainerDark,
        secondary = AnnaSecondaryDark,
        onSecondary = AnnaOnSecondaryDark,
        secondaryContainer = AnnaSecondaryContainerDark,
        onSecondaryContainer = AnnaOnSecondaryContainerDark,
        tertiary = AnnaTertiaryDark,
        onTertiary = AnnaOnTertiaryDark,
        tertiaryContainer = AnnaTertiaryContainerDark,
        onTertiaryContainer = AnnaOnTertiaryContainerDark,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
        outlineVariant = OutlineVariantDark,
        scrim = ScrimDark,
        inverseSurface = InverseSurfaceDark,
        inverseOnSurface = InverseOnSurfaceDark,
        inversePrimary = AnnaInversePrimaryDark,
        surfaceDim = SurfaceDimDark,
        surfaceBright = SurfaceBrightDark,
        surfaceContainerLowest = SurfaceContainerLowestDark,
        surfaceContainerLow = SurfaceContainerLowDark,
        surfaceContainer = SurfaceContainerDark,
        surfaceContainerHigh = SurfaceContainerHighDark,
        surfaceContainerHighest = SurfaceContainerHighestDark,
    )

private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

/** True when the Anna theme is active — read by transaction card composables to apply green/red backgrounds. */
val LocalAnnaTheme = compositionLocalOf { false }

@Composable
fun AntCashManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    highContrast: Boolean = false,
    largeText: Boolean = false,
    reduceMotion: Boolean = false,
    useAnnaTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseColorScheme =
        when {
            useAnnaTheme -> if (darkTheme) AnnaDarkColorScheme else AnnaLightColorScheme

            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    // Apply high-contrast overrides when enabled
    val colorScheme =
        if (highContrast) {
            if (darkTheme) {
                baseColorScheme.copy(
                    primary = HighContrastPrimaryDark,
                    onPrimary = HighContrastOnPrimaryDark,
                    primaryContainer = HighContrastPrimaryContainerDark,
                    onPrimaryContainer = HighContrastOnPrimaryContainerDark,
                    background = HighContrastBackgroundDark,
                    onBackground = HighContrastOnBackgroundDark,
                    surface = HighContrastSurfaceDark,
                    onSurface = HighContrastOnSurfaceDark,
                    outline = HighContrastOutlineDark,
                )
            } else {
                baseColorScheme.copy(
                    primary = HighContrastPrimaryLight,
                    onPrimary = HighContrastOnPrimaryLight,
                    primaryContainer = HighContrastPrimaryContainerLight,
                    onPrimaryContainer = HighContrastOnPrimaryContainerLight,
                    background = HighContrastBackgroundLight,
                    onBackground = HighContrastOnBackgroundLight,
                    surface = HighContrastSurfaceLight,
                    onSurface = HighContrastOnSurfaceLight,
                    outline = HighContrastOutlineLight,
                )
            }
        } else {
            baseColorScheme
        }

    val typography = if (largeText) scaledTypography() else AppTypography
    val responsiveTypography = rememberResponsiveTypography()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = AppShapes,
    ) {
        CompositionLocalProvider(
            LocalReduceMotion provides reduceMotion,
            LocalResponsiveTypography provides responsiveTypography,
            LocalAnnaTheme provides useAnnaTheme,
        ) {
            content()
        }
    }
}
