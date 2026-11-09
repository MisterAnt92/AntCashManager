package com.antcashmanager.domain.usecase.profile

import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.repository.ProfileRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Updates name, color and emoji of an existing profile. */
public class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Profile, Unit>(dispatcher) {
    override suspend fun execute(params: Profile) {
        profileRepository.updateProfile(params)
    }
}
