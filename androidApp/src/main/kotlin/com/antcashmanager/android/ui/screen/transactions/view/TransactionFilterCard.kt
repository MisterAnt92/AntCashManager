package com.antcashmanager.android.ui.screen.transactions.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.animation.AnimatedCard
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterCard(
    categories: List<Category>,
    selectedCategory: String?,
    selectedTransactionType: TransactionType?,
    selectedPaymentType: PaymentType?,
    onCategorySelected: (String?) -> Unit,
    onTransactionTypeSelected: (TransactionType?) -> Unit,
    onPaymentTypeSelected: (PaymentType?) -> Unit,
    onClearFilters: () -> Unit,
    hasFilterChanges: Boolean = false,
    onApplyFilters: () -> Unit = {},
    onCancelFilters: () -> Unit = {},
) {
    AnimatedCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header with clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    text = stringResource(R.string.transactions_advanced_filters),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                TextButton(onClick = onClearFilters) {
                    AppText(
                        text = stringResource(R.string.common_clear),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // Transaction Type Filter
            Column {
                AppText(
                    text = stringResource(R.string.transactions_filter_type),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VerticalSpacer(SpacingSize.XXS)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilterChip(
                        selected = selectedTransactionType == null,
                        onClick = { onTransactionTypeSelected(null) },
                        label = { AppText(stringResource(R.string.common_all), maxLines = 1) },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                    )
                    FilterChip(
                        selected = selectedTransactionType == TransactionType.INCOME,
                        onClick = {
                            onTransactionTypeSelected(
                                if (selectedTransactionType == TransactionType.INCOME) {
                                    null
                                } else {
                                    TransactionType.INCOME
                                },
                            )
                        },
                        label = {
                            AppText(
                                stringResource(R.string.transaction_type_income),
                                maxLines = 1,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = IncomeGreen,
                            )
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                            ),
                    )
                    FilterChip(
                        selected = selectedTransactionType == TransactionType.EXPENSE,
                        onClick = {
                            onTransactionTypeSelected(
                                if (selectedTransactionType == TransactionType.EXPENSE) {
                                    null
                                } else {
                                    TransactionType.EXPENSE
                                },
                            )
                        },
                        label = {
                            AppText(
                                stringResource(R.string.transaction_type_expense),
                                maxLines = 1,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ExpenseRed,
                            )
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                            ),
                    )
                }
            }

            // Payment Type Filter
            Column {
                AppText(
                    text = stringResource(R.string.transactions_filter_payment),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VerticalSpacer(SpacingSize.XXS)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilterChip(
                        selected = selectedPaymentType == null,
                        onClick = { onPaymentTypeSelected(null) },
                        label = { AppText(stringResource(R.string.common_all), maxLines = 1) },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                    )
                    PaymentType.values().forEach { paymentType ->
                        FilterChip(
                            selected = selectedPaymentType == paymentType,
                            onClick = {
                                onPaymentTypeSelected(
                                    if (selectedPaymentType == paymentType) null else paymentType,
                                )
                            },
                            label = {
                                AppText(
                                    text =
                                        when (paymentType) {
                                            PaymentType.ELECTRONIC -> stringResource(R.string.payment_type_electronic)
                                            PaymentType.CASH -> stringResource(R.string.payment_type_cash)
                                            PaymentType.MEAL_VOUCHERS ->
                                                stringResource(
                                                    R.string.payment_type_meal_vouchers,
                                                )
                                        },
                                    maxLines = 1,
                                )
                            },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                        )
                    }
                }
            }

            // Category Filter (only show if categories available)
            if (categories.isNotEmpty()) {
                Column {
                    AppText(
                        text = stringResource(R.string.transactions_filter_category),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    VerticalSpacer(SpacingSize.XXS)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelected(null) },
                            label = { AppText(stringResource(R.string.common_all), maxLines = 1) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                        )
                        categories.take(8).forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category.name,
                                onClick = {
                                    onCategorySelected(
                                        if (selectedCategory == category.name) null else category.name,
                                    )
                                },
                                label = { AppText(category.name, maxLines = 1) },
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    ),
                            )
                        }
                    }
                }
            }

            // Action buttons footer
            if (hasFilterChanges) {
                VerticalSpacer(SpacingSize.XS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onCancelFilters,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(40.dp),
                    ) {
                        AppText(
                            text = stringResource(R.string.common_cancel),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    AppButton(
                        onClick = onApplyFilters,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(40.dp),
                    ) {
                        AppText(
                            text = stringResource(R.string.common_confirm),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveFiltersRow(
    searchQuery: String,
    selectedCategory: String?,
    selectedTransactionType: TransactionType?,
    selectedPaymentType: PaymentType?,
    onClearAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (searchQuery.isNotEmpty()) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = {
                        AppText(
                            text =
                                stringResource(
                                    R.string.transactions_search_query_preview,
                                    searchQuery,
                                ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.height(28.dp),
                )
            }
            selectedTransactionType?.let { type ->
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = {
                        AppText(
                            text =
                                when (type) {
                                    TransactionType.INCOME -> stringResource(R.string.transaction_type_income)
                                    TransactionType.EXPENSE -> stringResource(R.string.transaction_type_expense)
                                },
                            maxLines = 1,
                        )
                    },
                    modifier = Modifier.height(28.dp),
                )
            }
            selectedPaymentType?.let { payment ->
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = {
                        AppText(
                            text =
                                when (payment) {
                                    PaymentType.ELECTRONIC -> stringResource(R.string.payment_type_electronic)
                                    PaymentType.CASH -> stringResource(R.string.payment_type_cash)
                                    PaymentType.MEAL_VOUCHERS -> stringResource(R.string.payment_type_meal_vouchers)
                                },
                            maxLines = 1,
                        )
                    },
                    modifier = Modifier.height(28.dp),
                )
            }
            selectedCategory?.let { category ->
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { AppText(category, maxLines = 1) },
                    modifier = Modifier.height(28.dp),
                )
            }
        }

        IconButton(
            onClick = onClearAll,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.common_clear),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
