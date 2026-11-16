package com.antcashmanager.data.manager

import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

public class ProfileManagerImpl(
    private val profileRepository: ProfileRepository,
) : ProfileManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _activeProfileId = MutableStateFlow(DEFAULT_PROFILE_ID)
    override val activeProfileId: StateFlow<Long> = _activeProfileId.asStateFlow()

    /**
     * When the active profile has shareTransactions = true, returns all profile IDs with
     * shareTransactions = true. Otherwise returns only the active profile ID.
     */
    override val sharedProfileIds: StateFlow<List<Long>> =
        combine(activeProfileId, profileRepository.getAllProfiles()) { activeId, profiles ->
            val active = profiles.find { it.id == activeId }
            if (active?.shareTransactions == true) {
                profiles.filter { it.shareTransactions }.map { it.id }
            } else {
                listOf(activeId)
            }
        }.stateIn(scope, SharingStarted.Eagerly, listOf(DEFAULT_PROFILE_ID))

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
