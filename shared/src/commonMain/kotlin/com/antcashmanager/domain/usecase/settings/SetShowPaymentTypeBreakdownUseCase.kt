package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SetShowPaymentTypeBreakdownUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<Boolean, Result<Unit>>(dispatcher) {

    override suspend fun execute(params: Boolean): Result<Unit> = runCatching {
        settingsRepository.setShowPaymentTypeBreakdown(params)
    }
}
