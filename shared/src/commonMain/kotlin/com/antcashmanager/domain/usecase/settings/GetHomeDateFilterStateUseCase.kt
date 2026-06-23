package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetHomeDateFilterStateUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsFlowUseCase<Result<SavedDateFilter>>(dispatcher) {

    override fun execute(): Flow<Result<SavedDateFilter>> =
        settingsRepository.getHomeDateFilterState()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}

