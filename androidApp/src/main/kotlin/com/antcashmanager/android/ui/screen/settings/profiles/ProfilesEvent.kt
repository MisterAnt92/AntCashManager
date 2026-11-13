package com.antcashmanager.android.ui.screen.settings.profiles

import android.net.Uri
import com.antcashmanager.domain.model.Profile

sealed class ProfilesEvent {
    data class SwitchProfile(val profile: Profile) : ProfilesEvent()
    data object ConfirmSwitch : ProfilesEvent()
    data object DismissSwitch : ProfilesEvent()
    data class DeleteProfile(val profile: Profile) : ProfilesEvent()
    data object ConfirmDelete : ProfilesEvent()
    data object DismissDelete : ProfilesEvent()
    data object ShowCreateDialog : ProfilesEvent()
    data object DismissCreateDialog : ProfilesEvent()
    data class CreateProfile(
        val name: String,
        val colorArgb: Long,
        val avatarEmoji: String,
        val photoUri: Uri? = null,
    ) : ProfilesEvent()

    data class UpdateProfile(
        val profile: Profile,
        val newPhotoUri: Uri? = null,
        val clearPhoto: Boolean = false,
    ) : ProfilesEvent()

    data class ShowEditDialog(val profile: Profile) : ProfilesEvent()
    data object DismissEditDialog : ProfilesEvent()
    data object DismissError : ProfilesEvent()
}
