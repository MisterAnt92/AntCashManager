package com.antcashmanager.android.ui.base

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.IOException

/**
 * Unified error state model for all ViewModels.
 * Centralizes error handling across the app.
 *
 * @param isError true if an error occurred
 * @param message human-readable error message
 * @param throwable the underlying exception (for logging only, never exposed to UI)
 * @param retryable true if the operation can be retried (e.g., network errors)
 */
data class ErrorState(
    val isError: Boolean = false,
    val message: String? = null,
    val throwable: Throwable? = null,
    val retryable: Boolean = false,
)

/**
 * Base class per tutti i ViewModel dell'app con supporto generico per eventi e dispatcher.
 *
 * Fornisce:
 * - Logging centralizzato tramite helper ([logDebug], [logInfo], [logWarn], [logError])
 * - Tag automatico derivato da [this::class.simpleName]
 * - Supporto per gli eventi tramite [onEvent] (override necessario nei ViewModel con eventi)
 * - Dispatcher configurabile (default [Dispatchers.Default]) per l'esecuzione di operazioni async
 * - Centralizzato error handling via [ErrorState] e [handleError] extension
 *
 * I ViewModel senza eventi estendono [BaseViewModel]<[Nothing]>; quelli con eventi specificano
 * il tipo dell'evento (e.g., [BaseViewModel]<HomeEvent>).
 *
 * **UDF Pattern**:
 * ```
 * FeatureState { errorState: ErrorState, data: T }
 * FeatureEvent { sealed events }
 * FeatureViewModel: onEvent() processes events → updates state via StateFlow
 * FeatureScreen: reads state.collectAsStateWithLifecycle() + emits onEvent()
 * ```
 *
 * @param E il tipo dell'evento. [Nothing] per i ViewModel senza eventi.
 * @param dispatcher il [CoroutineDispatcher] da usare in operazioni async. Default: [Dispatchers.Default].
 */
abstract class BaseViewModel<E : Any>(
    protected val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    protected val tag: String = this::class.simpleName ?: "ViewModel"

    /**
     * Gestisce gli eventi del ViewModel.
     * Override necessario nei ViewModel che supportano eventi.
     * Implementazione default: logga l'evento.
     *
     * @param event l'evento ricevuto
     */
    open fun onEvent(event: E) {
        logDebug("Event: $event")
    }

    protected fun logDebug(message: String) {
        Logger.d(tag = tag) { message }
    }

    protected fun logInfo(message: String) {
        Logger.i(tag = tag) { message }
    }

    protected fun logWarn(message: String) {
        Logger.w(tag = tag) { message }
    }

    protected fun logError(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Logger.e(throwable = throwable, tag = tag) { message }
        } else {
            Logger.e(tag = tag) { message }
        }
    }

    /**
     * Centralizzato error handling for Result<T>.
     * Automatically detects retryable errors (IOException) and logs appropriately.
     *
     * Usage in ViewModel:
     * ```
     * result.handleError { errorState ->
     *     _state.value = _state.value.copy(errorState = errorState)
     * }
     * ```
     *
     * @param onError callback to handle the error state
     * @return the successful value or null if error occurred
     */
    protected fun <T> Result<T>.handleError(
        onError: (error: ErrorState) -> Unit = { logError(it.message ?: "Unknown error") }
    ): T? {
        return onFailure { error ->
            if (error is CancellationException) throw error
            val errorState = ErrorState(
                isError = true,
                message = error.message ?: error::class.simpleName,
                throwable = error,
                retryable = error is IOException
            )
            onError(errorState)
        }.getOrNull()
    }
}
