package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.flow.Flow

public class GetHighContrastUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsObservableUseCase<Boolean>() {

    override fun execute(params: Unit): Flow<Boolean> = settingsRepository.getHighContrast()
}
