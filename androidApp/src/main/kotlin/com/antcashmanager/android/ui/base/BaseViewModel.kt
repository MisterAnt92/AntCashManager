package com.antcashmanager.android.ui.base

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Base class per tutti i ViewModel dell'app con supporto generico per eventi e dispatcher.
 *
 * Fornisce:
 * - Logging centralizzato tramite helper ([logDebug], [logInfo], [logWarn], [logError])
 * - Tag automatico derivato da [this::class.simpleName]
 * - Supporto per gli eventi tramite [onEvent] (override necessario nei ViewModel con eventi)
 * - Dispatcher configurabile (default [Dispatchers.Default]) per l'esecuzione di operazioni async
 *
 * I ViewModel senza eventi estendono [BaseViewModel]<[Nothing]>; quelli con eventi specificano
 * il tipo dell'evento (e.g., [BaseViewModel]<HomeEvent>).
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
}
