package com.antcashmanager.android.domain.usecase.feedback

import android.content.Context
import android.content.Intent

/**
 * UseCase per inviare il feedback via email.
 * Segue il principio di Single Responsibility e mantiene la logica di business
 * fuori da Screen e ViewModel.
 *
 * Riceve applicationContext solo nel metodo per evitare memory leak.
 */
class SendFeedbackEmailUseCase {

    /**
     * Invia un'email di feedback
     * @param applicationContext Contesto dell'applicazione (passato al momento della chiamata)
     * @param emailBody Corpo dell'email localizzato
     * @param versionName Versione dell'app
     * @return true se l'intent è stato lanciato con successo, false se nessuna app email è disponibile
     */
    fun sendFeedbackEmail(
        applicationContext: Context,
        emailBody: String,
        versionName: String,
    ): Boolean {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("misterant.developer@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "AntCashManager Feedback - v$versionName")
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }

        return if (emailIntent.resolveActivity(applicationContext.packageManager) != null) {
            applicationContext.startActivity(emailIntent)
            true
        } else {
            false
        }
    }
}

