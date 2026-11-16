package com.antcashmanager.architecture

import co.touchlab.kermit.Logger

/** Registra un messaggio di debug con il tag fornito. */
public fun <E : Any> BaseViewModel<E>.logDebug(tag: String, msg: String): Unit =
    Logger.d(tag = tag) { msg }

/** Registra un messaggio informativo con il tag fornito. */
public fun <E : Any> BaseViewModel<E>.logInfo(tag: String, msg: String): Unit =
    Logger.i(tag = tag) { msg }

/** Registra un avviso con il tag fornito. */
public fun <E : Any> BaseViewModel<E>.logWarn(tag: String, msg: String): Unit =
    Logger.w(tag = tag) { msg }

/** Registra un errore con il tag fornito e il throwable opzionale. */
public fun <E : Any> BaseViewModel<E>.logError(
    tag: String,
    msg: String,
    t: Throwable? = null,
): Unit = if (t != null) Logger.e(throwable = t, tag = tag) { msg } else Logger.e(tag = tag) { msg }
