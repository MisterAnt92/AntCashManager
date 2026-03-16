package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.BaseUseCase

class SetThemeUseCase(
    private val settingsRepository: SettingsRepository
) : BaseUseCase<AppTheme, Unit>() {

    override suspend fun invoke(params: AppTheme) =
        settingsRepository.setTheme(params)
}
