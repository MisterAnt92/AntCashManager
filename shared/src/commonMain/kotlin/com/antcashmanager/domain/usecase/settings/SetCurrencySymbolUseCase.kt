package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SetCurrencySymbolUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<String, Unit>(dispatcher) {

    override suspend fun execute(params: String): Unit =
        settingsRepository.setCurrencySymbol(params)
}
