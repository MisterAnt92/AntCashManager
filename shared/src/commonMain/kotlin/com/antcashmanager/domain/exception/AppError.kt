package com.antcashmanager.domain.exception

/**
 * Rappresenta gli errori di dominio dell'applicazione.
 * Estende [Throwable] per integrazione con [kotlin.Result].
 */
public sealed class AppError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable(message, cause) {

    /** Errori relativi alla persistenza dei dati. */
    public data class DatabaseError(val msg: String, val t: Throwable? = null) : AppError(msg, t)

    /** Errori di validazione input. */
    public data class ValidationError(val msg: String) : AppError(msg)

    /** Violazioni delle regole di business. */
    public data class BusinessError(val msg: String) : AppError(msg)

    /** Errore di connessione o rete. */
    public data class NetworkError(val msg: String, val t: Throwable? = null) : AppError(msg, t)

    /** Errore sconosciuto o non gestito. */
    public data class UnknownError(val t: Throwable) : AppError(t.message ?: "Unknown error", t)
}
