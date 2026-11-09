package com.antcashmanager.domain.exception

/** Domain exceptions for profile operations. */
public sealed class ProfileException(message: String) : Exception(message) {
    public object CannotDeleteDefault : ProfileException("Cannot delete the Default profile")
    public object CannotDeleteActive : ProfileException("Cannot delete the currently active profile")
    public class NotFound(id: Long) : ProfileException("Profile $id not found")
}
