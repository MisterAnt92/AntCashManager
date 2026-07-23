package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsResultFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetDecimalDigitsUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsResultFlowUseCase<Int>() {

    override fun execute(): Flow<Int> = settingsRepository.getDecimalDigits()
}
