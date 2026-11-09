package com.antcashmanager.android.ui.screen.settings.profiles

import com.antcashmanager.domain.model.Profile

data class ProfilesState(
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: Long = 1L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteConfirmation: Profile? = null,
    val showSwitchConfirmation: Profile? = null,
    val showCreateDialog: Boolean = false,
)
