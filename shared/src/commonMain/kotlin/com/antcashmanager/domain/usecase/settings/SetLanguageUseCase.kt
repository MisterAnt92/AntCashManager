package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.BaseUseCase

class SetLanguageUseCase(
    private val settingsRepository: SettingsRepository,
) : BaseUseCase<AppLanguage, Unit>() {

    override suspend fun invoke(params: AppLanguage) =
        settingsRepository.setLanguage(params)
}

