package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public class SetTutorialCompletedUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Boolean, Unit>(dispatcher) {

    override suspend fun execute(params: Boolean): Unit =
        settingsRepository.setIsTutorialCompleted(params)
}
