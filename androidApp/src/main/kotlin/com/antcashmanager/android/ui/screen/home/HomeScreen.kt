package com.antcashmanager.android.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.R
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import com.antcashmanager.android.navigation.ScreenHeaderConfig
import com.antcashmanager.android.ui.base.LocalMultiPaneCoordinator
import com.antcashmanager.android.ui.components.animation.AntEasterEggAnimation
import com.antcashmanager.android.ui.components.dialog.HelpButton
import com.antcashmanager.android.ui.components.state.AntErrorState
import com.antcashmanager.android.ui.components.layout.FoldableAwareLayout
import com.antcashmanager.android.ui.components.layout.LocalDisplayFeatures
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.overlay.TutorialOverlay
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.transaction.TransactionDetailsPane
import com.antcashmanager.android.ui.screen.home.event.HomeEvent
import com.antcashmanager.android.ui.screen.home.transactionDetail.TransactionDetailsDialog
import com.antcashmanager.android.ui.screen.home.view.HelpDialog
import com.antcashmanager.android.ui.screen.home.view.HomeListPane
import com.antcashmanager.android.ui.screen.home.view.HomeTopCardsOrderDialog
import com.antcashmanager.android.ui.screen.home.view.LoadingState
import org.koin.androidx.compose.koinViewModel

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        navController = navController,
        modifier = modifier,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    modifier: Modifier = Modifier,
) {
    // Pure UI state: dialog visibility
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }

    // Foldable device support
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo(displayFeatures = LocalDisplayFeatures.current)
    val multiPaneCoordinator = LocalMultiPaneCoordinator.current
    val foldingFeature = adaptiveLayoutInfo.foldingFeature

    // Preserva scroll position durante navigazione back/forward
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val headerConfigCallback = LocalScreenHeaderConfigCallback.current
    val dashboardTitle = stringResource(R.string.common_dashboard)
    LaunchedEffect(Unit) {
        headerConfigCallback?.invoke(
            ScreenHeaderConfig(
                title = dashboardTitle,
                showSearchIcon = true,
                hasOrderOption = true,
                onSearchClick = { onEvent(HomeEvent.ToggleSearchExpanded) },
                onOrderClick = { onEvent(HomeEvent.StartTopCardsReorder) },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HelpButton(onHelpClick = { showHelpDialog = true })
                    }
                },
            ),
        )
    }

    // Tutorial full-screen (non durante il caricamento iniziale, per evitare flickering)
    if (!state.isTutorialCompleted && !state.isLoading) {
        TutorialOverlay(onDismiss = { onEvent(HomeEvent.SetIsTutorialCompleted(true)) })
        return
    }

    // Error state overlay (FASE 5: Error Feedback UX)
    if (state.errorState.isError) {
        AntErrorState(
            mascotRes = R.drawable.ic_piggy_bank,
            title = state.errorState.message ?: "An error occurred",
            subtitle = "Please try again",
            modifier = modifier.fillMaxSize(),
            retryLabel = null, // TODO: add RetryLastOperation event to HomeEvent
            onRetry = null,
        )
        return
    }

    if (showFromDatePicker) {
        HomeDatePickerDialog(
            initialDate = state.dateRangeFrom,
            onConfirm = { onEvent(HomeEvent.SetDateRange(it, state.dateRangeTo)) },
            onDismiss = { showFromDatePicker = false },
        )
    }
    if (showToDatePicker) {
        HomeDatePickerDialog(
            initialDate = state.dateRangeTo,
            onConfirm = { onEvent(HomeEvent.SetDateRange(state.dateRangeFrom, it)) },
            onDismiss = { showToDatePicker = false },
        )
    }
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
    state.editingTopCardsOrder?.let { editingOrder ->
        HomeTopCardsOrderDialog(
            order = editingOrder,
            onMoveUp = { onEvent(HomeEvent.MoveTopCard(it, up = true)) },
            onMoveDown = { onEvent(HomeEvent.MoveTopCard(it, up = false)) },
            onDismiss = { onEvent(HomeEvent.CancelTopCardsReorder) },
            onConfirm = { onEvent(HomeEvent.ConfirmTopCardsOrder) },
        )
    }
    state.selectedTransaction?.let { selected ->
        TransactionDetailsDialog(
            transaction = selected,
            onDismiss = { onEvent(HomeEvent.DismissTransactionDetails) },
        )
    }
    if (showVersionDialog) {
        AntEasterEggAnimation(
            versionName = BuildConfig.VERSION_NAME,
            onDismiss = { showVersionDialog = false },
        )
    }

    val listPane: @Composable () -> Unit = {
        HomeListPane(
            state = state,
            onEvent = onEvent,
            listState = listState,
            horizontalPadding = adaptiveLayoutInfo.horizontalPadding,
            isExpanded = adaptiveLayoutInfo.isExpanded,
            onFromDateEdit = { showFromDatePicker = true },
            onToDateEdit = { showToDatePicker = true },
            onTransactionClick = { item ->
                // Foldable split-view sync is a layout concern, so it stays in the screen
                multiPaneCoordinator?.selectTransaction(
                    transaction = item.transaction,
                    navigateToDetailsPane = foldingFeature?.isSeparating == true,
                )
                onEvent(HomeEvent.ShowTransactionDetails(item.transaction))
            },
            modifier = modifier,
        )
    }

    when {
        state.isLoading -> LoadingState()
        adaptiveLayoutInfo.hasFold && foldingFeature != null ->
            FoldableAwareLayout(
                foldingFeature = foldingFeature,
                modifier = Modifier.fillMaxSize(),
                topContent = { _, _ -> listPane() },
                bottomContent = { _, _ ->
                    if (multiPaneCoordinator?.showDetailsPane?.value == true) {
                        TransactionDetailsPane(transaction = state.selectedTransaction)
                    }
                },
            )

        else -> listPane()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDatePickerDialog(
    initialDate: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let(onConfirm)
                    onDismiss()
                },
            ) { AppText(stringResource(R.string.common_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { AppText(stringResource(R.string.common_cancel)) }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
