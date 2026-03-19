package com.antcashmanager.android

import android.app.Application
import co.touchlab.kermit.Logger
import com.antcashmanager.data.local.DatabaseProvider
import com.antcashmanager.data.repository.CategoryRepositoryImpl
import com.antcashmanager.data.repository.SettingsRepositoryImpl
import com.antcashmanager.data.repository.TransactionRepositoryImpl
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository

class AntCashManagerApp : Application() {

    lateinit var transactionRepository: TransactionRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var categoryRepository: CategoryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        Logger.d("AntCashManagerApp") { "Application started" }
        val database = DatabaseProvider.getDatabase(this)
        transactionRepository = TransactionRepositoryImpl(database.transactionDao())
        settingsRepository = SettingsRepositoryImpl(this)
        categoryRepository = CategoryRepositoryImpl(database.categoryDao())
    }
}
