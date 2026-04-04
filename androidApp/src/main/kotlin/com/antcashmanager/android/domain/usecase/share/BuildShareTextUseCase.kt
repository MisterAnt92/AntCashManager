package com.antcashmanager.android.domain.usecase.share

import android.content.Context
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.screen.charts.MonthlyAmount
import com.antcashmanager.android.ui.screen.charts.YearlyAmount
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.domain.model.CurrencyFormat

/**
 * UseCase per costruire il testo da condividere per i vari tipi di report.
 * Segue il principio di Single Responsibility e mantiene la logica di business
 * fuori da Screen e ViewModel.
 */
class BuildShareTextUseCase(private val context: Context) {

    /**
     * Costruisce il testo da condividere per le uscite per categoria.
     */
    fun buildCategoryShareText(
        data: Map<String, Double>,
        fmt: CurrencyFormat,
    ): String {
        val total = data.values.sum()
        val sb = StringBuilder()
        sb.appendLine("📊 ${context.getString(R.string.share_category_title)}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        data.entries.forEach { (category, value) ->
            val percentage = if (total > 0) (value / total * 100) else 0.0
            sb.appendLine("• $category: ${formatAmount(value, fmt)} (%.1f%%)".format(percentage))
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("💰 ${context.getString(R.string.share_total)}: ${formatAmount(total, fmt)}")
        sb.appendLine("\n— AntCashManager 🐜")
        return sb.toString()
    }

    /**
     * Costruisce il testo da condividere per la panoramica mensile.
     */
    fun buildMonthlyShareText(
        data: List<MonthlyAmount>,
        fmt: CurrencyFormat,
    ): String {
        val sb = StringBuilder()
        sb.appendLine("📅 ${context.getString(R.string.share_monthly_title)}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        var totalIncome = 0.0
        var totalExpense = 0.0
        data.forEach { item ->
            totalIncome += item.income
            totalExpense += item.expense
            val balance = item.income - item.expense
            val balanceSymbol = if (balance >= 0) "📈" else "📉"
            sb.appendLine("${item.label}:")
            sb.appendLine("  💰 ${context.getString(R.string.share_income)}: ${formatAmount(item.income, fmt)}")
            sb.appendLine("  💸 ${context.getString(R.string.share_expense)}: ${formatAmount(item.expense, fmt)}")
            sb.appendLine("  $balanceSymbol ${context.getString(R.string.share_balance)}: ${formatAmount(balance, fmt)}")
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📊 ${context.getString(R.string.share_total_income)}: ${formatAmount(totalIncome, fmt)}")
        sb.appendLine("📊 ${context.getString(R.string.share_total_expense)}: ${formatAmount(totalExpense, fmt)}")
        sb.appendLine("📊 ${context.getString(R.string.share_final_balance)}: ${formatAmount(totalIncome - totalExpense, fmt)}")
        sb.appendLine("\n— AntCashManager 🐜")
        return sb.toString()
    }

    /**
     * Costruisce il testo da condividere per la panoramica annuale.
     */
    fun buildYearlyShareText(
        data: List<YearlyAmount>,
        fmt: CurrencyFormat,
    ): String {
        val sb = StringBuilder()
        sb.appendLine("📆 ${context.getString(R.string.share_yearly_title)}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        var totalIncome = 0.0
        var totalExpense = 0.0
        data.forEach { item ->
            totalIncome += item.income
            totalExpense += item.expense
            val balance = item.income - item.expense
            val balanceSymbol = if (balance >= 0) "📈" else "📉"
            sb.appendLine("${item.label}:")
            sb.appendLine("  💰 ${context.getString(R.string.share_income)}: ${formatAmount(item.income, fmt)}")
            sb.appendLine("  💸 ${context.getString(R.string.share_expense)}: ${formatAmount(item.expense, fmt)}")
            sb.appendLine("  $balanceSymbol ${context.getString(R.string.share_balance)}: ${formatAmount(balance, fmt)}")
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📊 ${context.getString(R.string.share_total_income)}: ${formatAmount(totalIncome, fmt)}")
        sb.appendLine("📊 ${context.getString(R.string.share_total_expense)}: ${formatAmount(totalExpense, fmt)}")
        sb.appendLine("📊 ${context.getString(R.string.share_final_balance)}: ${formatAmount(totalIncome - totalExpense, fmt)}")
        sb.appendLine("\n— AntCashManager 🐜")
        return sb.toString()
    }
}

