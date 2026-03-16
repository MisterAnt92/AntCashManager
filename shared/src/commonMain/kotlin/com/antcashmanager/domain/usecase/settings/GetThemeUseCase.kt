package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetThemeUseCase(
    private val settingsRepository: SettingsRepository
) : NoParamsFlowUseCase<AppTheme>() {

    override fun invoke(): Flow<AppTheme> =
        settingsRepository.getTheme()
}
