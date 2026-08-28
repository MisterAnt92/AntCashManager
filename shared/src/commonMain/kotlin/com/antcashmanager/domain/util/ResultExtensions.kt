package com.antcashmanager.domain.util

import kotlinx.coroutines.CancellationException

/**
 * Simile a [runCatching], ma garantisce che le [CancellationException] non vengano catturate.
 * Questo è fondamentale per il corretto funzionamento della structured concurrency in Kotlin Coroutines.
 */
public inline fun <R> runSuspendCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
