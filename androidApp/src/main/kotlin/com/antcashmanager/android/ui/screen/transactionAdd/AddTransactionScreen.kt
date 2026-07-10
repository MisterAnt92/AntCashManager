package com.antcashmanager.android.ui.screen.transactionAdd

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.screen.transactionAdd.view.CategorySelectionStep
import com.antcashmanager.android.ui.screen.transactionAdd.view.DetailsStep
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.TransactionType
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,
    onNavigateBack: () -> Unit,
    onTransactionAdded: () -> Unit,
) {
    Logger.d("AddTransactionScreen") { "Displaying AddTransactionScreen" }

    val viewModel: AddTransactionViewModel = koinViewModel { parametersOf(transactionId) }
    val analyticsManager: AnalyticsManager = koinInject()

    val state by viewModel.state.collectAsState()

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
            onTransactionAdded()
        }
    }

    AddTransactionContent(
        state = state,
        onEvent = { event -> viewModel.onEvent(event) },
        onNavigateBack = {
            analyticsManager.logEvent("transaction_form_cancelled")
            onNavigateBack()
        },
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT – flusso semplificato a 2 step
// ══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun AddTransactionContent(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }

        state.currentStep == AddTransactionStep.CATEGORY_SELECTION -> {
            CategorySelectionStep(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onSelectCategory = { onEvent(AddTransactionEvent.SelectCategory(it)) },
                onCancel = onNavigateBack,
            )
        }

        state.currentStep == AddTransactionStep.DETAILS -> {
            DetailsStep(
                state = state,
                onEvent = onEvent,
                onNavigateBack = onNavigateBack,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

@androidx.compose.ui.tooling.preview.Preview
@androidx.compose.ui.tooling.preview.Preview(
    name = "AddTransactionScreen - 7 inch",
    widthDp = 600,
    heightDp = 960,
)
@androidx.compose.ui.tooling.preview.Preview(
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
            onNavigateBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
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
            onNavigateBack = {},
        )
    }
}

