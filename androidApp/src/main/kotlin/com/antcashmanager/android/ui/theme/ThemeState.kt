package com.antcashmanager.android.ui.theme

import com.antcashmanager.architecture.UIState

/** Placeholder state per ThemeViewModel. Le preferenze tema sono esposte tramite StateFlow individuali. */
data class ThemeState(val placeholder: Unit = Unit) : UIState()
