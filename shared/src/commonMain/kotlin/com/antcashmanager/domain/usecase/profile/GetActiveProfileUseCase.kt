package com.antcashmanager.domain.usecase.profile

import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.repository.ProfileRepository
import com.antcashmanager.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Returns the currently active profile, or null if none is set. */
public class GetActiveProfileUseCase(
    private val profileRepository: ProfileRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Profile?>(dispatcher) {
    override suspend fun execute(params: Unit): Profile? = profileRepository.getActiveProfile()
}
