package com.antcashmanager.android.data.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri
import co.touchlab.kermit.Logger

/**
 * Android-specific helper for sending feedback via email.
 * Lives in data layer (not domain) because it requires android.content.Context and Intent.
 */
object FeedbackEmailHelper {

    /**
     * Sends a feedback email intent to the default email client.
     * @param context Application context
     * @param emailBody Localized email body content
     * @param versionName App version to include in subject
     * @return true if the intent was launched successfully, false if no email app is available
     */
    fun sendFeedbackEmail(
        context: Context,
        emailBody: String,
        versionName: String,
    ): Boolean {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("misterant.developer@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "AntCashManager Feedback - v$versionName")
            putExtra(Intent.EXTRA_TEXT, emailBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(emailIntent)
            true
        } catch (e: Exception) {
            Logger.e(tag = "FeedbackEmailHelper") { "Error launching email intent: ${e.message}" }
            false
        }
    }
}
