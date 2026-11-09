package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.Profile
import kotlinx.coroutines.flow.Flow

public interface ProfileRepository {
    public fun getAllProfiles(): Flow<List<Profile>>
    public suspend fun getProfileById(id: Long): Profile?
    public suspend fun getProfileByUuid(uuid: String): Profile?
    public suspend fun getActiveProfile(): Profile?
    public suspend fun insertProfile(profile: Profile): Long
    public suspend fun updateProfile(profile: Profile)
    public suspend fun deleteProfile(profile: Profile)
    public suspend fun setActiveProfile(profileId: Long)
    public suspend fun getProfileCount(): Int
}
