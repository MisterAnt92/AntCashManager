package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.flow.Flow

class GetTransactionDisplayTypeUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsObservableUseCase<TransactionDisplayType>() {

    override fun execute(params: Unit): Flow<TransactionDisplayType> = settingsRepository.getTransactionDisplayType()
}
