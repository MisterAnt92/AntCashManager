package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SetLanguageUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<AppLanguage, Unit>(dispatcher) {

    override suspend fun execute(params: AppLanguage): Unit =
        settingsRepository.setLanguage(params)
}
