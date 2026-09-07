package com.antcashmanager.android.ui.screen.transactions.addImport.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.card.AppCategoryCard
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType
import java.util.Locale

@Composable
internal fun CategorySelectionDialog(
    categories: List<Category>,
    selectedCategory: Category?,
    onSelectCategory: (Category) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                text = stringResource(R.string.add_transaction_select_category),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(categories) { category ->
                    val typeLabel =
                        when (category.type.uppercase(Locale.ROOT)) {
                            "INCOME" -> stringResource(R.string.add_transaction_income_label)
                            else -> stringResource(R.string.add_transaction_expense_label)
                        }

                    AppCategoryCard(
                        category = category,
                        isSelected = selectedCategory?.id == category.id,
                        onClick = { onSelectCategory(category) },
                        subtitle = typeLabel,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
internal fun TypeSelectionDialog(
    selectedType: TransactionType?,
    onSelectType: (TransactionType) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                text = stringResource(R.string.add_transaction_select_type),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TransactionType.entries.forEach { type ->
                    TypeRadioButton(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = { onSelectType(type) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
internal fun TypeRadioButton(
    type: TransactionType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ).clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        val typeLabel =
            when (type) {
                TransactionType.INCOME -> stringResource(R.string.add_transaction_income_label)
                TransactionType.EXPENSE -> stringResource(R.string.add_transaction_expense_label)
            }
        AppText(
            text = typeLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun PaymentTypeSelectionDialog(
    selectedPaymentType: PaymentType?,
    onSelectPaymentType: (PaymentType) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                text = stringResource(R.string.add_transaction_select_payment_type),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PaymentType.entries.forEach { paymentType ->
                    PaymentTypeRadioButton(
                        paymentType = paymentType,
                        isSelected = selectedPaymentType == paymentType,
                        onClick = { onSelectPaymentType(paymentType) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
internal fun PaymentTypeRadioButton(
    paymentType: PaymentType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ).clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        val paymentTypeLabel =
            when (paymentType) {
                PaymentType.CASH -> stringResource(R.string.add_transaction_payment_type_cash)
                PaymentType.ELECTRONIC -> stringResource(R.string.add_transaction_payment_type_electronic)
                PaymentType.MEAL_VOUCHERS -> stringResource(R.string.add_transaction_payment_type_meal_vouchers)
            }
        AppText(
            text = paymentTypeLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}
