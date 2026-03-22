package com.antcashmanager.android

import android.app.Application
import co.touchlab.kermit.Logger
import com.antcashmanager.data.local.DatabaseProvider
import com.antcashmanager.data.repository.CategoryRepositoryImpl
import com.antcashmanager.data.repository.SettingsRepositoryImpl
import com.antcashmanager.data.repository.TransactionRepositoryImpl
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.content.Context
import com.antcashmanager.android.BuildConfig

class AntCashManagerApp : Application() {

    lateinit var transactionRepository: TransactionRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var categoryRepository: CategoryRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Logger.d("AntCashManagerApp") { "Application started" }
        val database = DatabaseProvider.getDatabase(this)
        transactionRepository = TransactionRepositoryImpl(database.transactionDao())
        settingsRepository = SettingsRepositoryImpl(this)
        categoryRepository = CategoryRepositoryImpl(database.categoryDao())

        appScope.launch {
            seedDefaultCategories()
        }
    }

    private suspend fun seedDefaultCategories() {
        val count = categoryRepository.getDefaultCategoryCount()
        if (count > 0) return

        Logger.d("AntCashManagerApp") { "Seeding default categories" }

        val expenseCategories = listOf(
            Category(name = "Casa", icon = "home", color = 0xFF4FC3F7, type = "EXPENSE", isDefault = true),
            Category(name = "Trasporti", icon = "directions_car", color = 0xFF64B5F6, type = "EXPENSE", isDefault = true),
            Category(name = "Cibo", icon = "restaurant", color = 0xFFE57373, type = "EXPENSE", isDefault = true),
            Category(name = "Bollette", icon = "receipt_long", color = 0xFFFFB74D, type = "EXPENSE", isDefault = true),
            Category(name = "Pranzi/Cene fuori", icon = "local_dining", color = 0xFFF06292, type = "EXPENSE", isDefault = true),
            Category(name = "Divertimento", icon = "theater_comedy", color = 0xFFBA68C8, type = "EXPENSE", isDefault = true),
            Category(name = "Salute", icon = "local_hospital", color = 0xFF81C784, type = "EXPENSE", isDefault = true),
            Category(name = "Shopping", icon = "shopping_bag", color = 0xFFDCE775, type = "EXPENSE", isDefault = true),
            Category(name = "Istruzione", icon = "school", color = 0xFF7986CB, type = "EXPENSE", isDefault = true),
            Category(name = "Altro", icon = "more_horiz", color = 0xFF90A4AE, type = "EXPENSE", isDefault = true),
        )

        val incomeCategories = listOf(
            Category(name = "Stipendio", icon = "payments", color = 0xFF81C784, type = "INCOME", isDefault = true),
            Category(name = "Paghetta", icon = "savings", color = 0xFF4DB6AC, type = "INCOME", isDefault = true),
            Category(name = "Rimborso", icon = "currency_exchange", color = 0xFF64B5F6, type = "INCOME", isDefault = true),
            Category(name = "Investimenti", icon = "trending_up", color = 0xFFFFD54F, type = "INCOME", isDefault = true),
            Category(name = "Freelance", icon = "work", color = 0xFFA1887F, type = "INCOME", isDefault = true),
            Category(name = "Altro", icon = "more_horiz", color = 0xFF90A4AE, type = "INCOME", isDefault = true),
        )

        (expenseCategories + incomeCategories).forEach { category ->
            categoryRepository.insertCategory(category)
        }

        Logger.d("AntCashManagerApp") { "Default categories seeded successfully" }
    }
}
