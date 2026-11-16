package com.antcashmanager.domain.usecase.profile

import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.repository.ProfileRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Returns the profile with the given [id], or null if not found. */
public class GetProfileByIdUseCase(
    private val profileRepository: ProfileRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Long, Profile?>(dispatcher) {
    override suspend fun execute(params: Long): Profile? = profileRepository.getProfileById(params)
}
