package com.antcashmanager.android.ui

import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

/**
 * CompositionLocal che espone il Locale corrente globalmente nella tree di composable.
 * Usato come fallback per stringhe che potrebbero non localizzarsi tramite LocalContext.current.
 *
 * Inizializzato in MainActivity.WithAppLocale() per sincronizzare con LocalContext.
 */
val LocalLocale = compositionLocalOf<Locale> { Locale.getDefault() }
