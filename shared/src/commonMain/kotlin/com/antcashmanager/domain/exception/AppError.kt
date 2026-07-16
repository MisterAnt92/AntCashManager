package com.antcashmanager.domain.exception

/**
 * Rappresenta gli errori di dominio dell'applicazione.
 * Estende [Throwable] per integrazione con [kotlin.Result].
 */
sealed class AppError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable(message, cause) {

    /** Errori relativi alla persistenza dei dati. */
    data class DatabaseError(val msg: String, val t: Throwable? = null) : AppError(msg, t)

    /** Errori di validazione input. */
    data class ValidationError(val msg: String) : AppError(msg)

    /** Violazioni delle regole di business. */
    data class BusinessError(val msg: String) : AppError(msg)

    /** Errore di connessione o rete. */
    data class NetworkError(val msg: String, val t: Throwable? = null) : AppError(msg, t)

    /** Errore sconosciuto o non gestito. */
    data class UnknownError(val t: Throwable) : AppError(t.message ?: "Unknown error", t)
}
