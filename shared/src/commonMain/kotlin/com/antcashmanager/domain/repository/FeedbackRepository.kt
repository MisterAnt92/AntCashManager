package com.antcashmanager.domain.repository

/**
 * Repository for feedback-related operations.
 * Implementation is platform-specific (Android-only for now).
 */
public interface FeedbackRepository {
    /**
     * Send feedback email with the given body.
     *
     * @param emailBody The email body text
     * @return true if email was sent successfully, false otherwise
     */
    public suspend fun sendFeedbackEmail(emailBody: String): Boolean
}
