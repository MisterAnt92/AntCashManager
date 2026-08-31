package com.antcashmanager.android.ui.screen.transactions.addImport

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.screen.transactions.addImport.event.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transactions.addImport.view.CategorySelectionStep
import com.antcashmanager.android.ui.screen.transactions.addImport.view.DetailsStep
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.TransactionType
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN (FASE 7c: Refactored to remove callbacks, use navController directly)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,
    navController: NavController? = null,
    modifier: Modifier = Modifier,
) {
    Logger.d(tag = "AddTransactionScreen") { "Displaying AddTransactionScreen" }

    val viewModel: AddTransactionViewModel = koinViewModel { parametersOf(transactionId) }
    val analyticsManager: AnalyticsManager = koinInject()

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(transactionId) {
        val params = Bundle().apply {
            putString("mode", if (transactionId != null) "update" else "create")
        }
        analyticsManager.logEvent("transaction_form_opened", params)
    }

    // Naviga indietro quando la transazione è stata salvata con successo
    LaunchedEffect(state.isTransactionSaved) {
        if (state.isTransactionSaved) {
            val params = Bundle().apply {
                putString("mode", if (state.isModifying) "update" else "create")
                putString("transaction_type", state.selectedType?.name ?: "unknown")
            }
            analyticsManager.logEvent("transaction_submit_success", params)
            navController?.popBackStack()
        }
    }

    AddTransactionContent(
        state = state,
        onEvent = { event -> viewModel.onEvent(event) },
        navController = navController,
        analyticsManager = analyticsManager,
        modifier = modifier,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT – flusso semplificato a 2 step (FASE 7c: Refactored to use navController)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun AddTransactionContent(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    navController: NavController? = null,
    analyticsManager: AnalyticsManager? = null,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        state.currentStep == AddTransactionStep.CATEGORY_SELECTION -> {
            CategorySelectionStep(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onSelectCategory = { onEvent(AddTransactionEvent.SelectCategory(it)) },
                onCancel = {
                    analyticsManager?.logEvent("transaction_form_cancelled")
                    navController?.popBackStack()
                },
            )
        }

        state.currentStep == AddTransactionStep.DETAILS -> {
            DetailsStep(
                state = state,
                onEvent = onEvent,
                onNavigateBack = {
                    analyticsManager?.logEvent("transaction_form_cancelled")
                    navController?.popBackStack()
                },
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

@Preview
@Preview(
    name = "AddTransactionScreen - 7 inch",
    widthDp = 600,
    heightDp = 960,
)
@Preview(
    name = "AddTransactionScreen - 10 inch",
    widthDp = 840,
    heightDp = 1280,
)
@Composable
fun AddTransactionScreenNewPreview() {
    MaterialTheme {
        AddTransactionContent(
            state = AddTransactionState(
                currentStep = AddTransactionStep.DETAILS,
                selectedCategory = Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
                selectedType = TransactionType.EXPENSE,
                title = "Pizza",
                amount = "12.50",
            ),
            onEvent = {},
            navController = null,
            analyticsManager = null,
        )
    }
}

@Preview
@Composable
fun AddTransactionScreenEditPreview() {
    MaterialTheme {
        AddTransactionContent(
            state = AddTransactionState(
                currentStep = AddTransactionStep.DETAILS,
                isModifying = true,
                selectedCategory = Category(2, "Salary", "💰", 0xFF51CF66, "INCOME"),
                selectedType = TransactionType.INCOME,
                title = "Stipendio Marzo",
                amount = "1500.00",
                notes = "Stipendio mensile",
            ),
            onEvent = {},
            navController = null,
            analyticsManager = null,
        )
    }
}

