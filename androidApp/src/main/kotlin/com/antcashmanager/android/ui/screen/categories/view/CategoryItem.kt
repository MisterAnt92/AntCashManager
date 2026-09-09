package com.antcashmanager.android.ui.screen.categories.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.util.translateCategory
import com.antcashmanager.domain.model.Category

@Composable
fun CategoryItem(
    category: Category,
    onDelete: () -> Unit,
    onToggleHidden: () -> Unit,
    onClick: (() -> Unit)? = null,
) {
    val icon = categoryIconMap[category.icon]
    val translatedName = translateCategory(category.name)

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .let { if (onClick != null) it.clickable { onClick() } else it },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(category.color)),
                contentAlignment = Alignment.Center,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                } else {
                    AppText(
                        text = translatedName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HorizontalSpacer(SpacingSize.MD)
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = translatedName,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (category.isDefault) {
                    AppText(
                        text = stringResource(R.string.categories_default_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = onToggleHidden) {
                Icon(
                    imageVector = if (category.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription =
                        if (category.isHidden) {
                            stringResource(R.string.categories_show)
                        } else {
                            stringResource(R.string.categories_hide)
                        },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!category.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.dialog_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
