package com.antcashmanager.android.domain.usecase.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri
import co.touchlab.kermit.Logger

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
        // Usiamo ACTION_SENDTO con mailto: per una risoluzione più precisa dell'app email su Android 16
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("misterant.developer@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "AntCashManager Feedback - v$versionName")
            putExtra(Intent.EXTRA_TEXT, emailBody)
            // Aggiungiamo NEW_TASK perché usiamo il contesto dell'applicazione
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            applicationContext.startActivity(emailIntent)
            true
        } catch (e: Exception) {
            Logger.e("SendFeedbackEmailUseCase") { "Error launching email intent: ${e.message}" }
            false
        }
    }
}

