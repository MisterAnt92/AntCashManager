package com.antcashmanager.android.ui.screen.receiptScan

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.ErrorTracker
import com.antcashmanager.android.analytics.PerformanceTracker
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.ReceiptData
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.receipt.CreateTransactionFromReceiptParams
import com.antcashmanager.domain.usecase.receipt.CreateTransactionFromReceiptUseCase
import com.antcashmanager.domain.usecase.receipt.ScanReceiptUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel per la schermata di scansione scontrini.
 *
 * Coordina:
 * - Scansione OCR tramite [ScanReceiptUseCase] (iniettato, mockabile nei test)
 * - Caricamento categorie EXPENSE tramite [GetCategoriesUseCase]
 * - Creazione transazione tramite [CreateTransactionFromReceiptUseCase]
 * - Suggerimenti storici tramite [GetTransactionSuggestionsUseCase]
 *
 * @param scanReceiptUseCase UseCase per OCR + parsing. Iniettato per testabilità.
 * @param createTransactionUseCase UseCase per salvare la transazione generata.
 * @param getCategoriesUseCase UseCase per caricare le categorie disponibili.
 * @param getTransactionSuggestionsUseCase UseCase per arricchire titolo e luogo da storico.
 */
class ReceiptScanViewModel(
    private val scanReceiptUseCase: ScanReceiptUseCase,
    private val createTransactionUseCase: CreateTransactionFromReceiptUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase,
    private val analyticsManager: AnalyticsManager,
    private val performanceTracker: PerformanceTracker,
    private val errorTracker: ErrorTracker,
) : BaseViewModel<ReceiptScanEvent>() {

    // ── State ─────────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(ReceiptScanState())
    val state: StateFlow<ReceiptScanState> = _state.asStateFlow()

    private var activeJob: Job? = null

    private var distinctTitles = listOf<String>()
    private var distinctLocations = listOf<String>()

    init {
        loadExpenseCategories()
        loadSuggestions()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    override fun onEvent(event: ReceiptScanEvent) {
        logDebug("Event: $event")
        when (event) {
            is ReceiptScanEvent.ScanReceipt -> scanReceipt(event.imageBytes)
            is ReceiptScanEvent.UpdateTitle -> updateTitle(event.title)
            is ReceiptScanEvent.UpdatePayee -> updatePayee(event.payee)
            is ReceiptScanEvent.UpdateLocation -> updateLocation(event.location)
            is ReceiptScanEvent.UpdateNotes -> updateNotes(event.notes)
            is ReceiptScanEvent.UpdateAmount -> updateAmount(event.amount)
            is ReceiptScanEvent.SelectCategory -> selectCategory(event.category)
            is ReceiptScanEvent.SelectPaymentType -> selectPaymentType(event.paymentType)
            is ReceiptScanEvent.ShowCategoryDialog -> showCategoryDialog()
            is ReceiptScanEvent.DismissCategoryDialog -> dismissCategoryDialog()
            is ReceiptScanEvent.ShowPaymentTypeDialog -> showPaymentTypeDialog()
            is ReceiptScanEvent.DismissPaymentTypeDialog -> dismissPaymentTypeDialog()
            is ReceiptScanEvent.RetryCapture -> retryCapture()
            is ReceiptScanEvent.RetryLastOperation -> logInfo("Retry requested")
        }
    }

    /**
     * Avvia la scansione OCR sull'immagine fornita come [ByteArray].
     * Transita allo step [ReceiptScanStep.PROCESSING], poi [ReceiptScanStep.REVIEW] se ok.
     */
    private fun scanReceipt(imageBytes: ByteArray) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            val scanStartTime = System.currentTimeMillis()
            logDebug("Starting receipt scan, bytes=${imageBytes.size}")
            _state.update {
                it.copy(
                    step = ReceiptScanStep.PROCESSING,
                    isLoading = true,
                    error = null
                )
            }

            scanReceiptUseCase(imageBytes)
                .onSuccess { receiptData ->
                    logInfo("Scan OK: amount=${receiptData.totalAmount}, payee=${receiptData.payee}")
                    val duration = System.currentTimeMillis() - scanStartTime
                    performanceTracker.trackReceiptOcrProcessingTime(duration, pages = 1, success = true)

                    val refinedTitle = matchSuggestion(receiptData.payee, distinctTitles)
                        ?: matchAgainstRawText(receiptData.rawText, distinctTitles)
                        ?: receiptData.payee.ifBlank { ReceiptScanConstant.DEFAULT_TITLE_FALLBACK }

                    val refinedLocation = matchSuggestion(receiptData.location, distinctLocations)
                        ?: matchAgainstRawText(receiptData.rawText, distinctLocations)
                        ?: receiptData.location

                    _state.update { current ->
                        current.copy(
                            step = ReceiptScanStep.REVIEW,
                            receiptData = receiptData,
                            title = refinedTitle,
                            payee = receiptData.payee,
                            location = refinedLocation,
                            notes = buildDetailedNotes(receiptData),
                            selectedPaymentType = receiptData.paymentType,
                            vatNote = buildVatNote(receiptData),
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    logError("Scan failed", error)
                    val duration = System.currentTimeMillis() - scanStartTime
                    performanceTracker.trackReceiptOcrProcessingTime(duration, pages = 1, success = false)
                    val errorCode = when {
                        error.message?.contains("timeout") == true -> "network_timeout"
                        error.message?.contains("image") == true -> "invalid_image"
                        error.message?.contains("text") == true -> "text_not_found"
                        else -> "processing_failed"
                    }
                    errorTracker.trackReceiptOcrError(errorCode, retryCount = 0)
                    analyticsManager.logEvent("receipt_scan_failed", Bundle().apply {
                        putString("failure_reason", error.message?.take(40) ?: "unknown")
                    })
                    _state.update {
                        it.copy(
                            step = ReceiptScanStep.CAPTURE,
                            isLoading = false,
                            error = error.message ?: ReceiptScanConstant.ERROR_SCAN,
                        )
                    }
                }
        }
    }

    /** Aggiorna il titolo della transazione. */
    private fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    /** Aggiorna il beneficiario. */
    private fun updatePayee(payee: String) {
        _state.update { it.copy(payee = payee) }
    }

    /** Aggiorna il luogo. */
    private fun updateLocation(location: String) {
        _state.update { it.copy(location = location) }
    }

    /** Aggiorna le note. */
    private fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    /** Aggiorna l'importo totale durante la fase REVIEW. */
    private fun updateAmount(amount: Double) {
        _state.update { it.copy(editedAmount = amount) }
    }

    /** Seleziona una categoria e chiude il dialog. */
    private fun selectCategory(category: Category) {
        logDebug("Category selected: ${category.name}")
        _state.update { it.copy(selectedCategory = category, showCategoryDialog = false) }
    }

    private fun showCategoryDialog() {
        _state.update { it.copy(showCategoryDialog = true) }
    }

    /** Chiude il dialog di selezione categoria. */
    private fun dismissCategoryDialog() {
        _state.update { it.copy(showCategoryDialog = false) }
    }

    /** Permette all'utente di sovrascrivere il tipo di pagamento rilevato dall'OCR. */
    private fun selectPaymentType(paymentType: PaymentType) {
        logDebug("Payment type selected by user: $paymentType")
        _state.update { it.copy(selectedPaymentType = paymentType, showPaymentTypeDialog = false) }
    }

    /** Mostra il dialog di selezione tipo pagamento. */
    private fun showPaymentTypeDialog() {
        _state.update { it.copy(showPaymentTypeDialog = true) }
    }

    /** Chiude il dialog di selezione tipo pagamento. */
    private fun dismissPaymentTypeDialog() {
        _state.update { it.copy(showPaymentTypeDialog = false) }
    }

    /** Torna allo step di cattura per ripetere la scansione. */
    private fun retryCapture() {
        activeJob?.cancel()
        _state.update { ReceiptScanState(categories = _state.value.categories) }
    }

    /**
     * Salva la transazione con i dati revisionati dall'utente.
     * Richiede che una categoria sia selezionata e l'importo > 0.
     */
    fun saveTransaction() {
        val current = _state.value
        val receipt = current.receiptData ?: run {
            _state.update { it.copy(error = ReceiptScanConstant.ERROR_NO_RECEIPT_DATA) }
            return
        }
        val category = current.selectedCategory ?: run {
            _state.update { it.copy(error = ReceiptScanConstant.ERROR_SELECT_CATEGORY) }
            return
        }
        if (receipt.totalAmount <= 0.0) {
            _state.update { it.copy(error = ReceiptScanConstant.ERROR_INVALID_AMOUNT) }
            return
        }

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            logDebug("Saving transaction from receipt: ${current.title}")
            _state.update { it.copy(isLoading = true, error = null) }

            val effectiveAmount = current.editedAmount ?: receipt.totalAmount
            if (current.editedAmount != null) {
                logDebug("Amount edited by user: ${receipt.totalAmount} → ${current.editedAmount}")
                analyticsManager.logEvent("receipt_scan_manual_entry")
            }

            val params = CreateTransactionFromReceiptParams(
                receiptData = receipt.copy(
                    payee = current.payee,
                    location = current.location,
                    totalAmount = effectiveAmount,
                ),
                title = current.title,
                categoryName = category.name,
                categoryIcon = category.icon,
                categoryColor = category.color,
                notes = current.notes,
                paymentType = current.selectedPaymentType, // override utente sul rilevamento OCR
            )

            createTransactionUseCase(params)
                .onSuccess { id ->
                    logInfo("Transaction saved, id=$id")
                    _state.update { it.copy(isTransactionSaved = true, isLoading = false) }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    logError("Failed to save transaction", error)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: ReceiptScanConstant.ERROR_SAVE,
                        )
                    }
                }
        }
    }

    /** Azzera l'errore corrente. */
    fun clearError() = _state.update { it.copy(error = null) }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun loadSuggestions() {
        viewModelScope.launch {
            getTransactionSuggestionsUseCase().collect { result ->
                result.onSuccess { suggestions ->
                    distinctTitles = suggestions.titles
                    distinctLocations = suggestions.locations
                }
            }
        }
    }

    private fun matchSuggestion(input: String, suggestions: List<String>): String? {
        if (input.isBlank()) return null
        return suggestions.find { it.equals(input, ignoreCase = true) }
    }

    private fun matchAgainstRawText(rawText: String, suggestions: List<String>): String? {
        val lines = rawText.lines().map { it.trim().uppercase() }
        // Cerca se qualcuna delle suggerite è presente come sottostringa intera in una riga
        suggestions.forEach { suggestion ->
            val suggestionUpper = suggestion.uppercase()
            if (lines.any { it.contains(suggestionUpper) }) return suggestion
        }
        return null
    }

    private fun buildDetailedNotes(receipt: ReceiptData): String {
        return buildString {
            // 1. IVA
            if (receipt.vatRate > 0.0 || receipt.vatAmount > 0.0) {
                append(ReceiptScanConstant.LABEL_VAT)
                if (receipt.vatRate > 0.0) append(" ${receipt.vatRate.toInt()}%")
                if (receipt.vatAmount > 0.0) append(": €%.2f".format(receipt.vatAmount))
                append("\n\n")
            }

            // 2. Elenco prodotti
            if (receipt.items.isNotEmpty()) {
                append("${ReceiptScanConstant.LABEL_RECEIPT_DETAILS}\n")
                receipt.items.forEach { item ->
                    append("- ${item.name}: €%.2f\n".format(item.price))
                }
            }
        }.trim()
    }

    private fun loadExpenseCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                result.onSuccess { categories ->
                    val expenseCategories = categories.filter {
                        it.type.equals(
                            ReceiptScanConstant.EXPENSE_TYPE,
                            ignoreCase = true
                        ) && !it.isHidden
                    }.sortedBy { it.sortOrder }
                    _state.update { current ->
                        current.copy(
                            categories = expenseCategories,
                            selectedCategory = current.selectedCategory
                                ?: expenseCategories.firstOrNull(),
                        )
                    }
                }.onFailure { error ->
                    if (error is CancellationException) throw error
                    logError("Failed to load categories", error)
                }
            }
        }
    }

    private fun buildVatNote(receipt: ReceiptData): String {
        if (receipt.vatRate <= 0.0 && receipt.vatAmount <= 0.0) return ""
        return buildString {
            append(ReceiptScanConstant.LABEL_VAT)
            if (receipt.vatRate > 0.0) append(" ${receipt.vatRate.toInt()}%")
            if (receipt.vatAmount > 0.0) append(": €%.2f".format(receipt.vatAmount))
        }
    }
}
