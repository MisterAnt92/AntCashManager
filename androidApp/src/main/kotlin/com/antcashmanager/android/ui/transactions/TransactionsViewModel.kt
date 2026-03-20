package com.antcashmanager.android.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionsViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    private val insertTransactionUseCase = InsertTransactionUseCase(transactionRepository)
    private val updateTransactionUseCase = UpdateTransactionUseCase(transactionRepository)
    private val deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)
    private val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)

    val transactions = getTransactionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val categories = getCategoriesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun addTransaction(
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        timestamp: Long,
    ) {
        Logger.d("TransactionsViewModel") { "Adding transaction: $title" }
        viewModelScope.launch {
            insertTransactionUseCase(
                Transaction(
                    title = title,
                    amount = amount,
                    category = category,
                    type = type,
                    timestamp = timestamp,
                ),
            )
        }
    }

    fun updateTransaction(transaction: Transaction) {
        Logger.d("TransactionsViewModel") { "Updating transaction: ${transaction.title}" }
        viewModelScope.launch {
            updateTransactionUseCase(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        Logger.d("TransactionsViewModel") { "Deleting transaction: ${transaction.title}" }
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
        }
    }
}

