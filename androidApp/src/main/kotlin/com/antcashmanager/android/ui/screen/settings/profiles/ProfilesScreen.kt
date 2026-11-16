package com.antcashmanager.android.ui.screen.settings.profiles

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.common.AppIcon
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.profile.ProfileAvatar
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

private val PROFILE_COLORS = listOf(
    // Reds
    0xFFEF5350L, 0xFFE57373L,
    // Oranges
    0xFFFF7043L, 0xFFFF8A65L,
    // Ambers
    0xFFFFA726L, 0xFFFFB74DL,
    // Greens
    0xFF66BB6AL, 0xFF81C784L,
    // Teals
    0xFF26A69AL, 0xFF4DB6ACL,
    // Blues
    0xFF42A5F5L, 0xFF64B5F6L,
    // Indigos
    0xFF5C6BC0L, 0xFF7986CBL,
    // Purples
    0xFF8E24AAL, 0xFFBA68C8L,
    // Pinks
    0xFFE91E63L, 0xFFF06292L,
    // Neutrals
    0xFF795548L, 0xFF78909CL,
)

private val PROFILE_EMOJIS = listOf(
    // Persone generiche
    "👤", "👩", "👨", "🧑", "👦", "👧",
    // Professionisti
    "👩‍💼", "👨‍💼", "👩‍🎓", "👨‍🎓", "👩‍💻", "👨‍💻",
    // Ruoli speciali
    "👷", "👮", "🤵", "👸", "🕵️", "🧙",
    // Personaggi / fantasia
    "👼", "🧚", "🦸", "🤴", "🎅", "🤶",
    // Animali (popolari come avatar)
    "🐱", "🐶", "🦊", "🐻", "🦁", "🐧",
)

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
                            ActiveProfileHeader(
                                profile = activeProfile,
                                onEdit = { onEvent(ProfilesEvent.ShowEditDialog(activeProfile)) },
                            )
                            VerticalSpacer(SpacingSize.SM)
                        }
                    }

                    items(state.profiles, key = { it.id }) { profile ->
                        ProfileItem(
                            profile = profile,
                            isActive = profile.id == state.activeProfileId,
                            onSwitch = { onEvent(ProfilesEvent.SwitchProfile(profile)) },
                            onEdit = { onEvent(ProfilesEvent.ShowEditDialog(profile)) },
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
        ProfileFormDialog(
            initialProfile = null,
            titleRes = R.string.profiles_create_dialog_title,
            defaultShareTransactions = true,
            shareTransactionsDescriptionRes = R.string.profiles_share_transactions_create_description,
            onShareTransactionsToggle = {},
            onConfirm = { name, color, emoji, photoUri, _, shareTransactions ->
                onEvent(ProfilesEvent.CreateProfile(name, color, emoji, photoUri, shareTransactions))
            },
            onDismiss = { onEvent(ProfilesEvent.DismissCreateDialog) },
        )
    }

    state.showEditDialog?.let { profile ->
        ProfileFormDialog(
            initialProfile = profile,
            titleRes = R.string.profiles_edit_dialog_title,
            onConfirm = { name, color, emoji, photoUri, clearPhoto, shareTransactions ->
                onEvent(
                    ProfilesEvent.UpdateProfile(
                        profile = profile.copy(
                            name = name,
                            colorArgb = color,
                            avatarEmoji = emoji,
                            shareTransactions = shareTransactions,
                        ),
                        newPhotoUri = photoUri,
                        clearPhoto = clearPhoto,
                    ),
                )
                onEvent(ProfilesEvent.DismissEditDialog)
            },
            onDismiss = { onEvent(ProfilesEvent.DismissEditDialog) },
            onShareTransactionsToggle = if (state.profiles.size > 1) { enabled ->
                onEvent(ProfilesEvent.ToggleShareTransactions(profile, enabled))
            } else null,
        )
    }
}

