package com.antcashmanager.android.ui.screen.home.transactionDetail

import com.antcashmanager.architecture.ErrorState
import com.antcashmanager.architecture.UIState

data class TransactionDetailsState(
    val isLoading: Boolean = false,
    val errorState: ErrorState = ErrorState(),
) : UIState()
