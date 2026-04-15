package com.antcashmanager.android.ui.screen.home.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AnimatedCard
import com.antcashmanager.android.ui.components.FadeInOnAppear
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.BalanceText
import com.antcashmanager.android.ui.components.text.MoneyText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.domain.model.PaymentType

@Composable
fun BalanceCard(
    balance: Double,
    modifier: Modifier = Modifier,
    showPaymentTypeBreakdown: Boolean = false,
    balanceByPaymentType: Map<PaymentType, Double> = emptyMap(),
    reduceMotion: Boolean = false,
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

                // Payment Type Breakdown
                AnimatedVisibility(
                    visible = showPaymentTypeBreakdown && balanceByPaymentType.isNotEmpty(),
                    enter = if (reduceMotion) {
                        EnterTransition.None
                    } else {
                        fadeIn(animationSpec = tween(400)) + expandVertically()
                    },
                    exit = if (reduceMotion) {
                        ExitTransition.None
                    } else {
                        fadeOut(animationSpec = tween(400)) + shrinkVertically()
                    },
                ) {
                    PaymentTypeBreakdown(
                        balanceByPaymentType = balanceByPaymentType,
                        reduceMotion = reduceMotion,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeBreakdown(
    balanceByPaymentType: Map<PaymentType, Double>,
    reduceMotion: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppText(
            text = stringResource(R.string.payment_breakdown_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Get ordered payment types (ELECTRONIC, CASH, MEAL_VOUCHERS)
        val orderedPaymentTypes = PaymentType.values().mapNotNull { type ->
            balanceByPaymentType[type]?.let { type to it }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            orderedPaymentTypes.forEachIndexed { index, (paymentType, amount) ->
                if (index > 0) {
                    Spacer(modifier = Modifier.width(12.dp))
                }

                FadeInOnAppear(
                    durationMillis = if (reduceMotion) 0 else 300,
                ) {
                    PaymentTypeItem(
                        paymentType = paymentType,
                        amount = amount,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeItem(
    paymentType: PaymentType,
    amount: Double,
) {
    val (icon, labelRes) = when (paymentType) {
        PaymentType.ELECTRONIC -> Icons.Default.CreditCard to R.string.payment_type_electronic
        PaymentType.CASH -> Icons.Default.Money to R.string.payment_type_cash
        PaymentType.MEAL_VOUCHERS -> Icons.Default.Restaurant to R.string.payment_type_meal_vouchers
    }

    val paymentTypeName = stringResource(labelRes)
    val contentDesc = stringResource(
        R.string.payment_breakdown_item_desc,
        paymentTypeName,
        amount,
    )

    Card(
        modifier = Modifier.semantics {
            contentDescription = contentDesc
            role = Role.Button
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            AppText(
                text = paymentTypeName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(2.dp))
            MoneyText(
                amount = amount,
                fontSize = 12,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
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

@Preview(showBackground = true, name = "Balance Card - With Breakdown (3 Types)")
@Composable
private fun BalanceCardWithBreakdownPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 3500.75,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 2000.50,
                PaymentType.CASH to 1200.25,
                PaymentType.MEAL_VOUCHERS to 300.0,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - With Breakdown (1 Type)")
@Composable
private fun BalanceCardWithBreakdownOneTypePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 1500.00,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 1500.0,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - With Breakdown (Cash Only)")
@Composable
private fun BalanceCardWithBreakdownCashOnlyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 850.50,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.CASH to 850.50,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - Dark Theme with Breakdown")
@Composable
private fun BalanceCardDarkWithBreakdownPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        BalanceCard(
            balance = 2500.75,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 1800.50,
                PaymentType.CASH to 700.25,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - Reduce Motion")
@Composable
private fun BalanceCardReduceMotionPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 1250.00,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 1000.0,
                PaymentType.MEAL_VOUCHERS to 250.0,
            ),
            reduceMotion = true,
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - Breakdown Hidden (Empty)")
@Composable
private fun BalanceCardBreakdownHiddenPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 500.00,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = emptyMap(),
        )
    }
}


