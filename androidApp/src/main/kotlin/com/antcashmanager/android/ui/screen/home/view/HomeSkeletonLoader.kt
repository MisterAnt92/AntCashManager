package com.antcashmanager.android.ui.screen.home.view

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
internal fun HomeSkeletonLoader(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(SpacingSize.SM.dp),
    ) {
        // DateRangeFilter chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingSize.XS.dp)) {
                repeat(4) {
                    SkeletonLoader(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        cornerRadius = 16,
                    )
                }
            }
        }

        item { VerticalSpacer(SpacingSize.XS) }

        // BalanceCard
        item {
            SkeletonLoader(
                modifier = Modifier.fillMaxWidth(),
                height = 110.dp,
                cornerRadius = 16,
            )
        }

        // IncomeExpense row
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingSize.XS.dp)) {
                SkeletonLoader(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    cornerRadius = 16,
                )
                SkeletonLoader(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    cornerRadius = 16,
                )
            }
        }

        // QuickInsights card
        item {
            SkeletonLoader(
                modifier = Modifier.fillMaxWidth(),
                height = 100.dp,
                cornerRadius = 16,
            )
        }

        item { VerticalSpacer(SpacingSize.XS) }

        // Label placeholder
        item {
            SkeletonLoader(
                modifier = Modifier.fillMaxWidth(0.4f),
                height = 14.dp,
                cornerRadius = 4,
            )
        }

        item { VerticalSpacer(SpacingSize.XXXS) }

        // Transaction rows
        items(5) {
            SkeletonLoader(height = 16.dp, cornerRadius = 8)
            VerticalSpacer(SpacingSize.XXS)
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingSize.XS.dp)) {
                SkeletonLoader(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp),
                    cornerRadius = 6,
                )
                SkeletonLoader(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp),
                    cornerRadius = 6,
                )
            }
            VerticalSpacer(SpacingSize.XXS)
            SkeletonLoader(height = 20.dp, cornerRadius = 8)
            VerticalSpacer(SpacingSize.SM)
        }

        item { VerticalSpacer(SpacingSize.XXL) }
    }
}

@Preview(name = "HomeSkeletonLoader - Light", showBackground = true)
@Composable
private fun HomeSkeletonLoaderPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeSkeletonLoader()
    }
}

@Preview(
    name = "HomeSkeletonLoader - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeSkeletonLoaderPreviewDark() {
    AntCashManagerTheme(dynamicColor = false, darkTheme = true) {
        HomeSkeletonLoader()
    }
}

@Preview(
    name = "HomeSkeletonLoader - 2x Font",
    showBackground = true,
    fontScale = 2.0f,
)
@Composable
private fun HomeSkeletonLoaderPreview2xFont() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeSkeletonLoader()
    }
}
