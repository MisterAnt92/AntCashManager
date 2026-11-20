package com.antcashmanager.architecture

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract base per tutti i ViewModel dell'applicazione.
 *
 * Definisce:
 * - [state]: stato UI osservabile, aggiornato via [StateFlow]
 * - [emitEvent]: punto di ingresso unico per gli eventi UI
 * - [tag]: identificatore usato nei log (implementazione default: nome della classe)
 * - logging centralizzato tramite Kermit ([logDebug], [logInfo], [logWarn], [logError])
 *
 * Le implementazioni Android estendono [androidx.lifecycle.ViewModel] e implementano questa
 * interfaccia; questo design rispetta il vincolo Kotlin di ereditarietà singola.
 *
 * @param E il tipo dell'evento, deve estendere [UIEvent]. [Nothing] per i ViewModel senza eventi.
 * @param S il tipo dello state, deve estendere [UIState].
 */
public interface BaseViewModel<E : UIEvent, S : UIState> {

    /** Stato UI corrente. Sempre non-null; aggiornato da [emitEvent] → handleEvent. */
    public val state: StateFlow<S>

    public fun emitEvent(event: E)

    /** Alias di comodo per [emitEvent]. */
    public fun onEvent(event: E): Unit = emitEvent(event)

    /** Tag usato nei messaggi di log. Default: nome semplice della classe. */
    public val tag: String get() = this::class.simpleName ?: "ViewModel"

    public fun logDebug(message: String): Unit = Logger.d(tag = tag) { message }

    public fun logInfo(message: String): Unit = Logger.i(tag = tag) { message }

    public fun logWarn(message: String): Unit = Logger.w(tag = tag) { message }

    public fun logError(
        message: String,
        throwable: Throwable? = null,
    ): Unit =
        if (throwable != null) {
            Logger.e(throwable = throwable, tag = tag) { message }
        } else {
            Logger.e(tag = tag) { message }
        }
}
