package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf

/**
 * Generic UseCase for updating settings/preferences of type [T].
 * Eliminates boilerplate for 30+ identical set-preference use cases.
 *
 * **Usage Example**:
 * ```kotlin
 * // Instead of creating SetLanguageUseCase, SetThemeUseCase, etc.
 * val setLanguageUseCase = SetSettingUseCase(
 *     setter = settingsRepository::setLanguage
 * )
 *
 * val setThemeUseCase = SetSettingUseCase(
 *     setter = settingsRepository::setTheme
 * )
 * ```
 *
 * **Benefits**:
 * - Removes 33 boilerplate files (1000+ lines)
 * - Single source of truth for setting preferences
 * - Simplifies DI module (1 registration per setting type instead of duplicated factories)
 * - Makes adding new preferences trivial
 *
 * @param T The type of setting to update
 * @param setter A lambda that updates the setting in repository (suspend function)
 * @param dispatcher The coroutine dispatcher (default: Default for performance)
 */
class SetSettingUseCase<T>(
    private val setter: suspend (T) -> Unit,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<T, Unit>(dispatcher) {

    override suspend fun execute(params: T) {
        setter(params)
    }
}
