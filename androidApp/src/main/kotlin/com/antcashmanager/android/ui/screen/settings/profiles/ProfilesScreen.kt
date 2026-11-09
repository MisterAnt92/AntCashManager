package com.antcashmanager.android.ui.screen.settings.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
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
import com.antcashmanager.domain.model.Profile
import org.koin.androidx.compose.koinViewModel

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
                title = { Text(stringResource(R.string.profiles_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(ProfilesEvent.ShowCreateDialog) }) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                ) {
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

    // Switch confirmation dialog
    state.showSwitchConfirmation?.let { target ->
        AlertDialog(
            onDismissRequest = { onEvent(ProfilesEvent.DismissSwitch) },
            title = { Text(stringResource(R.string.profiles_switch_dialog_title)) },
            text = { Text(stringResource(R.string.profiles_switch_dialog_message, target.name)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProfilesEvent.ConfirmSwitch) }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfilesEvent.DismissSwitch) }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }

    // Delete confirmation dialog
    state.showDeleteConfirmation?.let { target ->
        AlertDialog(
            onDismissRequest = { onEvent(ProfilesEvent.DismissDelete) },
            title = { Text(stringResource(R.string.profiles_delete_dialog_title)) },
            text = { Text(stringResource(R.string.profiles_delete_dialog_warning, target.name)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProfilesEvent.ConfirmDelete) }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfilesEvent.DismissDelete) }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }

    // Create dialog
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
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface,
        ),
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
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                )
                if (isActive) {
                    Text(
                        text = stringResource(R.string.profiles_active_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (isActive) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            // Cannot delete the Default profile (id=1)
            if (profile.id != 1L) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.profiles_delete_dialog_title),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    profile: Profile,
    modifier: Modifier = Modifier,
) {
    val bgColor = Color(profile.colorArgb)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (profile.avatarEmoji.isNotEmpty()) {
            Text(
                text = profile.avatarEmoji,
                style = MaterialTheme.typography.titleMedium,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun CreateProfileDialog(
    onConfirm: (name: String, colorArgb: Long, avatarEmoji: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val defaultColor = 0xFF6650A4L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profiles_create_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.profiles_name_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank()) onConfirm(name.trim(), defaultColor, "")
                    }),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), defaultColor, "") },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
