package com.antcashmanager.domain.usecase.profile

import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.ProfileRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Creates a new profile and seeds it with the Default profile's default categories.
 *
 * Returns the new profile's id.
 */
public class CreateProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<CreateProfileParams, Long>(dispatcher) {

    override suspend fun execute(params: CreateProfileParams): Long {
        val uuid = java.util.UUID.randomUUID().toString()
        val newId = profileRepository.insertProfile(
            Profile(
                name = params.name,
                uuid = uuid,
                colorArgb = params.colorArgb,
                avatarEmoji = params.avatarEmoji,
                shareTransactions = params.shareTransactions,
            ),
        )
        categoryRepository.seedDefaultCategoriesForProfile(
            sourceProfileId = DEFAULT_PROFILE_ID,
            targetProfileId = newId,
        )
        return newId
    }

    private companion object {
        const val DEFAULT_PROFILE_ID = 1L
    }
}
