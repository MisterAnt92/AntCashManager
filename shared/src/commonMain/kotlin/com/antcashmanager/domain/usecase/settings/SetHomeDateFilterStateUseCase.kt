package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SetHomeDateFilterStateUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<SavedDateFilter, Result<Unit>>(dispatcher) {

    override suspend fun execute(params: SavedDateFilter): Result<Unit> = runCatching {
        settingsRepository.setHomeDateFilterState(params)
    }
}

