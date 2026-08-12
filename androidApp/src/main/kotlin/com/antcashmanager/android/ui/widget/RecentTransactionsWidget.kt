package com.antcashmanager.android.ui.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.util.formatAmountWithSign
import com.antcashmanager.android.util.translateCategoryPlain
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import java.util.Locale

private const val MAX_TRANSACTIONS = 20

class RecentTransactionsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 120.dp),
            DpSize(250.dp, 120.dp),
            DpSize(250.dp, 300.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val transactionRepository = WidgetDependencies.transactionRepository
            val settingsRepository = WidgetDependencies.settingsRepository

            val transactions =
                transactionRepository.getAllTransactions().first().take(MAX_TRANSACTIONS)
            val currencyFormat = loadCurrencyFormat(settingsRepository)
            val palette = loadWidgetPalette(settingsRepository)
            val language = loadLanguage(settingsRepository)

            val localizedContext = if (language != AppLanguage.SYSTEM) {
                val locale = Locale.forLanguageTag(language.code)
                val config = Configuration(context.resources.configuration).apply {
                    setLocale(locale)
                }
                context.createConfigurationContext(config)
            } else {
                context
            }

            provideContent {
                CompositionLocalProvider(LocalContext provides localizedContext) {
                    RecentTransactionsContent(transactions, currencyFormat, palette)
                }
            }
        } catch (e: Exception) {
            Logger.e(tag = "RecentTransactionsWidget") { "Error providing glance: ${e.message}" }
            // In caso di errore critico (es. Koin non pronto), mostriamo un contenuto minimo
            // invece di lasciare che il launcher mostri "Errore nel caricamento".
            provideContent {
                ErrorWidgetContent(context)
            }
        }
    }
}

@Composable
private fun ErrorWidgetContent(context: Context) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.error_generic),
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
        )
    }
}

class RecentTransactionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RecentTransactionsWidget()
}

@Composable
private fun RecentTransactionsContent(
    transactions: List<Transaction>,
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
            .clickable(actionRunCallback<RecentTransactionsWidgetTapAction>()),
    ) {
        Text(
            text = context.getString(R.string.widget_recent_transactions_title),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(palette.primaryText)
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (transactions.isEmpty()) {
            Text(
                text = context.getString(R.string.empty_state_no_transactions),
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(palette.secondaryText)),
            )
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(transactions, itemId = { it.id }) { transaction ->
                    TransactionRow(transaction, currencyFormat, palette)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    currencyFormat: CurrencyFormat,
    palette: WidgetPalette
) {
    val context = LocalContext.current
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(10.dp)
                .background(Color(transaction.categoryColor))
                .cornerRadius(5.dp),
        ) {}
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = transaction.title.ifBlank {
                    translateCategoryPlain(
                        context,
                        transaction.category
                    )
                },
                maxLines = 1,
                style = TextStyle(fontSize = 13.sp, color = ColorProvider(palette.primaryText)),
            )
            Text(
                text = translateCategoryPlain(context, transaction.category),
                maxLines = 1,
                style = TextStyle(fontSize = 11.sp, color = ColorProvider(palette.secondaryText)),
            )
        }
        Text(
            text = formatAmountWithSign(transaction.amount, currencyFormat, isIncome),
            maxLines = 1,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(amountColor)
            ),
        )
    }
}
