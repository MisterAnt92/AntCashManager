package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per l'impostazione del tema dell'app.
 */
class SetThemeUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<AppTheme, Unit>(dispatcher) {

    override suspend fun execute(params: AppTheme): Unit =
        settingsRepository.setTheme(params)
}
