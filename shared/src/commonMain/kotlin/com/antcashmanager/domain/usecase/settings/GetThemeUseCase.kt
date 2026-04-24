package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetThemeUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsFlowUseCase<Result<AppTheme>>() {

    override fun execute(): Flow<Result<AppTheme>> =
        settingsRepository.getTheme()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
