package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public class SetTransactionDisplayTypeUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<TransactionDisplayType, Unit>(dispatcher) {

    override suspend fun execute(params: TransactionDisplayType): Unit =
        settingsRepository.setTransactionDisplayType(params)
}
