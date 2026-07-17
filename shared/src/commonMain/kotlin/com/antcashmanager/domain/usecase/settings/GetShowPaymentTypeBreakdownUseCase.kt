package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsResultFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetShowPaymentTypeBreakdownUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsResultFlowUseCase<Boolean>() {

    override fun execute(): Flow<Boolean> = settingsRepository.getShowPaymentTypeBreakdown()
}
