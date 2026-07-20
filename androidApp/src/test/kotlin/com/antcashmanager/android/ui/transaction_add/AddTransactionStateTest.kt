package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionState
import com.antcashmanager.domain.model.PaymentType
import org.junit.Assert.assertEquals
import org.junit.Test

class AddTransactionStateTest {

    @Test
    fun totalAmount_shouldEqualEnteredAmount_whenPaymentTypeIsElectronic() {
        val state = AddTransactionState(
            amount = "42.50",
            selectedPaymentType = PaymentType.ELECTRONIC,
            mealVoucherCount = "3",
            mealVoucherValue = 5.29,
        )

        assertEquals(42.50, state.totalAmount, 0.001)
    }

    @Test
    fun totalAmount_shouldEqualEnteredAmount_whenPaymentTypeIsMealVouchers() {
        // L'importo inserito rappresenta gia' il totale del pagamento (buoni + differenza):
        // non deve essere sommato al subtotale dei buoni.
        val state = AddTransactionState(
            amount = "20.00",
            selectedPaymentType = PaymentType.MEAL_VOUCHERS,
            mealVoucherCount = "3",
            mealVoucherValue = 5.29,
        )

        assertEquals(20.00, state.totalAmount, 0.001)
    }

    @Test
    fun totalAmount_shouldBeZero_whenAmountIsNotANumber() {
        val state = AddTransactionState(amount = "abc")

        assertEquals(0.0, state.totalAmount, 0.001)
    }

    @Test
    fun mealVoucherDifferencePaid_shouldSubtractVoucherSubtotalFromTotal_whenVouchersUsed() {
        val state = AddTransactionState(
            amount = "20.00",
            selectedPaymentType = PaymentType.MEAL_VOUCHERS,
            mealVoucherCount = "3",
            mealVoucherValue = 5.29,
        )

        // 20.00 - (3 * 5.29) = 4.13
        assertEquals(4.13, state.mealVoucherDifferencePaid, 0.001)
    }

    @Test
    fun mealVoucherDifferencePaid_shouldEqualTotal_whenNoVouchersUsed() {
        val state = AddTransactionState(
            amount = "20.00",
            selectedPaymentType = PaymentType.MEAL_VOUCHERS,
            mealVoucherCount = "0",
            mealVoucherValue = 5.29,
        )

        assertEquals(20.00, state.mealVoucherDifferencePaid, 0.001)
    }

    @Test
    fun mealVoucherDifferencePaid_shouldBeNegative_whenVoucherSubtotalExceedsEnteredTotal() {
        val state = AddTransactionState(
            amount = "10.00",
            selectedPaymentType = PaymentType.MEAL_VOUCHERS,
            mealVoucherCount = "5",
            mealVoucherValue = 5.29,
        )

        // 10.00 - (5 * 5.29) = -16.45: segnala all'utente un importo totale incoerente
        assertEquals(-16.45, state.mealVoucherDifferencePaid, 0.001)
    }
}
