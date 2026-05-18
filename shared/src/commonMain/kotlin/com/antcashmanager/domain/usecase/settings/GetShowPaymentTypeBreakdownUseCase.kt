package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetShowPaymentTypeBreakdownUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsFlowUseCase<Result<Boolean>>() {

    override fun execute(): Flow<Result<Boolean>> =
        settingsRepository.getShowPaymentTypeBreakdown()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
