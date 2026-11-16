package com.antcashmanager.domain.manager

import kotlinx.coroutines.flow.StateFlow

public interface ProfileManager {
    public val activeProfileId: StateFlow<Long>

    /**
     * IDs of profiles whose transactions are visible to the active profile.
     * Contains only [activeProfileId] when sharing is off; contains all sharing-enabled
     * profile IDs when the active profile has shareTransactions = true.
     */
    public val sharedProfileIds: StateFlow<List<Long>>

    public suspend fun initialize()
    public suspend fun switchProfile(id: Long)

    /** Non-suspend fast path — returns the in-memory cached active profile id. */
    public fun getActiveProfileIdSync(): Long
}
