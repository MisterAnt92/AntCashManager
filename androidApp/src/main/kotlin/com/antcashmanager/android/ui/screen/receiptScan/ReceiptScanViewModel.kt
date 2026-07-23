package com.antcashmanager.android.ui.screen.receiptScan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.Bundle
import co.touchlab.kermit.Logger
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.ReceiptData
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.receipt.CreateTransactionFromReceiptParams
import com.antcashmanager.domain.usecase.receipt.CreateTransactionFromReceiptUseCase
import com.antcashmanager.domain.usecase.receipt.ScanReceiptUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
) : ViewModel() {

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

    /**
     * Avvia la scansione OCR sull'immagine fornita come [ByteArray].
     * Transita allo step [ReceiptScanStep.PROCESSING], poi [ReceiptScanStep.REVIEW] se ok.
     */
    fun scanReceipt(imageBytes: ByteArray) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            Logger.d(tag = ReceiptScanConstant.TAG) { "Starting receipt scan, bytes=${imageBytes.size}" }
            _state.update {
                it.copy(
                    step = ReceiptScanStep.PROCESSING,
                    isLoading = true,
                    error = null
                )
            }

            scanReceiptUseCase(imageBytes)
                .onSuccess { receiptData ->
                    Logger.i(tag = ReceiptScanConstant.TAG) {
                        "Scan OK: amount=${receiptData.totalAmount}, payee=${receiptData.payee}"
                    }

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
                    Logger.e(throwable = error, tag = ReceiptScanConstant.TAG) { "Scan failed" }
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
    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    /** Aggiorna il beneficiario. */
    fun updatePayee(payee: String) {
        _state.update { it.copy(payee = payee) }
    }

    /** Aggiorna il luogo. */
    fun updateLocation(location: String) {
        _state.update { it.copy(location = location) }
    }

    /** Aggiorna le note. */
    fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    /** Aggiorna l'importo totale durante la fase REVIEW. */
    fun updateAmount(amount: Double) {
        _state.update { it.copy(editedAmount = amount) }
    }

    /** Seleziona una categoria e chiude il dialog. */
    fun selectCategory(category: Category) {
        Logger.d(tag = ReceiptScanConstant.TAG) { "Category selected: ${category.name}" }
        _state.update { it.copy(selectedCategory = category, showCategoryDialog = false) }
    }

    fun showCategoryDialog() {
        _state.update { it.copy(showCategoryDialog = true) }
    }

    /** Chiude il dialog di selezione categoria. */
    fun dismissCategoryDialog() {
        _state.update { it.copy(showCategoryDialog = false) }
    }

    /** Permette all'utente di sovrascrivere il tipo di pagamento rilevato dall'OCR. */
    fun selectPaymentType(paymentType: PaymentType) {
        Logger.d(tag = ReceiptScanConstant.TAG) { "Payment type selected by user: $paymentType" }
        _state.update { it.copy(selectedPaymentType = paymentType, showPaymentTypeDialog = false) }
    }

    /** Mostra il dialog di selezione tipo pagamento. */
    fun showPaymentTypeDialog() {
        _state.update { it.copy(showPaymentTypeDialog = true) }
    }

    /** Chiude il dialog di selezione tipo pagamento. */
    fun dismissPaymentTypeDialog() {
        _state.update { it.copy(showPaymentTypeDialog = false) }
    }

    /** Torna allo step di cattura per ripetere la scansione. */
    fun retryCapture() {
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
            Logger.d(tag = ReceiptScanConstant.TAG) { "Saving transaction from receipt: ${current.title}" }
            _state.update { it.copy(isLoading = true, error = null) }

            val effectiveAmount = current.editedAmount ?: receipt.totalAmount
            if (current.editedAmount != null) {
                Logger.d(tag = ReceiptScanConstant.TAG) {
                    "Amount edited by user: ${receipt.totalAmount} → ${current.editedAmount}"
                }
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
                    Logger.i(tag = ReceiptScanConstant.TAG) { "Transaction saved, id=$id" }
                    _state.update { it.copy(isTransactionSaved = true, isLoading = false) }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    Logger.e(
                        throwable = error,
                        tag = ReceiptScanConstant.TAG
                    ) { "Failed to save transaction" }
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
                    Logger.e(
                        throwable = error,
                        tag = ReceiptScanConstant.TAG
                    ) { "Failed to load categories" }
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
