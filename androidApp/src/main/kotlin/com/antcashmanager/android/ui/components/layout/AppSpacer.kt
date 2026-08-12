package com.antcashmanager.android.ui.components.layout

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Standardized spacing system following Material Design 8dp grid.
 * All spacing values are multiples of 4dp for consistency.
 */
enum class SpacingSize(val dp: Dp) {
    /** 4.dp - Extra small spacing (tight layouts, compact elements) */
    XXXS(4.dp),

    /** 6.dp - Extra extra small spacing (special cases) */
    XXS(6.dp),

    /** 8.dp - Extra small spacing (compact cards, between elements) */
    XS(8.dp),

    /** 12.dp - Small spacing (card sections, form fields) */
    SM(12.dp),

    /** 16.dp - Medium spacing (default card/component spacing) */
    MD(16.dp),

    /** 20.dp - Medium-Large spacing (prominent sections) */
    ML(20.dp),

    /** 24.dp - Large spacing (major sections, screen padding) */
    LG(24.dp),

    /** 32.dp - Extra large spacing (screen transitions, major dividers) */
    XL(32.dp),

    /** 48.dp - Extra extra large spacing (hero sections) */
    XXL(48.dp),

    /** 80.dp - Extra extra extra large spacing (bottom padding for FAB overlap) */
    XXXL(80.dp),
}

/**
 * Vertical spacer with predefined spacing size.
 *
 * Usage:
 * ```
 * Column {
 *     AppText("Title")
 *     VerticalSpacer(SpacingSize.MD)  // 16.dp spacing
 *     AppText("Content")
 * }
 * ```
 *
 * @param size Predefined spacing size from SpacingSize enum
 */
@Composable
fun VerticalSpacer(size: SpacingSize) {
    Spacer(modifier = Modifier.height(size.dp))
}

/**
 * Horizontal spacer with predefined spacing size.
 *
 * Usage:
 * ```
 * Row {
 *     AppText("Left")
 *     HorizontalSpacer(SpacingSize.SM)  // 12.dp spacing
 *     AppText("Right")
 * }
 * ```
 *
 * @param size Predefined spacing size from SpacingSize enum
 */
@Composable
fun HorizontalSpacer(size: SpacingSize) {
    Spacer(modifier = Modifier.width(size.dp))
}

/**
 * Custom vertical spacer with specific dp value.
 * Use only when predefined sizes don't fit requirements.
 *
 * @param dp Custom height in dp
 */
@Composable
fun VerticalSpacer(dp: Dp) {
    Spacer(modifier = Modifier.height(dp))
}

/**
 * Custom horizontal spacer with specific dp value.
 * Use only when predefined sizes don't fit requirements.
 *
 * @param dp Custom width in dp
 */
@Composable
fun HorizontalSpacer(dp: Dp) {
    Spacer(modifier = Modifier.width(dp))
}

// ── Previews ──

@Preview(name = "AppSpacer - Light", showBackground = true)
@Composable
private fun AppSpacerPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = "Vertical Spacers Demo",
                style = MaterialTheme.typography.titleMedium,
            )

            VerticalSpacer(SpacingSize.XXXS)
            AppText("XXXS - 4.dp", style = MaterialTheme.typography.bodySmall)

            VerticalSpacer(SpacingSize.XS)
            AppText("XS - 8.dp", style = MaterialTheme.typography.bodySmall)

            VerticalSpacer(SpacingSize.SM)
            AppText("SM - 12.dp", style = MaterialTheme.typography.bodySmall)

            VerticalSpacer(SpacingSize.MD)
            AppText("MD - 16.dp (Default)", style = MaterialTheme.typography.bodySmall)

            VerticalSpacer(SpacingSize.LG)
            AppText("LG - 24.dp", style = MaterialTheme.typography.bodySmall)

            VerticalSpacer(SpacingSize.XL)
            AppText("XL - 32.dp", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(
    name = "AppSpacer - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AppSpacerPreviewDark() {
    AppSpacerPreviewLight()
}

@Preview(name = "HorizontalSpacer - Light", showBackground = true, widthDp = 360)
@Composable
private fun HorizontalSpacerPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppText(
                text = "Horizontal Spacers Demo",
                style = MaterialTheme.typography.titleMedium,
            )
            VerticalSpacer(SpacingSize.MD)

            Row {
                AppText("A")
                HorizontalSpacer(SpacingSize.XS)
                AppText("XS")
            }
            VerticalSpacer(SpacingSize.SM)

            Row {
                AppText("B")
                HorizontalSpacer(SpacingSize.SM)
                AppText("SM")
            }
            VerticalSpacer(SpacingSize.SM)

            Row {
                AppText("C")
                HorizontalSpacer(SpacingSize.MD)
                AppText("MD")
            }
            VerticalSpacer(SpacingSize.SM)

            Row {
                AppText("D")
                HorizontalSpacer(SpacingSize.LG)
                AppText("LG")
            }
        }
    }
}

@Preview(
    name = "HorizontalSpacer - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HorizontalSpacerPreviewDark() {
    HorizontalSpacerPreviewLight()
}

