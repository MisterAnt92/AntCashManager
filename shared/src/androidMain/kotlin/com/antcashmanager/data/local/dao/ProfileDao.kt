package com.antcashmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.antcashmanager.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY created_at ASC")
    public fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    public suspend fun getProfileById(id: Long): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE uuid = :uuid LIMIT 1")
    public suspend fun getProfileByUuid(uuid: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE is_active = 1 LIMIT 1")
    public suspend fun getActiveProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    public suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    public suspend fun deleteProfile(profile: ProfileEntity)

    @Query("SELECT COUNT(*) FROM profiles")
    public suspend fun getProfileCount(): Int

    @Transaction
    public suspend fun setActiveProfile(profileId: Long) {
        clearActiveProfile()
        activateProfile(profileId)
    }

    @Query("UPDATE profiles SET is_active = 0")
    public suspend fun clearActiveProfile()

    @Query("UPDATE profiles SET is_active = 1 WHERE id = :profileId")
    public suspend fun activateProfile(profileId: Long)
}
