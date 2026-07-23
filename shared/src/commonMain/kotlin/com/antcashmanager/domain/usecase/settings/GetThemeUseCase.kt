package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class GetThemeUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<AppTheme>(dispatcher) {

    override fun execute(params: Unit): Flow<AppTheme> = settingsRepository.getTheme()
}
