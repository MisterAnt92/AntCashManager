package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetLanguageUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsFlowUseCase<AppLanguage>() {

    override fun invoke(): Flow<AppLanguage> =
        settingsRepository.getLanguage()
}

