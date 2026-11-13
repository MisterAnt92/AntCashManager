package com.antcashmanager.android.ui.components.profile

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileAvatar(
    profile: Profile,
    isActive: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        profile.colorArgb != 0xFF6650A4L -> Color(profile.colorArgb)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentTint = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val photoBitmap by produceState<ImageBitmap?>(null, profile.photoPath) {
        value = profile.photoPath?.let { path ->
            withContext(Dispatchers.IO) {
                try { BitmapFactory.decodeFile(path)?.asImageBitmap() } catch (_: Exception) { null }
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (photoBitmap != null) {
            Image(
                bitmap = photoBitmap!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (profile.avatarEmoji.isNotEmpty()) {
            AppText(
                text = profile.avatarEmoji,
                style = if (size >= 56.dp) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = contentTint,
                modifier = Modifier.size(if (size >= 56.dp) 32.dp else 26.dp),
            )
        }
    }
}
