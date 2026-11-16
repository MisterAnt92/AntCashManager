package com.antcashmanager.domain.usecase.profile

import com.antcashmanager.domain.exception.ProfileException
import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.model.Profile
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.ProfileRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Deletes a profile along with all its transactions and categories.
 *
 * Guards: id=1 (Default) and the currently active profile cannot be deleted.
 */
public class DeleteProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val profileManager: ProfileManager,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Profile, Unit>(dispatcher) {

    override suspend fun execute(params: Profile) {
        if (params.id == DEFAULT_PROFILE_ID) {
            throw ProfileException.CannotDeleteDefault
        }
        if (params.id == profileManager.getActiveProfileIdSync()) {
            throw ProfileException.CannotDeleteActive
        }
        transactionRepository.deleteAllTransactionsForProfile(params.id)
        categoryRepository.deleteAllCategoriesForProfile(params.id)
        profileRepository.deleteProfile(params)
    }

    private companion object {
        const val DEFAULT_PROFILE_ID = 1L
    }
}
