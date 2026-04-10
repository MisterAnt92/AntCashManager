package com.antcashmanager.android.ui.screen.home.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AnimatedCard
import com.antcashmanager.android.ui.components.FadeInOnAppear
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.BalanceText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen

@Composable
fun BalanceCard(
    balance: Double,
    modifier: Modifier = Modifier,
) {
    val balanceColor by animateColorAsState(
        targetValue = if (balance >= 0) IncomeGreen else ExpenseRed,
        animationSpec = tween(600),
        label = "balance_color",
    )

    FadeInOnAppear(durationMillis = 600) {
        AnimatedCard(
            modifier = modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Title with ant and piggy bank
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    AppText(
                        text = "🐜",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppText(
                        text = stringResource(R.string.home_total_balance),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppText(
                        text = "🐷",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                BalanceText(
                    amount = balance,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 32,
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .shadow(
                            elevation = 0.1.dp,
                            shape = RoundedCornerShape(50.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    AppText(
                        text = if (balance >= 0) {
                            stringResource(R.string.home_balance_positive)
                        } else {
                            stringResource(R.string.home_balance_negative)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = balanceColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Balance Card - Positive")
@Composable
private fun BalanceCardPositivePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(balance = 2294.50)
    }
}

@Preview(showBackground = true, name = "Balance Card - Negative")
@Composable
private fun BalanceCardNegativePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(balance = -150.25)
    }
}

@Preview(showBackground = true, name = "Balance Card - Zero")
@Composable
private fun BalanceCardZeroPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(balance = 0.0)
    }
}

