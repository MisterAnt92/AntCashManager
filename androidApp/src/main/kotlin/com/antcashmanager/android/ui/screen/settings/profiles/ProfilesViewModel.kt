package com.antcashmanager.android.ui.screen.settings.profiles

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.usecase.profile.CreateProfileParams
import com.antcashmanager.domain.usecase.profile.CreateProfileUseCase
import com.antcashmanager.domain.usecase.profile.DeleteProfileUseCase
import com.antcashmanager.domain.usecase.profile.GetProfilesUseCase
import com.antcashmanager.domain.usecase.profile.SwitchProfileUseCase
import com.antcashmanager.domain.usecase.profile.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfilesViewModel(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val switchProfileUseCase: SwitchProfileUseCase,
    private val profileManager: ProfileManager,
) : BaseViewModel<ProfilesEvent>() {

    private val _state = MutableStateFlow(ProfilesState())
    val state: StateFlow<ProfilesState> = _state

    init {
        combine(
            getProfilesUseCase(),
            profileManager.activeProfileId,
        ) { profilesResult, activeId ->
            profilesResult.getOrNull()?.let { profiles ->
                _state.update { it.copy(profiles = profiles, activeProfileId = activeId, isLoading = false) }
            } ?: _state.update { it.copy(isLoading = false) }
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: ProfilesEvent) {
        when (event) {
            is ProfilesEvent.SwitchProfile -> requestSwitch(event.profile)
            is ProfilesEvent.ConfirmSwitch -> confirmSwitch()
            is ProfilesEvent.DismissSwitch -> dismissSwitch()
            is ProfilesEvent.DeleteProfile -> requestDelete(event.profile)
            is ProfilesEvent.ConfirmDelete -> confirmDelete()
            is ProfilesEvent.DismissDelete -> dismissDelete()
            is ProfilesEvent.ShowCreateDialog -> _state.update { it.copy(showCreateDialog = true) }
            is ProfilesEvent.DismissCreateDialog -> _state.update { it.copy(showCreateDialog = false) }
            is ProfilesEvent.CreateProfile -> createProfile(event.name, event.colorArgb, event.avatarEmoji)
            is ProfilesEvent.UpdateProfile -> updateProfile(event.profile)
            is ProfilesEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun requestSwitch(profile: Profile) {
        if (profile.id == _state.value.activeProfileId) return
        _state.update { it.copy(showSwitchConfirmation = profile) }
    }

    private fun confirmSwitch() {
        val target = _state.value.showSwitchConfirmation ?: return
        _state.update { it.copy(showSwitchConfirmation = null, isLoading = true) }
        viewModelScope.launch {
            switchProfileUseCase(target.id).handleError { err ->
                _state.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun dismissSwitch() {
        _state.update { it.copy(showSwitchConfirmation = null) }
    }

    private fun requestDelete(profile: Profile) {
        _state.update { it.copy(showDeleteConfirmation = profile) }
    }

    private fun confirmDelete() {
        val target = _state.value.showDeleteConfirmation ?: return
        _state.update { it.copy(showDeleteConfirmation = null, isLoading = true) }
        viewModelScope.launch {
            deleteProfileUseCase(target).handleError { err ->
                _state.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun dismissDelete() {
        _state.update { it.copy(showDeleteConfirmation = null) }
    }

    private fun createProfile(name: String, colorArgb: Long, avatarEmoji: String) {
        _state.update { it.copy(showCreateDialog = false, isLoading = true) }
        viewModelScope.launch {
            createProfileUseCase(CreateProfileParams(name, colorArgb, avatarEmoji)).handleError { err ->
                _state.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            updateProfileUseCase(profile).handleError { err ->
                _state.update { it.copy(errorMessage = err.message) }
            }
        }
    }
}
