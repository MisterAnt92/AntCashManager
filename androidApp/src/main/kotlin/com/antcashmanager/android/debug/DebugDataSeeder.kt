package com.antcashmanager.android.debug

import android.content.Context
import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object DebugDataSeeder {
	suspend fun seedIfNeeded(context: Context, transactionDao: TransactionDao) {
		withContext(Dispatchers.IO) {
			// If there are already transactions, skip seeding
			try {
				val count = transactionDao.getCount()
				if (count > 0) return@withContext
			} catch (e: Exception) {
				// if getCount not available or error, proceed to attempt seeding
			}

			try {
				val assetName = "debug_initial_data.json"
				val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
				val obj = JSONObject(json)
				val transactions = obj.optJSONArray("transactions") ?: return@withContext

				for (i in 0 until transactions.length()) {
					val t = transactions.getJSONObject(i)
					val entity = TransactionEntity(
						id = t.optLong("id", 0L),
						title = t.optString("title", "Senza titolo"),
						amount = t.optDouble("amount", 0.0),
						category = t.optString("category", "Uncategorized"),
						type = t.optString("type", "EXPENSE"),
						timestamp = t.optLong("timestamp", System.currentTimeMillis()),
						notes = t.optString("notes", ""),
						payee = t.optString("payee", ""),
						location = t.optString("location", ""),
						isRecurring = t.optBoolean("isRecurring", false),
						tags = (t.optJSONArray("tags")?.let { arr ->
							val list = mutableListOf<String>()
							for (j in 0 until arr.length()) list.add(arr.optString(j))
							list.joinToString(",")
						} ?: ""),
						recurrenceInterval = t.optString("recurrenceInterval", "")
					)
					try {
						transactionDao.insertTransaction(entity)
					} catch (e: Exception) {
						// ignore insertion errors for debug
					}
				}
			} catch (e: Exception) {
				// ignore any error reading assets in debug
			}
		}
	}
}


