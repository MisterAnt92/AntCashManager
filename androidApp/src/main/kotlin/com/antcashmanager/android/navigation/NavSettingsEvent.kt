package com.antcashmanager.android.navigation

sealed class NavSettingsEvent {
    data class SetAnalyticsConsent(val granted: Boolean) : NavSettingsEvent()
}
