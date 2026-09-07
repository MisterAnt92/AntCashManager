package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@Deprecated(
    message = "Use GetSettingUseCase<T> or SetSettingUseCase<T> instead. This class is pure boilerplate and will be removed in v1.8.",
    replaceWith = ReplaceWith("GetSettingUseCase<T>() or SetSettingUseCase<T>()"),
)
public class GetTransactionsDateFilterStateUseCase(
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<SavedDateFilter>(dispatcher) {
    override fun execute(params: Unit): Flow<SavedDateFilter> = settingsRepository.getTransactionsDateFilterState()
}
