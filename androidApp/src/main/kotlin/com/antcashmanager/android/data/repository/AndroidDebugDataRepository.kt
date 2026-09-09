package com.antcashmanager.android.data.repository

import android.content.Context
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.ui.screen.settings.SettingsConstant
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.DebugDataRepository
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Implementazione Android di DebugDataRepository.
 *
 * Legge il file asset `debug_initial_data.json` e importa
 * i dati di transazione nel database.
 * Esecuzione solo in DEBUG build.
 *
 * Responsabilità:
 * - Leggere asset dal Context (Android-specific)
 * - Parsare JSON
 * - Cancellare dati esistenti
 * - Inserire transazioni via UseCase
 */
class AndroidDebugDataRepository(
    private val context: Context,
    private val deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase,
    private val insertTransactionUseCase: InsertTransactionUseCase,
) : DebugDataRepository {
    override suspend fun importDebugData(): Boolean {
        if (!BuildConfig.DEBUG) {
            Logger.d { "Debug import skipped: not a DEBUG build" }
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                Logger.d { "Importing debug data from assets" }

                // Leggi il file asset
                val json = try {
                    context.assets
                        .open(SettingsConstant.DEBUG_ASSET_NAME)
                        .bufferedReader()
                        .use { it.readText() }
                } catch (ex: Exception) {
                    Logger.e(throwable = ex) { "Cannot open debug asset" }
                    return@withContext false
                }

                val obj = JSONObject(json)
                val transactions = obj.optJSONArray(SettingsConstant.JSON_KEY_TRANSACTIONS)
                    ?: return@withContext false

                // Cancella i dati esistenti per demo
                deleteAllTransactionsUseCase()

                // Importa le transazioni
                for (i in 0 until transactions.length()) {
                    try {
                        val t = transactions.getJSONObject(i)
                        val transaction = Transaction(
                            id = t.optLong(SettingsConstant.JSON_KEY_ID, 0L),
                            title = t.optString(
                                SettingsConstant.JSON_KEY_TITLE,
                                SettingsConstant.DEFAULT_TRANSACTION_TITLE,
                            ),
                            amount = t.optDouble(SettingsConstant.JSON_KEY_AMOUNT, 0.0),
                            category = t.optString(
                                SettingsConstant.JSON_KEY_CATEGORY,
                                SettingsConstant.DEFAULT_TRANSACTION_CATEGORY,
                            ),
                            type = try {
                                TransactionType.valueOf(
                                    t.optString(
                                        SettingsConstant.JSON_KEY_TYPE,
                                        SettingsConstant.DEFAULT_TRANSACTION_TYPE,
                                    ),
                                )
                            } catch (_: Exception) {
                                TransactionType.EXPENSE
                            },
                            timestamp = t.optLong(
                                SettingsConstant.JSON_KEY_TIMESTAMP,
                                System.currentTimeMillis(),
                            ),
                            notes = t.optString(SettingsConstant.JSON_KEY_NOTES, ""),
                            payee = t.optString(SettingsConstant.JSON_KEY_PAYEE, ""),
                            location = t.optString(SettingsConstant.JSON_KEY_LOCATION, ""),
                            isRecurring = t.optBoolean(
                                SettingsConstant.JSON_KEY_IS_RECURRING,
                                false,
                            ),
                            tags = if (t.has(SettingsConstant.JSON_KEY_TAGS)) {
                                t.optJSONArray(SettingsConstant.JSON_KEY_TAGS)?.let { arr ->
                                    val list = mutableListOf<String>()
                                    for (j in 0 until arr.length()) {
                                        list.add(arr.optString(j))
                                    }
                                    list.joinToString(",")
                                } ?: t.optString(SettingsConstant.JSON_KEY_TAGS, "")
                            } else {
                                ""
                            },
                            recurrenceInterval = t.optString(
                                SettingsConstant.JSON_KEY_RECURRENCE_RULE,
                                "",
                            ),
                            paymentType = try {
                                PaymentType.valueOf(
                                    t.optString(
                                        SettingsConstant.JSON_KEY_PAYMENT_TYPE,
                                        SettingsConstant.DEFAULT_PAYMENT_TYPE,
                                    ),
                                )
                            } catch (_: Exception) {
                                PaymentType.ELECTRONIC
                            },
                        )

                        try {
                            insertTransactionUseCase(transaction)
                        } catch (insertError: Exception) {
                            Logger.w(throwable = insertError) { "Failed to insert transaction" }
                        }
                    } catch (entryError: Exception) {
                        Logger.w(throwable = entryError) { "Skipped malformed entry" }
                    }
                }

                Logger.d { "Debug data import completed successfully" }
                return@withContext true
            } catch (ex: Exception) {
                Logger.e(throwable = ex) { "Error importing debug data" }
                return@withContext false
            }
        }
    }
}
