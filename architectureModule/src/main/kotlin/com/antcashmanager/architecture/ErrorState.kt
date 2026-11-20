package com.antcashmanager.architecture

/**
 * Modello di errore centralizzato per tutti i ViewModel.
 *
 * @param isError true se si è verificato un errore
 * @param message messaggio human-readable
 * @param throwable l'eccezione sottostante (solo per logging, mai esposta direttamente alla UI)
 * @param retryable true se l'operazione può essere ritentata (es. errori di rete)
 */
public data class ErrorState(
    val isError: Boolean = false,
    val message: String? = null,
    val throwable: Throwable? = null,
    val retryable: Boolean = false,
)
