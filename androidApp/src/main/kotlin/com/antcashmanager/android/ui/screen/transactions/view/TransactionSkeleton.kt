package com.antcashmanager.android.ui.screen.transactions.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.animation.SkeletonLoader
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Skeleton loader item for transaction list during loading state.
 */
@Composable
fun TransactionSkeletonItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        SkeletonLoader(height = 16.dp, cornerRadius = 8)
        VerticalSpacer(SpacingSize.XS)
        SkeletonLoader(height = 20.dp, cornerRadius = 8)
    }
}

@Preview(showBackground = true, name = "TransactionSkeletonItem - Light")
@Composable
private fun TransactionSkeletonItemLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionSkeletonItem()
    }
}

@Preview(showBackground = true, name = "TransactionSkeletonItem - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionSkeletonItemDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        TransactionSkeletonItem()
    }
}
