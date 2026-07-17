package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsResultFlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class GetThemeUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsResultFlowUseCase<AppTheme>(dispatcher) {

    override fun execute(): Flow<AppTheme> = settingsRepository.getTheme()
}
