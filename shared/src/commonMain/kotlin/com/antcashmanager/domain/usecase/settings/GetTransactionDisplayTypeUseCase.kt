package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsResultFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetTransactionDisplayTypeUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsResultFlowUseCase<TransactionDisplayType>() {

    override fun execute(): Flow<TransactionDisplayType> = settingsRepository.getTransactionDisplayType()
}
