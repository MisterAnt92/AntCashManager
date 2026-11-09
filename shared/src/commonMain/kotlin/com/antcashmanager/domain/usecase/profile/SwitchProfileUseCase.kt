package com.antcashmanager.domain.usecase.profile

import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Switches the active profile to the given profileId. */
public class SwitchProfileUseCase(
    private val profileManager: ProfileManager,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Long, Unit>(dispatcher) {
    override suspend fun execute(params: Long) {
        profileManager.switchProfile(params)
    }
}
