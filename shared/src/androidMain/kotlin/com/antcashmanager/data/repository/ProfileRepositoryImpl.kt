package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.ProfileDao
import com.antcashmanager.data.local.entity.ProfileEntity
import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class ProfileRepositoryImpl(
    private val profileDao: ProfileDao,
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<Profile>> =
        profileDao.getAllProfiles().map { list -> list.map { it.toDomain() } }

    override suspend fun getProfileById(id: Long): Profile? =
        profileDao.getProfileById(id)?.toDomain()

    override suspend fun getProfileByUuid(uuid: String): Profile? =
        profileDao.getProfileByUuid(uuid)?.toDomain()

    override suspend fun getActiveProfile(): Profile? =
        profileDao.getActiveProfile()?.toDomain()

    override suspend fun insertProfile(profile: Profile): Long =
        profileDao.insertProfile(profile.toEntity())

    override suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile.toEntity())
    }

    override suspend fun deleteProfile(profile: Profile) {
        profileDao.deleteProfile(profile.toEntity())
    }

    override suspend fun setActiveProfile(profileId: Long) {
        profileDao.setActiveProfile(profileId)
    }

    override suspend fun getProfileCount(): Int = profileDao.getProfileCount()

    private fun ProfileEntity.toDomain() = Profile(
        id = id,
        name = name,
        uuid = uuid,
        createdAt = createdAt,
        isActive = isActive,
        colorArgb = colorArgb,
        avatarEmoji = avatarEmoji,
    )

    private fun Profile.toEntity() = ProfileEntity(
        id = id,
        name = name,
        uuid = uuid,
        createdAt = createdAt,
        isActive = isActive,
        colorArgb = colorArgb,
        avatarEmoji = avatarEmoji,
    )
}
