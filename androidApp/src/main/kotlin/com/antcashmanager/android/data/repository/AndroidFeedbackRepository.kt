package com.antcashmanager.android.data.repository

import android.content.Context
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.data.feedback.FeedbackEmailHelper
import com.antcashmanager.domain.repository.FeedbackRepository

/**
 * Android implementation of FeedbackRepository.
 * Encapsulates Context and FeedbackEmailHelper details.
 *
 * @param context Android Context (required for email intents)
 */
public class AndroidFeedbackRepository(
    private val context: Context,
) : FeedbackRepository {
    override suspend fun sendFeedbackEmail(emailBody: String): Boolean {
        return FeedbackEmailHelper.sendFeedbackEmail(
            context = context,
            emailBody = emailBody,
            versionName = BuildConfig.VERSION_NAME,
        )
    }
}
