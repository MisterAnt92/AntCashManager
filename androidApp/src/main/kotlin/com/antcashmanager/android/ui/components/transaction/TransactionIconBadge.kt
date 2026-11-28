package com.antcashmanager.android.ui.components.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.categories.view.categoryIconMap
import com.antcashmanager.android.ui.theme.expenseColor
import com.antcashmanager.android.ui.theme.incomeColor
import com.antcashmanager.domain.model.TransactionDisplayType

/**
 * Shared icon badge for transaction list items (trend arrow or category icon).
 * Used by both [TransactionItem] and [RecentTransactionItem].
 */
@Composable
fun TransactionIconBadge(
    isIncome: Boolean,
    displayType: TransactionDisplayType,
    categoryIcon: String,
    categoryColor: Long,
    categoryName: String,
    categoryInitial: String,
) {
    when (displayType) {
        TransactionDisplayType.TREND -> {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(
                            (if (isIncome) incomeColor() else expenseColor()).copy(alpha = 0.25f),
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                        .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (isIncome) incomeColor() else expenseColor(),
                    modifier = Modifier.size(20.dp),
                )
            }
            HorizontalSpacer(SpacingSize.SM)
        }

        TransactionDisplayType.CATEGORY -> {
            val categoryIconVector = categoryIconMap[categoryIcon]
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(
                            color = Color(categoryColor),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                if (categoryIconVector != null) {
                    Icon(
                        imageVector = categoryIconVector,
                        contentDescription = categoryName,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    AppText(
                        text = categoryInitial,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HorizontalSpacer(SpacingSize.SM)
        }

        TransactionDisplayType.NONE -> {
            // No icon
        }
    }
}
