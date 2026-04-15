package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.BaseUseCase

class SetShowPaymentTypeBreakdownUseCase(
    private val settingsRepository: SettingsRepository,
) : BaseUseCase<Boolean, Unit>() {

    override suspend fun invoke(params: Boolean) =
        settingsRepository.setShowPaymentTypeBreakdown(params)
}

