package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Generic UseCase for retrieving settings/preferences of type [T].
 * Eliminates boilerplate for 30+ identical get-preference use cases.
 *
 * **Usage Example**:
 * ```kotlin
 * // Instead of creating GetLanguageUseCase, GetThemeUseCase, etc.
 * val getLanguageUseCase = GetSettingUseCase(
 *     getter = settingsRepository::getLanguage
 * )
 *
 * val getThemeUseCase = GetSettingUseCase(
 *     getter = settingsRepository::getTheme
 * )
 * ```
 *
 * **Benefits**:
 * - Removes 33 boilerplate files (1000+ lines)
 * - Single source of truth for getting preferences
 * - Simplifies DI module (1 registration per setting type instead of duplicated factories)
 * - Makes adding new preferences trivial
 *
 * @param T The type of setting to retrieve
 * @param getter A lambda that retrieves the setting from repository as a Flow<T>
 * @param dispatcher The coroutine dispatcher (default: Default for performance)
 */
class GetSettingUseCase<T>(
    private val getter: () -> Flow<T>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<T>(dispatcher) {

    override fun execute(params: Unit): Flow<T> = getter()
}
