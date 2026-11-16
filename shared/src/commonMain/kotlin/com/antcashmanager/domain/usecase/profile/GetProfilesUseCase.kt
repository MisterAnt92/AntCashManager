package com.antcashmanager.domain.usecase.profile

import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.repository.ProfileRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/** Returns all profiles ordered by creation date as a reactive Flow. */
public class GetProfilesUseCase(
    private val profileRepository: ProfileRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<List<Profile>>(dispatcher) {
    override fun execute(params: Unit): Flow<List<Profile>> = profileRepository.getAllProfiles()
}
