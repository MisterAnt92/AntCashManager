package com.antcashmanager.android.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antcashmanager.architecture.BaseViewModel
import com.antcashmanager.architecture.UIEvent
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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
 * - Event queue tramite [Channel] UNLIMITED: eventi accodati FIFO, processati sequenzialmente
 * - Logging centralizzato tramite Kermit ([logDebug], [logInfo], [logWarn], [logError])
 * - Tag automatico derivato da [this::class.simpleName]
 * - Crash tracking tramite [trackCrash]
 * - Dispatcher configurabile per operazioni async
 * - Error handling via [ErrorState] e [handleError] extension
 * - Helper [launchCancelling] per operazioni restartable (cancella il job precedente)
 *
 * @param E il tipo dell'evento, deve estendere [UIEvent]. [Nothing] per i ViewModel senza eventi.
 * @param dispatcher il [CoroutineDispatcher] da usare in operazioni async. Default: [Dispatchers.Default].
 */
abstract class AndroidBaseViewModel<E : UIEvent>(
    protected val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel(), BaseViewModel<E> {

    override val tag: String = this::class.simpleName ?: "ViewModel"
    protected var activeJob: Job? = null

    // ── Event Channel ──────────────────────────────────────────────────────
    // UNLIMITED: trySend è sempre non-blocking su Channel.UNLIMITED.
    private val _eventChannel = Channel<E>(Channel.UNLIMITED)

    /** Punto di ingresso pubblico per tutti gli eventi UI. Non-suspend, thread-safe. */
    final override fun emitEvent(event: E) {
        _eventChannel.trySend(event)
    }

    init {
        // Consumer sequenziale: handleEvent è chiamato uno alla volta, nell'ordine FIFO.
        // try-catch impedisce che un'eccezione in handleEvent blocchi il consumer permanentemente.
        viewModelScope.launch {
            _eventChannel.receiveAsFlow().collect { event ->
                logDebug("→ Event[${event.id}] ${event::class.simpleName}")
                try {
                    handleEvent(event)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logError("Event[${event.id}] handling failed: ${e.message}", e)
                    trackCrash(e)
                }
            }
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
     * Gestisce gli eventi UI. Override necessario nelle subclass che supportano eventi.
     * Implementare con un `when` che delega a metodi privati.
     */
    protected abstract fun handleEvent(event: E)

    /**
     * Cancella il job precedente ([activeJob]) e lancia un nuovo coroutine su [viewModelScope].
     * Pattern standard per operazioni restartable (ricerca, scan, re-encryption): garantisce
     * che non girino due coroutine concorrenti per la stessa operazione.
     */
    protected fun launchCancelling(block: suspend CoroutineScope.() -> Unit): Job {
        activeJob?.cancel()
        return viewModelScope.launch(block = block).also { activeJob = it }
    }

    override fun onCleared() {
        super.onCleared()
        activeJob?.cancel()
        _eventChannel.close()
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
