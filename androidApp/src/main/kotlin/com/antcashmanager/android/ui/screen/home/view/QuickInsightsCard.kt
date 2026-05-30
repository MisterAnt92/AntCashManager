package com.antcashmanager.android.ui.screen.home.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AnimatedCard
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.CompactMoneyText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import kotlin.math.abs

@Composable
fun QuickInsightsCard(
    totalIncome: Double,
    totalExpense: Double,
    transactionCount: Int,
    modifier: Modifier = Modifier,
) {
    val averageAmount = if (transactionCount > 0) {
        (abs(totalIncome) + abs(totalExpense)) / transactionCount
    } else {
        0.0
    }
    val savingsRate = if (totalIncome > 0.0) {
        ((totalIncome - totalExpense) / totalIncome) * 100.0
    } else {
        0.0
    }

    AnimatedCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AutoGraph,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                AppText(
                    text = stringResource(R.string.home_quick_insights_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            InsightRow(
                label = stringResource(R.string.home_quick_insights_transactions_count),
                value = transactionCount.toString(),
            )
            InsightRow(
                label = stringResource(R.string.home_quick_insights_average_amount),
                money = averageAmount,
            )
            InsightRow(
                label = stringResource(R.string.home_quick_insights_savings_rate),
                value = stringResource(R.string.home_quick_insights_savings_rate_value, savingsRate),
            )
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String? = null,
    money: Double? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
        )

        if (money != null) {
            CompactMoneyText(
                amount = money,
                fontWeight = FontWeight.Bold,
            )
        } else {
            AppText(
                text = value.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(name = "QuickInsightsCard - Light", showBackground = true)
@Composable
private fun QuickInsightsCardPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        QuickInsightsCard(
            totalIncome = 2500.0,
            totalExpense = 1200.0,
            transactionCount = 24,
        )
    }
}

@Preview(
    name = "QuickInsightsCard - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun QuickInsightsCardPreviewDark() {
    QuickInsightsCardPreviewLight()
}