@Composable
private fun ActiveProfileHeader(
    profile: Profile,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(profile = profile, isActive = true, size = 56.dp)
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.profiles_edit_dialog_title),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ProfileItem(
    profile: Profile,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onSwitch,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(profile = profile, isActive = isActive, size = 48.dp)
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
                AppIcon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            HorizontalSpacer(SpacingSize.XS)
            // Edit button — available for all profiles
            ActionIconButton(
                onClick = onEdit,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            ) {
                AppIcon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.profiles_edit_dialog_title),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            // Delete button — hidden for the Default profile (id=1)
            if (profile.id != 1L) {
                HorizontalSpacer(SpacingSize.XXS)
                ActionIconButton(
                    onClick = onDelete,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                ) {
                    AppIcon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.profiles_delete_dialog_title),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileFormDialog(
    initialProfile: Profile?,
    titleRes: Int,
    onConfirm: (name: String, colorArgb: Long, avatarEmoji: String, photoUri: Uri?, clearPhoto: Boolean, shareTransactions: Boolean) -> Unit,
    onDismiss: () -> Unit,
    defaultShareTransactions: Boolean = false,
    shareTransactionsDescriptionRes: Int = R.string.profiles_share_transactions_description,
    onShareTransactionsToggle: ((Boolean) -> Unit)? = null,
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var selectedColor by remember { mutableLongStateOf(initialProfile?.colorArgb ?: PROFILE_COLORS.first()) }
    var selectedEmoji by remember { mutableStateOf(initialProfile?.avatarEmoji ?: "") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    // true quando l'utente ha esplicitamente scelto un emoji o rimosso la foto:
    // impedisce che la foto salvata ritorni se l'emoji viene de-selezionata
    var photoCleared by remember { mutableStateOf(false) }
    var shareTransactionsChecked by remember {
        mutableStateOf(initialProfile?.shareTransactions ?: defaultShareTransactions)
    }

    val context = LocalContext.current
    val pickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            selectedEmoji = "" // photo overrides emoji
            photoCleared = false // nuova foto: annulla eventuale clearPhoto precedente
        }
    }

    // Load bitmap preview: new URI takes priority over existing saved photo path.
    // selectedEmoji e photoCleared sono chiavi: quando cambiano, il bitmap viene ricalcolato.
    val previewPhotoPath = initialProfile?.photoPath
    val previewBitmap by produceState<ImageBitmap?>(null, selectedPhotoUri, previewPhotoPath, selectedEmoji, photoCleared) {
        value = when {
            selectedPhotoUri != null -> withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(selectedPhotoUri!!)?.use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                } catch (_: Exception) { null }
            }
            // Carica la foto esistente SOLO se l'utente non ha scelto emoji né ha rimosso la foto
            previewPhotoPath != null && selectedEmoji.isEmpty() && !photoCleared -> withContext(Dispatchers.IO) {
                try { BitmapFactory.decodeFile(previewPhotoPath)?.asImageBitmap() } catch (_: Exception) { null }
            }
            else -> null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = stringResource(titleRes), style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Photo picker row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Avatar preview
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                pickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            previewBitmap != null -> {
                                Image(
                                    bitmap = previewBitmap!!,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            selectedEmoji.isNotEmpty() -> {
                                AppText(text = selectedEmoji, style = MaterialTheme.typography.titleLarge)
                            }
                            else -> {
                                AppIcon(
                                    imageVector = Icons.Filled.AddAPhoto,
                                    contentDescription = stringResource(R.string.profiles_change_photo),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                    }
                    Column {
                        AppText(
                            text = stringResource(R.string.profiles_change_photo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                pickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                            },
                        )
                        if (previewBitmap != null && selectedEmoji.isEmpty()) {
                            AppText(
                                text = "✓",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { AppText(text = stringResource(R.string.profiles_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (name.isNotBlank()) onConfirm(
                                name.trim(), selectedColor, selectedEmoji, selectedPhotoUri,
                                selectedPhotoUri == null && (selectedEmoji.isNotEmpty() || photoCleared),
                                shareTransactionsChecked,
                            )
                        },
                    ),
                )

                if (onShareTransactionsToggle != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            AppText(
                                text = stringResource(R.string.profiles_share_transactions_title),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            AppText(
                                text = stringResource(shareTransactionsDescriptionRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = shareTransactionsChecked,
                            onCheckedChange = { checked ->
                                shareTransactionsChecked = checked
                                onShareTransactionsToggle(checked)
                            },
                        )
                    }
                }

                Column {
                    AppText(
                        text = stringResource(R.string.profiles_color_label),
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
                        text = stringResource(R.string.profiles_emoji_label),
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
                                    .clickable {
                                        if (!isSelected) {
                                            // selezione emoji: sovrascrive foto
                                            selectedEmoji = emoji
                                            selectedPhotoUri = null
                                            photoCleared = true
                                        } else {
                                            // deselezione emoji: photoCleared resta true,
                                            // la foto NON viene ripristinata automaticamente
                                            selectedEmoji = ""
                                        }
                                    },
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
                onClick = {
                    if (name.isNotBlank()) onConfirm(
                        name.trim(), selectedColor, selectedEmoji, selectedPhotoUri,
                        selectedPhotoUri == null && (selectedEmoji.isNotEmpty() || photoCleared),
                        shareTransactionsChecked,
                    )
                },
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
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            AppIcon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ─── Previews ───────────────────────────────────────────────────────────────

private val previewProfileActive = Profile(
    id = 1L, name = "Alice", uuid = "preview-1",
    isActive = true, colorArgb = 0xFF6650A4, avatarEmoji = "😊",
)

private val previewProfileInactive = Profile(
    id = 2L, name = "Bob", uuid = "preview-2",
    isActive = false, colorArgb = 0xFF4CAF50, avatarEmoji = "🧑",
    shareTransactions = true,
)

@Preview(showBackground = true, name = "ActiveProfileHeader - Light")
@Composable
private fun ActiveProfileHeaderLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ActiveProfileHeader(profile = previewProfileActive, onEdit = {})
    }
}

@Preview(showBackground = true, name = "ActiveProfileHeader - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActiveProfileHeaderDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ActiveProfileHeader(profile = previewProfileActive, onEdit = {})
    }
}

@Preview(showBackground = true, name = "ActiveProfileHeader - 2x", fontScale = 2.0f)
@Composable
private fun ActiveProfileHeaderLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ActiveProfileHeader(profile = previewProfileActive, onEdit = {})
    }
}

@Preview(showBackground = true, name = "ProfileItem Active - Light")
@Composable
private fun ProfileItemActiveLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ProfileItem(
            profile = previewProfileActive,
            isActive = true,
            onSwitch = {}, onEdit = {}, onDelete = {},
        )
    }
}

@Preview(showBackground = true, name = "ProfileItem Active - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileItemActiveDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ProfileItem(
            profile = previewProfileActive,
            isActive = true,
            onSwitch = {}, onEdit = {}, onDelete = {},
        )
    }
}

@Preview(showBackground = true, name = "ProfileItem Inactive - Light")
@Composable
private fun ProfileItemInactiveLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ProfileItem(
            profile = previewProfileInactive,
            isActive = false,
            onSwitch = {}, onEdit = {}, onDelete = {},
        )
    }
}

