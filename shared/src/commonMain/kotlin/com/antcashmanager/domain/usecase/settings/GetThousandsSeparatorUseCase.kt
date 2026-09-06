package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.flow.Flow

@Deprecated(
    message = "Use GetSettingUseCase<T> or SetSettingUseCase<T> instead. This class is pure boilerplate and will be removed in v1.8.",
    replaceWith = ReplaceWith("GetSettingUseCase<T>() or SetSettingUseCase<T>()")
)
public class GetThousandsSeparatorUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsObservableUseCase<String>() {

    override fun execute(params: Unit): Flow<String> = settingsRepository.getThousandsSeparator()
}
