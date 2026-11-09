package com.antcashmanager.android.navigation

sealed class NavSettingsEvent {
    data class SetAnalyticsConsentEvent(val granted: Boolean) : NavSettingsEvent()
}
