package com.antcashmanager.android.ui.components.profile

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
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
                style = MaterialTheme.typography.titleLarge.copy(fontSize = (size.value * 0.50f).sp),
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

@Composable
private fun ProfileAvatarPreviewContent() {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatar(
            profile = Profile(name = "Default", uuid = "1", isActive = true),
            isActive = true,
            size = 40.dp,
        )
        ProfileAvatar(
            profile = Profile(name = "Personal", uuid = "2", isActive = false),
            isActive = false,
            size = 40.dp,
        )
        ProfileAvatar(
            profile = Profile(name = "Emoji Active", uuid = "3", avatarEmoji = "🦊"),
            isActive = true,
            size = 40.dp,
        )
        ProfileAvatar(
            profile = Profile(name = "Emoji Inactive", uuid = "4", avatarEmoji = "💼"),
            isActive = false,
            size = 40.dp,
        )
        ProfileAvatar(
            profile = Profile(name = "Custom Color", uuid = "5", colorArgb = 0xFF4CAF50L),
            isActive = false,
            size = 56.dp,
        )
    }
}

@Preview(name = "ProfileAvatar - Light", showBackground = true)
@Composable
private fun ProfileAvatarPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        ProfileAvatarPreviewContent()
    }
}

@Preview(
    name = "ProfileAvatar - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProfileAvatarPreviewDark() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ProfileAvatarPreviewContent()
    }
}

@Preview(name = "ProfileAvatar - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun ProfileAvatarPreviewAccessibility() {
    AntCashManagerTheme(dynamicColor = false) {
        ProfileAvatarPreviewContent()
    }
}
