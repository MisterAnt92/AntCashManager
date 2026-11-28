package com.antcashmanager.android.ui.screen.charts.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.animation.SkeletonLoader
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
internal fun ChartsSkeletonLoader(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(SpacingSize.SM.dp),
    ) {
        // PeriodFilter chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingSize.XS.dp)) {
                repeat(5) {
                    SkeletonLoader(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        cornerRadius = 18,
                    )
                }
            }
        }

        // SummaryRow: 3 equal cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingSize.XS.dp)) {
                repeat(3) {
                    SkeletonLoader(
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp),
                        cornerRadius = 16,
                    )
                }
            }
        }

        item { VerticalSpacer(SpacingSize.XS) }

        // ChartCard 1 — pie chart placeholder
        item {
            SkeletonLoader(
                modifier = Modifier.fillMaxWidth(),
                height = 220.dp,
                cornerRadius = 16,
            )
        }

        // ChartCard 2 — bar chart placeholder
        item {
            SkeletonLoader(
                modifier = Modifier.fillMaxWidth(),
                height = 200.dp,
                cornerRadius = 16,
            )
        }

        // ChartCard 3 — top categories placeholder
        item {
            SkeletonLoader(
                modifier = Modifier.fillMaxWidth(),
                height = 160.dp,
                cornerRadius = 16,
            )
        }

        item { VerticalSpacer(SpacingSize.XXL) }
    }
}

@Preview(name = "ChartsSkeletonLoader - Light", showBackground = true)
@Composable
private fun ChartsSkeletonLoaderPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        ChartsSkeletonLoader()
    }
}

@Preview(
    name = "ChartsSkeletonLoader - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChartsSkeletonLoaderPreviewDark() {
    AntCashManagerTheme(dynamicColor = false, darkTheme = true) {
        ChartsSkeletonLoader()
    }
}

@Preview(
    name = "ChartsSkeletonLoader - 2x Font",
    showBackground = true,
    fontScale = 2.0f,
)
@Composable
private fun ChartsSkeletonLoaderPreview2xFont() {
    AntCashManagerTheme(dynamicColor = false) {
        ChartsSkeletonLoader()
    }
}
