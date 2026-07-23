package com.antcashmanager.android.ui.base

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger

open class BaseViewModel : ViewModel() {
    protected val tag: String = this::class.simpleName ?: "ViewModel"

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
