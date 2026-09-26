package com.antcashmanager.android.ui.screen.home.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.math.abs
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.filter.DateRangeFilter
import com.antcashmanager.android.ui.components.filter.SearchComponent
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.state.AntEmptyState
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.mapper.TransactionUi
import com.antcashmanager.android.ui.screen.home.HomeConstant
import com.antcashmanager.android.ui.screen.home.HomeState
import com.antcashmanager.android.ui.screen.home.event.HomeEvent
import com.antcashmanager.android.ui.screen.home.model.HomeTopCardType
import kotlinx.coroutines.launch

private const val HOME_SCREEN_TAG = "home_screen"
private const val RECENT_COUNT_TAG = "recent_transactions_count"
private const val SEARCH_TAG = "search_component"

/** Scrollable list pane of the Home screen (used both single-pane and as the top pane on foldables). */
@Composable
fun HomeListPane(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    listState: LazyListState,
    horizontalPadding: Dp,
    isExpanded: Boolean,
    onFromDateEdit: () -> Unit,
    onToDateEdit: () -> Unit,
    onTransactionClick: (TransactionUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > HomeConstant.SCROLL_TO_TOP_ITEM_THRESHOLD }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag(HOME_SCREEN_TAG),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                FloatingActionButton(
                    onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = stringResource(R.string.home_scroll_to_top),
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = horizontalPadding, vertical = if (isExpanded) 16.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!state.isSearchExpanded) {
                item {
                    DateRangeFilter(
                        selectedPresetIndex = state.selectedPresetIndex,
                        presets = HomeState.PRESETS,
                        dateRangeFrom = state.dateRangeFrom,
                        dateRangeTo = state.dateRangeTo,
                        expanded = state.dateFilterExpanded,
                        onExpandedChange = { onEvent(HomeEvent.SetDateFilterExpanded(it)) },
                        onPresetSelected = { onEvent(HomeEvent.SelectPreset(it)) },
                        onFromDateEdit = onFromDateEdit,
                        onToDateEdit = onToDateEdit,
                    )
                }
                state.visibleTopCards.forEach { topCardType ->
                    item(key = topCardType.storageKey) { TopCard(topCardType, state) }
                }
            }

            item {
                AppText(
                    text = stringResource(R.string.home_recent_transactions_count, state.transactionCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag(RECENT_COUNT_TAG),
                )
            }

            // Rendered as its own item so the FocusRequester is attached after composition
            if (state.isSearchExpanded) {
                item {
                    SearchComponent(
                        isVisible = true,
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = { onEvent(HomeEvent.UpdateSearchQuery(it)) },
                        searchSuggestions = state.searchSuggestions,
                        modifier = Modifier.testTag(SEARCH_TAG),
                    )
                }
            }

            if (state.recentTransactions.isEmpty()) {
                item {
                    AntEmptyState(
                        mascotRes = R.drawable.ic_piggy_bank,
                        title = stringResource(R.string.empty_state_no_transactions),
                        subtitle = stringResource(R.string.empty_state_no_transactions_subtitle),
                    )
                }
            } else {
                items(items = state.recentTransactions, key = { it.id }) { transaction ->
                    RecentTransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) },
                        displayType = state.transactionDisplayType,
                    )
                }
            }

            item { VerticalSpacer(SpacingSize.XS) }
        }
    }
}

@Composable
private fun TopCard(
    type: HomeTopCardType,
    state: HomeState,
) {
    when (type) {
        HomeTopCardType.BALANCE ->
            BalanceCard(
                balance = state.balance,
                showPaymentTypeBreakdown = state.showPaymentTypeBreakdown,
                balanceByPaymentType = state.balanceByPaymentTypeOrdered,
                reduceMotion = state.reduceMotion,
            )

        HomeTopCardType.INCOME_EXPENSE ->
            IncomeExpenseRow(totalIncome = state.totalIncome, totalExpense = abs(state.totalExpense))

        HomeTopCardType.QUICK_INSIGHTS ->
            QuickInsightsCard(
                transactionCount = state.transactionCount,
                netBalance = state.netBalance,
                averageAmount = state.averageAmount,
                dailyAverageExpense = state.dailyAverageExpense,
                biggestExpenseCategory = state.biggestExpense?.category,
                biggestExpenseAmount = state.biggestExpense?.amount,
            )
    }
}
