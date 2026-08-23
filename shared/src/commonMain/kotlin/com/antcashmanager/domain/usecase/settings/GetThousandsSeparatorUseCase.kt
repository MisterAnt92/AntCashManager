package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.flow.Flow

public class GetThousandsSeparatorUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsObservableUseCase<String>() {

    override fun execute(params: Unit): Flow<String> = settingsRepository.getThousandsSeparator()
}
