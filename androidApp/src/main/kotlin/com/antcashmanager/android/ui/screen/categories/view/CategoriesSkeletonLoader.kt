package com.antcashmanager.android.ui.screen.categories.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.animation.SkeletonLoader
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
internal fun CategoriesSkeletonLoader(
    horizontalPadding: Dp = 16.dp,
    itemCount: Int = 6,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
    ) {
        VerticalSpacer(SpacingSize.SM)
        // Tab row skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SkeletonLoader(modifier = Modifier.weight(1f), height = 36.dp, cornerRadius = 8)
            SkeletonLoader(modifier = Modifier.weight(1f), height = 36.dp, cornerRadius = 8)
        }
        VerticalSpacer(SpacingSize.MD)
        repeat(itemCount) {
            CategorySkeletonItem()
            VerticalSpacer(SpacingSize.XS)
        }
    }
}

@Composable
private fun CategorySkeletonItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Category icon
        SkeletonLoader(modifier = Modifier.size(40.dp), height = 40.dp, cornerRadius = 50)
        HorizontalSpacer(SpacingSize.MD)
        // Category name + type
        Column(modifier = Modifier.weight(1f)) {
            SkeletonLoader(modifier = Modifier.fillMaxWidth(0.55f), height = 16.dp, cornerRadius = 8)
            VerticalSpacer(SpacingSize.XXS)
            SkeletonLoader(modifier = Modifier.fillMaxWidth(0.35f), height = 12.dp, cornerRadius = 8)
        }
        HorizontalSpacer(SpacingSize.SM)
        // Action icons area
        SkeletonLoader(modifier = Modifier.size(24.dp), height = 24.dp, cornerRadius = 50)
    }
}

@Preview(name = "CategoriesSkeletonLoader - Light", showBackground = true)
@Preview(
    name = "CategoriesSkeletonLoader - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "CategoriesSkeletonLoader - 2x font",
    showBackground = true,
    fontScale = 2.0f,
)
@Composable
private fun CategoriesSkeletonLoaderPreview() {
    AntCashManagerTheme {
        CategoriesSkeletonLoader()
    }
}
