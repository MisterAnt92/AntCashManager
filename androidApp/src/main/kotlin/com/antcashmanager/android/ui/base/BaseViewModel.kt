package com.antcashmanager.android.ui.base

import androidx.lifecycle.ViewModel
import android.os.Bundle
import co.touchlab.kermit.Logger
import com.antcashmanager.android.analytics.AnalyticsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Rappresenta lo stato di errore in un ViewModel.
 *
 * @property isError true se c'è un errore attivo
 * @property message Messaggio di errore user-friendly
 * @property throwable Exception sottostante (per logging/debugging)
 * @property retryable true se l'operazione può essere ritenuta (es. network error)
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
 * - Error handling centralizzato tramite [handleError] (elimina 36 duplicazioni)
 * - Lifecycle hooks [onViewModelCreated] e [onViewModelCleared]
 *
 * I ViewModel senza eventi estendono [BaseViewModel]<[Nothing]>; quelli con eventi specificano
 * il tipo dell'evento (e.g., [BaseViewModel]<HomeEvent>).
 *
 * **Pattern di Error Handling:**
 * ```kotlin
 * viewModelScope.launch {
 *     repository.fetchData()
 *         .handleError(viewModel)  // Centralizzato: logs + optional onError callback
 *         ?.let { data -> _state.update { it.copy(data = data) } }
 * }
 * ```
 *
 * @param E il tipo dell'evento. [Nothing] per i ViewModel senza eventi.
 * @param dispatcher il [CoroutineDispatcher] da usare in operazioni async. Default: [Dispatchers.Default].
 */
abstract class BaseViewModel<E : Any>(
    protected val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel(), KoinComponent {

    protected val tag: String = this::class.simpleName ?: "ViewModel"

    // Lazy injection di AnalyticsManager via Koin (safe if not available)
    private val analyticsManager: AnalyticsManager? by lazy {
        runCatching {
            getKoin().get<AnalyticsManager>()
        }.getOrNull()
    }

    init {
        onViewModelCreated()
        // Track ViewModel creation with analytics
        analyticsManager?.logEvent(
            "viewmodel_created",
            Bundle().apply {
                putString("viewmodel_class", tag)
            }
        )
    }

    override fun onCleared() {
        // Track ViewModel destruction with analytics
        analyticsManager?.logEvent(
            "viewmodel_cleared",
            Bundle().apply {
                putString("viewmodel_class", tag)
            }
        )
        onViewModelCleared()
        super.onCleared()
    }

    /**
     * Hook chiamato quando il ViewModel è creato.
     * Override per aggiungere logica di inizializzazione custom.
     */
    open fun onViewModelCreated() {
        // Default: nessuna azione
    }

    /**
     * Hook chiamato quando il ViewModel è pulito/distrutto.
     * Override per aggiungere logica di cleanup custom.
     */
    open fun onViewModelCleared() {
        // Default: nessuna azione
    }

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
     * Gestisce gli errori da Result in modo centralizzato.
     *
     * Automaticamente:
     * - Propaga [CancellationException]
     * - Crea [ErrorState] con metadati (message, throwable, retryable)
     * - Chiama il callback onError
     * - Logga l'errore
     *
     * @param onError Callback opzionale per gestire l'errore (es. update UI state)
     * @return il valore di successo, o null se errore
     *
     * **Uso:**
     * ```kotlin
     * viewModelScope.launch {
     *     repository.fetchData()
     *         .handleError { errorState ->
     *             _state.update { it.copy(errorState = errorState) }
     *         }
     *         ?.let { data -> _state.update { it.copy(data = data) } }
     * }
     * ```
     */
    protected fun <T> Result<T>.handleError(
        onError: (errorState: ErrorState) -> Unit = { },
    ): T? {
        return onFailure { error ->
            // Propaga CancellationException senza creare error state
            if (error is CancellationException) throw error

            // Crea error state con metadati
            val errorState = ErrorState(
                isError = true,
                message = error.message ?: "Unknown error occurred",
                throwable = error,
                retryable = error is IOException, // Network errors sono retryable
            )

            // Log l'errore
            logError(errorState.message!!, throwable = error)

            // Chiama il callback
            onError(errorState)
        }.getOrNull()
    }
}
