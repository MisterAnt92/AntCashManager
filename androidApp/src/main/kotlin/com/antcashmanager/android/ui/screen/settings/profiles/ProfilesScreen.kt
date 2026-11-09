package com.antcashmanager.android.ui.screen.settings.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.Profile
import org.koin.androidx.compose.koinViewModel

private val PROFILE_COLORS = listOf(
    0xFFE57373L, 0xFFFF8A65L, 0xFFFFB74DL, 0xFF81C784L,
    0xFF4DB6ACL, 0xFF64B5F6L, 0xFF7986CBL, 0xFFBA68C8L,
    0xFFF06292L, 0xFF90A4AEL,
)

private val PROFILE_EMOJIS = listOf("👤", "👑", "⭐", "💰", "🏠", "✈️", "🛒", "💼", "🎯", "🌟", "❤️", "🔑")

@Composable
fun ProfilesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfilesViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfilesContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = { navController.popBackStack() },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilesContent(
    state: ProfilesState,
    onEvent: (ProfilesEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(ProfilesEvent.DismissError)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { AppText(text = stringResource(R.string.profiles_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(ProfilesEvent.ShowCreateDialog) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.profiles_add_button))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val activeProfile = state.profiles.firstOrNull { it.id == state.activeProfileId }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    if (activeProfile != null) {
                        item(key = "active_header") {
                            ActiveProfileHeader(profile = activeProfile)
                            VerticalSpacer(SpacingSize.SM)
                        }
                    }

                    items(state.profiles, key = { it.id }) { profile ->
                        ProfileItem(
                            profile = profile,
                            isActive = profile.id == state.activeProfileId,
                            onSwitch = { onEvent(ProfilesEvent.SwitchProfile(profile)) },
                            onDelete = { onEvent(ProfilesEvent.DeleteProfile(profile)) },
                        )
                    }
                }
            }
        }
    }

    state.showSwitchConfirmation?.let { target ->
        AlertDialog(
            onDismissRequest = { onEvent(ProfilesEvent.DismissSwitch) },
            title = { AppText(text = stringResource(R.string.profiles_switch_dialog_title), style = MaterialTheme.typography.titleMedium) },
            text = { AppText(text = stringResource(R.string.profiles_switch_dialog_message, target.name)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProfilesEvent.ConfirmSwitch) }) {
                    AppText(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfilesEvent.DismissSwitch) }) {
                    AppText(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }

    state.showDeleteConfirmation?.let { target ->
        AlertDialog(
            onDismissRequest = { onEvent(ProfilesEvent.DismissDelete) },
            title = { AppText(text = stringResource(R.string.profiles_delete_dialog_title), style = MaterialTheme.typography.titleMedium) },
            text = { AppText(text = stringResource(R.string.profiles_delete_dialog_warning, target.name)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProfilesEvent.ConfirmDelete) }) {
                    AppText(text = stringResource(android.R.string.ok), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfilesEvent.DismissDelete) }) {
                    AppText(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }

    if (state.showCreateDialog) {
        CreateProfileDialog(
            onConfirm = { name, color, emoji ->
                onEvent(ProfilesEvent.CreateProfile(name, color, emoji))
            },
            onDismiss = { onEvent(ProfilesEvent.DismissCreateDialog) },
        )
    }
}

@Composable
private fun ActiveProfileHeader(
    profile: Profile,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(
                profile = profile,
                isActive = true,
                size = 56.dp,
            )
            HorizontalSpacer(SpacingSize.MD)
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                VerticalSpacer(SpacingSize.XXXS)
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                ) {
                    AppText(
                        text = stringResource(R.string.profiles_active_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun ProfileItem(
    profile: Profile,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onSwitch,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(
                profile = profile,
                isActive = isActive,
                size = 48.dp,
            )
            HorizontalSpacer(SpacingSize.MD)
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                )
                if (isActive) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        AppText(
                            text = stringResource(R.string.profiles_active_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            if (isActive) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (profile.id != 1L) {
                HorizontalSpacer(SpacingSize.XS)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.profiles_delete_dialog_title),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    profile: Profile,
    isActive: Boolean,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        profile.colorArgb != 0xFF6650A4L -> Color(profile.colorArgb)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentTint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (profile.avatarEmoji.isNotEmpty()) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateProfileDialog(
    onConfirm: (name: String, colorArgb: Long, avatarEmoji: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PROFILE_COLORS.first()) }
    var selectedEmoji by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = stringResource(R.string.profiles_create_dialog_title), style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { AppText(text = stringResource(R.string.profiles_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank()) onConfirm(name.trim(), selectedColor, selectedEmoji)
                    }),
                )

                Column {
                    AppText(
                        text = "Colore",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    VerticalSpacer(SpacingSize.XS)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 5,
                    ) {
                        PROFILE_COLORS.forEach { colorValue ->
                            ColorSwatch(
                                colorValue = colorValue,
                                isSelected = colorValue == selectedColor,
                                onClick = { selectedColor = colorValue },
                            )
                        }
                    }
                }

                Column {
                    AppText(
                        text = "Emoji",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    VerticalSpacer(SpacingSize.XS)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        maxItemsInEachRow = 6,
                    ) {
                        PROFILE_EMOJIS.forEach { emoji ->
                            val isSelected = emoji == selectedEmoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    )
                                    .clickable { selectedEmoji = if (isSelected) "" else emoji },
                                contentAlignment = Alignment.Center,
                            ) {
                                AppText(text = emoji, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), selectedColor, selectedEmoji) },
                enabled = name.isNotBlank(),
            ) {
                AppText(text = stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText(text = stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun ColorSwatch(
    colorValue: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(colorValue))
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