@Preview(showBackground = true, name = "ProfileFormDialog Create - Light")
@Composable
private fun ProfileFormDialogCreateLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ProfileFormDialog(
            initialProfile = null,
            titleRes = R.string.profiles_create_dialog_title,
            onConfirm = { _, _, _, _, _, _ -> },
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "ProfileFormDialog Create - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileFormDialogCreateDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ProfileFormDialog(
            initialProfile = null,
            titleRes = R.string.profiles_create_dialog_title,
            onConfirm = { _, _, _, _, _, _ -> },
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "ProfileFormDialog Edit + Share - Light")
@Composable
private fun ProfileFormDialogEditShareLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ProfileFormDialog(
            initialProfile = previewProfileInactive,
            titleRes = R.string.profiles_edit_dialog_title,
            onConfirm = { _, _, _, _, _, _ -> },
            onDismiss = {},
            onShareTransactionsToggle = {},
        )
    }
}

@Preview(
    showBackground = true,
    name = "ProfileFormDialog Edit + Share - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProfileFormDialogEditShareDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ProfileFormDialog(
            initialProfile = previewProfileInactive,
            titleRes = R.string.profiles_edit_dialog_title,
            onConfirm = { _, _, _, _, _, _ -> },
            onDismiss = {},
            onShareTransactionsToggle = {},
        )
    }
}

@Preview(showBackground = true, name = "ColorSwatch - Light")
@Composable
private fun ColorSwatchLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            ColorSwatch(colorValue = 0xFF6650A4L, isSelected = true, onClick = {})
            ColorSwatch(colorValue = 0xFF4CAF50L, isSelected = false, onClick = {})
            ColorSwatch(colorValue = 0xFFEF5350L, isSelected = false, onClick = {})
        }
    }
}
