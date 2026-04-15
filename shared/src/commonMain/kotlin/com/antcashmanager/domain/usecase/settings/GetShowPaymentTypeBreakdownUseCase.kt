package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetShowPaymentTypeBreakdownUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsFlowUseCase<Boolean>() {

    override fun invoke(): Flow<Boolean> =
        settingsRepository.getShowPaymentTypeBreakdown()
}

