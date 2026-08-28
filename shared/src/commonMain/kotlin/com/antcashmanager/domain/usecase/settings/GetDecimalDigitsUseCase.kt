package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.flow.Flow

public class GetDecimalDigitsUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsObservableUseCase<Int>() {

    override fun execute(params: Unit): Flow<Int> = settingsRepository.getDecimalDigits()
}
