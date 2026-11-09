package com.antcashmanager.data.manager

import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class ProfileManagerImpl(
    private val profileRepository: ProfileRepository,
) : ProfileManager {

    private val _activeProfileId = MutableStateFlow(DEFAULT_PROFILE_ID)
    override val activeProfileId: StateFlow<Long> = _activeProfileId.asStateFlow()

    override suspend fun initialize() {
        val active = profileRepository.getActiveProfile()
        _activeProfileId.value = active?.id ?: DEFAULT_PROFILE_ID
    }

    override suspend fun switchProfile(id: Long) {
        profileRepository.setActiveProfile(id)
        _activeProfileId.value = id
    }

    override fun getActiveProfileIdSync(): Long = _activeProfileId.value

    private companion object {
        const val DEFAULT_PROFILE_ID = 1L
    }
}
