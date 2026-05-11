package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetChartsDateFilterStateUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsFlowUseCase<Result<SavedDateFilter>>() {

    override fun execute(): Flow<Result<SavedDateFilter>> =
        settingsRepository.getChartsDateFilterState()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}

