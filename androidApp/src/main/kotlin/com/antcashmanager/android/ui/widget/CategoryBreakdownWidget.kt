package com.antcashmanager.android.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.antcashmanager.android.R
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.android.util.translateCategoryPlain
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import java.util.Calendar

private const val MAX_CATEGORIES_PER_SECTION = 4

internal data class CategoryTotal(val category: String, val total: Double, val color: Long)

class CategoryBreakdownWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 180.dp),
            DpSize(250.dp, 180.dp),
            DpSize(250.dp, 320.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val transactionRepository = WidgetDependencies.transactionRepository
        val settingsRepository = WidgetDependencies.settingsRepository

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val from = calendar.timeInMillis
        val to = System.currentTimeMillis()

        val transactions = transactionRepository.getTransactionsByDateRange(from, to).first()
        val currencyFormat = loadCurrencyFormat(settingsRepository)
        val palette = loadWidgetPalette(settingsRepository)

        val income = aggregateByCategory(transactions.filter { it.type == TransactionType.INCOME })
        val expenses =
            aggregateByCategory(transactions.filter { it.type == TransactionType.EXPENSE })

        provideContent {
            CategoryBreakdownContent(income, expenses, currencyFormat, palette)
        }
    }
}

class CategoryBreakdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CategoryBreakdownWidget()
}

private fun aggregateByCategory(transactions: List<Transaction>): List<CategoryTotal> =
    transactions.groupBy { it.category }
        .map { (category, txs) ->
            CategoryTotal(
                category,
                txs.sumOf { it.amount },
                txs.first().categoryColor
            )
        }
        .sortedByDescending { it.total }
        .take(MAX_CATEGORIES_PER_SECTION)

@Composable
private fun CategoryBreakdownContent(
    income: List<CategoryTotal>,
    expenses: List<CategoryTotal>,
    currencyFormat: CurrencyFormat,
    palette: WidgetPalette,
) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(palette.background)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionRunCallback<CategoryBreakdownWidgetTapAction>()),
    ) {
        Text(
            text = context.getString(R.string.widget_category_breakdown_title),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(palette.primaryText)
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))

        if (income.isEmpty() && expenses.isEmpty()) {
            Text(
                text = context.getString(R.string.empty_state_no_transactions),
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(palette.secondaryText)),
            )
        } else {
            if (expenses.isNotEmpty()) {
                CategorySection(
                    title = context.getString(R.string.charts_expenses),
                    categories = expenses,
                    currencyFormat = currencyFormat,
                    palette = palette,
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
            if (income.isNotEmpty()) {
                CategorySection(
                    title = context.getString(R.string.charts_income),
                    categories = income,
                    currencyFormat = currencyFormat,
                    palette = palette,
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    categories: List<CategoryTotal>,
    currencyFormat: CurrencyFormat,
    palette: WidgetPalette,
) {
    val context = LocalContext.current
    val maxTotal = categories.maxOf { it.total }

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(palette.secondaryText)
            ),
        )
        categories.forEach { categoryTotal ->
            CategoryBar(
                name = translateCategoryPlain(context, categoryTotal.category),
                amountLabel = formatAmount(categoryTotal.total, currencyFormat),
                color = Color(categoryTotal.color),
                fraction = if (maxTotal > 0) (categoryTotal.total / maxTotal).toFloat() else 0f,
                palette = palette,
            )
        }
    }
}

private const val BAR_MAX_WIDTH_DP = 120
private const val BAR_MIN_WIDTH_DP = 3

@Composable
private fun CategoryBar(
    name: String,
    amountLabel: String,
    color: Color,
    fraction: Float,
    palette: WidgetPalette
) {
    Column(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = name,
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(palette.primaryText)),
            )
            Text(
                text = amountLabel,
                maxLines = 1,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(palette.primaryText)
                ),
            )
        }
        Spacer(modifier = GlanceModifier.height(3.dp))
        Box(
            modifier = GlanceModifier
                .width(BAR_MAX_WIDTH_DP.dp)
                .height(4.dp)
                .background(palette.trackBackground)
                .cornerRadius(2.dp),
        ) {
            val barFraction = fraction.coerceIn(0f, 1f)
            if (barFraction > 0f) {
                val barWidth =
                    (BAR_MAX_WIDTH_DP * barFraction).toInt().coerceAtLeast(BAR_MIN_WIDTH_DP)
                Box(
                    modifier = GlanceModifier
                        .width(barWidth.dp)
                        .height(4.dp)
                        .background(color)
                        .cornerRadius(2.dp),
                ) {}
            }
        }
    }
}
