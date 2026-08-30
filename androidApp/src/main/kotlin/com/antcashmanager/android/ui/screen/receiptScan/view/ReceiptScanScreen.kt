package com.antcashmanager.android.ui.screen.receiptScan.view

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.selection.AppSelectionItemCard
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanState
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanStep
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanViewModel
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanEvent
import com.antcashmanager.android.ui.screen.transactions.addImport.view.CategorySelectionDialog
import com.antcashmanager.android.ui.screen.transactions.addImport.view.PaymentTypeSelectionDialog
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.ImageCompressionHelper
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.io.File

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN ENTRY POINT
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Schermata principale per la scansione scontrini.
 * Gestisce il ciclo fotocamera → OCR → revisione → salvataggio transazione EXPENSE.
 */
@Composable
fun ReceiptScanScreen(
    onNavigateBack: () -> Unit,
    onTransactionSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReceiptScanViewModel = koinViewModel()
    val analyticsManager: AnalyticsManager = koinInject()

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isTransactionSaved) {
        if (state.isTransactionSaved) {
            analyticsManager.logEvent("receipt_scan_saved")
            onTransactionSaved()
        }
    }

    ReceiptScanContent(
        state = state,
        onImageBytes = { viewModel.onEvent(ReceiptScanEvent.ScanReceipt(it)) },
        onUpdateTitle = { viewModel.onEvent(ReceiptScanEvent.UpdateTitle(it)) },
        onUpdatePayee = { viewModel.onEvent(ReceiptScanEvent.UpdatePayee(it)) },
        onUpdateLocation = { viewModel.onEvent(ReceiptScanEvent.UpdateLocation(it)) },
        onUpdateNotes = { viewModel.onEvent(ReceiptScanEvent.UpdateNotes(it)) },
        onUpdateAmount = { viewModel.onEvent(ReceiptScanEvent.UpdateAmount(it)) },
        onSelectCategory = { viewModel.onEvent(ReceiptScanEvent.SelectCategory(it)) },
        onShowCategoryDialog = { viewModel.onEvent(ReceiptScanEvent.ShowCategoryDialog) },
        onDismissCategoryDialog = { viewModel.onEvent(ReceiptScanEvent.DismissCategoryDialog) },
        onSelectPaymentType = { viewModel.onEvent(ReceiptScanEvent.SelectPaymentType(it)) },
        onShowPaymentTypeDialog = { viewModel.onEvent(ReceiptScanEvent.ShowPaymentTypeDialog) },
        onDismissPaymentTypeDialog = { viewModel.onEvent(ReceiptScanEvent.DismissPaymentTypeDialog) },
        onRetry = { viewModel.onEvent(ReceiptScanEvent.RetryCapture) },
        onSave = viewModel::saveTransaction,
        onClearError = viewModel::clearError,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT COMPOSABLE
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReceiptScanContent(
    state: ReceiptScanState,
    onImageBytes: (ByteArray) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdatePayee: (String) -> Unit,
    onUpdateLocation: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateAmount: (Double) -> Unit,
    onSelectCategory: (Category) -> Unit,
    onShowCategoryDialog: () -> Unit,
    onDismissCategoryDialog: () -> Unit,
    onSelectPaymentType: (PaymentType) -> Unit,
    onShowPaymentTypeDialog: () -> Unit,
    onDismissPaymentTypeDialog: () -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText(stringResource(R.string.receipt_scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (state.step) {
            ReceiptScanStep.CAPTURE ->
                CaptureStep(
                    onImageBytes = onImageBytes,
                    modifier = contentModifier,
                )

            ReceiptScanStep.PROCESSING ->
                ProcessingStep(modifier = contentModifier)

            ReceiptScanStep.REVIEW ->
                ReviewStep(
                    state = state,
                    onUpdateTitle = onUpdateTitle,
                    onUpdatePayee = onUpdatePayee,
                    onUpdateLocation = onUpdateLocation,
                    onUpdateNotes = onUpdateNotes,
                    onUpdateAmount = onUpdateAmount,
                    onShowCategoryDialog = onShowCategoryDialog,
                    onShowPaymentTypeDialog = onShowPaymentTypeDialog,
                    onRetry = onRetry,
                    onSave = onSave,
                    modifier = contentModifier,
                )
        }
    }

    if (state.showCategoryDialog) {
        CategorySelectionDialog(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onSelectCategory = onSelectCategory,
            onDismiss = onDismissCategoryDialog,
        )
    }

    if (state.showPaymentTypeDialog) {
        PaymentTypeSelectionDialog(
            selectedPaymentType = state.selectedPaymentType,
            onSelectPaymentType = onSelectPaymentType,
            onDismiss = onDismissPaymentTypeDialog,
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP: CAPTURE
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CaptureStep(
    onImageBytes: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val analyticsManager: AnalyticsManager = koinInject()

    // Launcher per foto dalla fotocamera (salva in file temp)
    val tempFile = remember { File.createTempFile("receipt_", ".jpg", context.cacheDir) }
    val tempUri = remember {
        if (isPreview) {
            Uri.EMPTY
        } else {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
        }
    }

    val cameraLauncher = if (isPreview) null else rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success && tempFile.exists()) {
            val bytes = tempFile.readBytes()
            if (bytes.isNotEmpty()) {
                analyticsManager.logEvent("receipt_scan_captured")

                // ▼ AGGIUNTO: Comprimi immagine prima di passare a OCR
                try {
                    val compressedBytes = ImageCompressionHelper.compressImage(bytes)
                    onImageBytes(compressedBytes)
                } catch (e: Exception) {
                    Logger.e(throwable = e) { "Image compression failed, using original" }
                    // Fallback: usa comunque l'immagine originale (downsampling in
                    // MlKitReceiptOcrService gestirà il ridimensionamento)
                    onImageBytes(bytes)
                }
                // ▲ FINE AGGIUNTO
            }
        }
    }

    val requestCameraPermissionLauncher =
        if (isPreview) null else rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                cameraLauncher?.launch(tempUri)
            }
        }

    val galleryLauncher = if (isPreview) null else rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { stream ->
                stream.readBytes()
            }
            if (bytes != null && bytes.isNotEmpty()) {
                analyticsManager.logEvent("receipt_scan_captured")

                // ▼ AGGIUNTO: Comprimi immagine prima di passare a OCR
                try {
                    val compressedBytes = ImageCompressionHelper.compressImage(bytes)
                    onImageBytes(compressedBytes)
                } catch (e: Exception) {
                    Logger.e(throwable = e) { "Image compression failed, using original" }
                    // Fallback: usa comunque l'immagine originale (downsampling in
                    // MlKitReceiptOcrService gestirà il ridimensionamento)
                    onImageBytes(bytes)
                }
                // ▲ FINE AGGIUNTO
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        VerticalSpacer(SpacingSize.LG)
        AppText(
            text = stringResource(R.string.receipt_scan_capture_hint),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalSpacer(SpacingSize.XL)
        AppButton(
            onClick = {
                when {
                    cameraLauncher == null -> Unit
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA,
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                        cameraLauncher.launch(tempUri)
                    }

                    else -> requestCameraPermissionLauncher?.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.receipt_scan_take_photo),
        )
        VerticalSpacer(SpacingSize.SM)
        AppButton(
            onClick = { galleryLauncher?.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.receipt_scan_pick_gallery),
            buttonColor = MaterialTheme.colorScheme.secondary,
            textColor = MaterialTheme.colorScheme.onSecondary,
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP: PROCESSING
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ProcessingStep(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            VerticalSpacer(SpacingSize.MD)
            AppText(
                text = stringResource(R.string.receipt_scan_processing),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP: REVIEW
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ReviewStep(
    state: ReceiptScanState,
    onUpdateTitle: (String) -> Unit,
    onUpdatePayee: (String) -> Unit,
    onUpdateLocation: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateAmount: (Double) -> Unit,
    onShowCategoryDialog: () -> Unit,
    onShowPaymentTypeDialog: () -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val analyticsManager: AnalyticsManager = koinInject()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Importo estratto
        ReceiptAmountCard(
            amount = state.receiptData?.totalAmount ?: 0.0,
            editedAmount = state.editedAmount,
            onAmountChange = { newAmount ->
                analyticsManager.logEvent("receipt_scan_amount_edited")
                onUpdateAmount(newAmount)
            },
            vatNote = state.vatNote,
        )

        // Selezione categoria (AppSelectionItemCard style)
        AppSelectionItemCard(
            label = stringResource(R.string.add_transaction_select_category),
            value = state.selectedCategory?.name
                ?: stringResource(R.string.add_transaction_none),
            icon = state.selectedCategory?.icon,
            isEditable = true,
            onClick = onShowCategoryDialog,
        )

        // Selezione tipo pagamento (AppSelectionItemCard style)
        AppSelectionItemCard(
            label = stringResource(R.string.receipt_scan_select_payment_type),
            value = when (state.selectedPaymentType) {
                PaymentType.ELECTRONIC -> stringResource(R.string.payment_type_electronic)
                PaymentType.CASH -> stringResource(R.string.payment_type_cash)
                PaymentType.MEAL_VOUCHERS -> stringResource(R.string.payment_type_meal_vouchers)
            },
            icon = when (state.selectedPaymentType) {
                PaymentType.ELECTRONIC -> "💳"
                PaymentType.CASH -> "💵"
                PaymentType.MEAL_VOUCHERS -> "🎫"
            },
            isEditable = true,
            onClick = onShowPaymentTypeDialog,
        )

        // Campo titolo (con shape RoundedCornerShape)
        OutlinedTextField(
            value = state.title,
            onValueChange = onUpdateTitle,
            label = { AppText(stringResource(R.string.receipt_scan_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
        )

        // Campo beneficiario
        OutlinedTextField(
            value = state.payee,
            onValueChange = onUpdatePayee,
            label = { AppText(stringResource(R.string.receipt_scan_payee_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
        )

        // Campo luogo
        OutlinedTextField(
            value = state.location,
            onValueChange = onUpdateLocation,
            label = { AppText(stringResource(R.string.receipt_scan_location_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
        )

        // Campo note (Dettaglio prodotti)
        OutlinedTextField(
            value = state.notes,
            onValueChange = onUpdateNotes,
            label = { AppText(stringResource(R.string.add_transaction_notes_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
        )

        VerticalSpacer(SpacingSize.SM)

        AppButton(
            text = stringResource(R.string.receipt_scan_save),
            enabled = !state.isLoading && state.selectedCategory != null,
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )

        AppButton(
            text = stringResource(R.string.receipt_scan_retry),
            onClick = {
                analyticsManager.logEvent("receipt_scan_retry")
                onRetry()
            },
            modifier = Modifier.fillMaxWidth(),
            buttonColor = MaterialTheme.colorScheme.secondary,
            textColor = MaterialTheme.colorScheme.onSecondary,
        )
    }
}

@Composable
private fun ReceiptAmountCard(
    amount: Double,
    editedAmount: Double?,
    onAmountChange: (Double) -> Unit,
    vatNote: String,
    modifier: Modifier = Modifier,
) {
    val displayAmount = editedAmount ?: amount
    val isEdited = editedAmount != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isEdited)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppText(
            text = stringResource(R.string.receipt_scan_total_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        OutlinedTextField(
            value = "%.2f".format(displayAmount),
            onValueChange = { newValue ->
                newValue.replace(',', '.').toDoubleOrNull()?.let { validAmount ->
                    if (validAmount > 0.0) {
                        onAmountChange(validAmount)
                    }
                }
            },
            label = { AppText(stringResource(R.string.receipt_scan_total_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            supportingText = if (isEdited) {
                {
                    AppText(
                        text = stringResource(R.string.receipt_scan_amount_edited),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            } else null,
        )

        if (vatNote.isNotBlank()) {
            AppText(
                text = vatNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Preview(showBackground = true, name = "ReceiptScanScreen - 7 inch", widthDp = 600, heightDp = 960)
@Preview(
    showBackground = true,
    name = "ReceiptScanScreen - 10 inch",
    widthDp = 840,
    heightDp = 1280
)
@Composable
fun ReceiptScanCapturePreview() {
    AntCashManagerTheme {
        ReceiptScanContent(
            state = ReceiptScanState(step = ReceiptScanStep.CAPTURE),
            onImageBytes = {},
            onUpdateTitle = {},
            onUpdatePayee = {},
            onUpdateLocation = {},
            onUpdateNotes = {},
            onUpdateAmount = {},
            onSelectCategory = {},
            onShowCategoryDialog = {},
            onDismissCategoryDialog = {},
            onSelectPaymentType = {},
            onShowPaymentTypeDialog = {},
            onDismissPaymentTypeDialog = {},
            onRetry = {},
            onSave = {},
            onClearError = {},
            onNavigateBack = {},
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiptScanReviewPreview() {
    AntCashManagerTheme {
        val category =
            Category(name = "Alimentari", type = "EXPENSE", icon = "🛒", color = 0xFF4CAF50)
        ReceiptScanContent(
            state = ReceiptScanState(
                step = ReceiptScanStep.REVIEW,
                title = "Esselunga",
                payee = "Esselunga S.p.A.",
                location = "Via Roma 1, Milano",
                vatNote = "IVA 22%: €12.50",
                selectedCategory = category,
                selectedPaymentType = PaymentType.ELECTRONIC,
                categories = listOf(category),
                receiptData = com.antcashmanager.domain.model.ReceiptData(
                    totalAmount = 68.90,
                    vatRate = 22.0,
                    vatAmount = 12.50,
                    payee = "Esselunga S.p.A.",
                    location = "Via Roma 1, Milano",
                    paymentType = PaymentType.ELECTRONIC,
                ),
            ),
            onImageBytes = {},
            onUpdateTitle = {},
            onUpdatePayee = {},
            onUpdateLocation = {},
            onUpdateNotes = {},
            onUpdateAmount = {},
            onSelectCategory = {},
            onShowCategoryDialog = {},
            onDismissCategoryDialog = {},
            onSelectPaymentType = {},
            onShowPaymentTypeDialog = {},
            onDismissPaymentTypeDialog = {},
            onRetry = {},
            onSave = {},
            onClearError = {},
            onNavigateBack = {},
            modifier = Modifier,
        )
    }
}
