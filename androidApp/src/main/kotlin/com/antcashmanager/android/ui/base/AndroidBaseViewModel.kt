package com.antcashmanager.android.ui.base

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.architecture.BaseViewModel
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

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
 * Base class Android per tutti i ViewModel dell'app.
 *
 * Estende [ViewModel] (lifecycle-aware) e implementa [BaseViewModel] (contratto architetturale
 * definito in :architectureModule). Fornisce:
 * - Logging centralizzato tramite Kermit ([logDebug], [logInfo], [logWarn], [logError])
 * - Tag automatico derivato da [this::class.simpleName]
 * - Crash tracking tramite [trackCrash]
 * - Dispatcher configurabile per operazioni async
 * - Error handling via [ErrorState] e [handleError] extension
 * - Gestione eventi UDF: [onEvent] è `final` e delega a [handleEvent] (override nelle subclass)
 *
 * @param E il tipo dell'evento sealed class. [Nothing] per i ViewModel senza eventi.
 * @param dispatcher il [CoroutineDispatcher] da usare in operazioni async. Default: [Dispatchers.Default].
 */
abstract class AndroidBaseViewModel<E : Any>(
    protected val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel(), BaseViewModel<E> {

    protected val tag: String = this::class.simpleName ?: "ViewModel"
    protected var activeJob: Job? = null

    protected fun logDebug(message: String) {
        Logger.d(tag = tag) { message }
    }

    protected fun logInfo(message: String) {
        Logger.i(tag = tag) { message }
    }

    protected fun logWarn(message: String) {
        Logger.w(tag = tag) { message }
    }

    protected fun logError(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            Logger.e(throwable = throwable, tag = tag) { message }
        } else {
            Logger.e(tag = tag) { message }
        }
    }

    /**
     * Registra il crash nelle analisi/diagnostica (es. Firebase Crashlytics).
     * Override per collegarlo a un vero crash reporter.
     */
    protected open fun trackCrash(
        t: Throwable,
        context: Map<String, Any> = emptyMap(),
    ) {
        logError("Crash tracked: ${t.message}", t)
    }

    /**
     * Punto di ingresso unico per gli eventi UI. `final`: non sovrascrivere nelle subclass;
     * implementare invece [handleEvent].
     */
    final override fun onEvent(event: E) = handleEvent(event)

    /**
     * Gestisce gli eventi UI. Override necessario nelle subclass che supportano eventi.
     * Implementare con un `when` che delega a metodi privati.
     */
    protected abstract fun handleEvent(event: E)

    override fun onCleared() {
        super.onCleared()
        activeJob?.cancel()
    }

    /**
     * Centralizzato error handling for Result<T>.
     * Automatically detects retryable errors (IOException) and logs appropriately.
     */
    protected fun <T> Result<T>.handleError(
        onError: (error: ErrorState) -> Unit = { logError(it.message ?: "Unknown error") },
    ): T? =
        onFailure { error ->
            if (error is CancellationException) throw error
            val errorState =
                ErrorState(
                    isError = true,
                    message = error.message ?: error::class.simpleName,
                    throwable = error,
                    retryable = error is IOException,
                )
            onError(errorState)
        }.getOrNull()
}
