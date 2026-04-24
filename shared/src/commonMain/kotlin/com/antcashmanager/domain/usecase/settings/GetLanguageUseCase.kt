package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetLanguageUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsFlowUseCase<Result<AppLanguage>>() {

    override fun execute(): Flow<Result<AppLanguage>> =
        settingsRepository.getLanguage()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
