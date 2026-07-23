package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsResultFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetThousandsSeparatorUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsResultFlowUseCase<String>() {

    override fun execute(): Flow<String> = settingsRepository.getThousandsSeparator()
}
