package com.antcashmanager.android.ui.homeTransactionDetail

import android.content.Context
import android.content.Intent
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.screen.homeTransactionDetail.TransactionDetailsViewModel
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.ShareTransactionUseCase
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class TransactionDetailsViewModelMockkTest : BaseUnitTest() {

    private lateinit var shareTransactionUseCase: ShareTransactionUseCase
    private lateinit var context: Context
    private lateinit var chooserIntent: Intent
    private lateinit var viewModel: TransactionDetailsViewModel

    private val expenseTransaction = Transaction(
        id = 1L,
        title = "Groceries",
        amount = 42.0,
        category = "Food",
        type = TransactionType.EXPENSE,
        timestamp = 1_700_000_000_000L,
    )

    private val incomeTransaction = expenseTransaction.copy(type = TransactionType.INCOME)

    @Before
    fun setup() {
        shareTransactionUseCase = mockk()
        context = mockk()
        chooserIntent = mockk(relaxed = true)

        mockkConstructor(Intent::class)
        mockkStatic(Intent::class)

        every { anyConstructed<Intent>().setAction(any()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().putExtra(any<String>(), any<String>()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().setType(any()) } returns mockk(relaxed = true)
        every { context.getString(R.string.transaction_details_share) } returns "Share transaction"
        every { context.startActivity(any()) } just Runs
        every { Intent.createChooser(any(), "Share transaction") } returns chooserIntent

        viewModel = TransactionDetailsViewModel(shareTransactionUseCase)
    }

    @After
    fun tearDown() {
        unmockkConstructor(Intent::class)
        unmockkStatic(Intent::class)
    }

    @Test
    fun shareTransaction_shouldBuildSendIntentWithFormattedText_whenTransactionIsExpense() {
        every {
            shareTransactionUseCase.formatTransactionForShare(expenseTransaction, isIncome = false)
        } returns "formatted expense"

        viewModel.shareTransaction(expenseTransaction, context)

        verify { anyConstructed<Intent>().setAction(Intent.ACTION_SEND) }
        verify { anyConstructed<Intent>().putExtra(Intent.EXTRA_TEXT, "formatted expense") }
        verify { anyConstructed<Intent>().setType("text/plain") }
    }

    @Test
    fun shareTransaction_shouldPassIncomeFlagTrue_whenTransactionTypeIsIncome() {
        every {
            shareTransactionUseCase.formatTransactionForShare(incomeTransaction, isIncome = true)
        } returns "formatted income"

        viewModel.shareTransaction(incomeTransaction, context)

        verify { shareTransactionUseCase.formatTransactionForShare(incomeTransaction, isIncome = true) }
    }

    @Test
    fun shareTransaction_shouldStartChooserActivity_whenIntentIsBuilt() {
        every {
            shareTransactionUseCase.formatTransactionForShare(expenseTransaction, isIncome = false)
        } returns "formatted expense"

        viewModel.shareTransaction(expenseTransaction, context)

        verify { Intent.createChooser(any(), "Share transaction") }
        verify { context.startActivity(chooserIntent) }
    }
}
