package com.antcashmanager.domain.service

/**
 * Interfaccia per l'estrazione di testo da immagini tramite OCR.
 * L'implementazione è platform-specific (es. ML Kit su Android).
 */
public interface ReceiptOcrService {
    /**
     * Estrae il testo da un'immagine fornita come array di byte.
     *
     * @param imageBytes Bytes dell'immagine (JPEG/PNG).
     * @return [Result] contenente il testo estratto, o un errore di dominio.
     */
    public suspend fun extractText(imageBytes: ByteArray): Result<String>
}
