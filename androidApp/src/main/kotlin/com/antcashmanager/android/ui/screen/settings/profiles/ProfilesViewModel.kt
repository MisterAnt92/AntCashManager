package com.antcashmanager.android.ui.screen.settings.profiles

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.android.util.ProfilePhotoStorage
import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.usecase.profile.CreateProfileParams
import com.antcashmanager.domain.usecase.profile.CreateProfileUseCase
import com.antcashmanager.domain.usecase.profile.DeleteProfileUseCase
import com.antcashmanager.domain.usecase.profile.GetProfileByIdUseCase
import com.antcashmanager.domain.usecase.profile.GetProfilesUseCase
import com.antcashmanager.domain.usecase.profile.SwitchProfileUseCase
import com.antcashmanager.domain.usecase.profile.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfilesViewModel(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val switchProfileUseCase: SwitchProfileUseCase,
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val profileManager: ProfileManager,
    private val profilePhotoStorage: ProfilePhotoStorage,
) : BaseViewModel<ProfilesEvent>() {

    private val _state = MutableStateFlow(ProfilesState())
    val state: StateFlow<ProfilesState> = _state

    init {
        combine(
            getProfilesUseCase(),
            profileManager.activeProfileId,
        ) { profilesResult, activeId ->
            profilesResult.getOrNull()?.let { profiles ->
                _state.update { current ->
                    current.copy(
                        profiles = profiles,
                        activeProfileId = activeId,
                        isLoading = false,
                        showEditDialog = current.showEditDialog?.let { editing ->
                            profiles.find { it.id == editing.id } ?: editing
                        },
                    )
                }
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
            is ProfilesEvent.CreateProfile -> createProfile(
                event.name,
                event.colorArgb,
                event.avatarEmoji,
                event.photoUri,
                event.shareTransactions,
            )

            is ProfilesEvent.UpdateProfile -> updateProfile(event.profile, event.newPhotoUri, event.clearPhoto)
            is ProfilesEvent.ShowEditDialog -> _state.update { it.copy(showEditDialog = event.profile) }
            is ProfilesEvent.DismissEditDialog -> _state.update { it.copy(showEditDialog = null) }
            is ProfilesEvent.ToggleShareTransactions -> toggleShareTransactions(event.profile, event.enabled)
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

    private fun createProfile(name: String, colorArgb: Long, avatarEmoji: String, photoUri: Uri?, shareTransactions: Boolean = true) {
        _state.update { it.copy(showCreateDialog = false, isLoading = true) }
        viewModelScope.launch {
            if (shareTransactions) {
                val activeProfile = _state.value.profiles.find { it.id == _state.value.activeProfileId }
                if (activeProfile != null && !activeProfile.shareTransactions) {
                    updateProfileUseCase(activeProfile.copy(shareTransactions = true)).handleError { err ->
                        logWarn("Failed to enable shareTransactions on active profile: ${err.message}")
                    }
                }
            }
            val newId = createProfileUseCase(CreateProfileParams(name, colorArgb, avatarEmoji, shareTransactions = shareTransactions)).handleError { err ->
                _state.update { it.copy(isLoading = false, errorMessage = err.message) }
            } ?: run {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            if (photoUri != null) {
                val path = try {
                    profilePhotoStorage.savePhoto(newId, photoUri)
                } catch (e: Exception) {
                    logWarn("Failed to save profile photo: ${e.message}")
                    null
                }
                if (path != null) {
                    // Legge il profilo dal DB (già committato da createProfileUseCase) per evitare
                    // la race condition con lo StateFlow che potrebbe non aver emesso il nuovo profilo ancora.
                    val newProfile = getProfileByIdUseCase(newId).getOrNull()
                    if (newProfile != null) {
                        updateProfileUseCase(newProfile.copy(photoPath = path)).handleError { err ->
                            logWarn("Failed to persist photo path: ${err.message}")
                        }
                    } else {
                        logWarn("Profile $newId not found after creation, photo path lost")
                    }
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun toggleShareTransactions(profile: Profile, enabled: Boolean) {
        _state.update { current ->
            current.copy(
                showEditDialog = current.showEditDialog
                    ?.takeIf { it.id == profile.id }
                    ?.copy(shareTransactions = enabled)
                    ?: current.showEditDialog,
            )
        }
        viewModelScope.launch {
            updateProfileUseCase(profile.copy(shareTransactions = enabled)).handleError { err ->
                _state.update { current ->
                    current.copy(
                        showEditDialog = current.showEditDialog
                            ?.takeIf { it.id == profile.id }
                            ?.copy(shareTransactions = !enabled)
                            ?: current.showEditDialog,
                        errorMessage = err.message,
                    )
                }
            }
        }
    }

    private fun updateProfile(profile: Profile, newPhotoUri: Uri?, clearPhoto: Boolean) {
        viewModelScope.launch {
            val photoPath = when {
                newPhotoUri != null -> try {
                    profilePhotoStorage.savePhoto(profile.id, newPhotoUri)
                } catch (e: Exception) {
                    logWarn("Failed to save profile photo: ${e.message}")
                    profile.photoPath
                }
                // User switched to emoji: rimuovi la foto salvata
                clearPhoto -> {
                    if (profile.photoPath != null) profilePhotoStorage.deletePhoto(profile.id)
                    null
                }
                else -> profile.photoPath
            }
            updateProfileUseCase(profile.copy(photoPath = photoPath)).handleError { err ->
                _state.update { it.copy(errorMessage = err.message) }
            }
        }
    }
}
