package com.antcashmanager.domain.manager

import kotlinx.coroutines.flow.StateFlow

public interface ProfileManager {
    public val activeProfileId: StateFlow<Long>
    public suspend fun initialize()
    public suspend fun switchProfile(id: Long)

    /** Non-suspend fast path — returns the in-memory cached active profile id. */
    public fun getActiveProfileIdSync(): Long
}
