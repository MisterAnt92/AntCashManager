package com.antcashmanager.android.navigation

import com.antcashmanager.architecture.UIEvent

sealed class NavSettingsEvent : UIEvent() {
    data class SetAnalyticsConsentEvent(val granted: Boolean) : NavSettingsEvent()
}
