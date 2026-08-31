package com.antcashmanager.android.di

import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.tracker.EngagementTracker
import com.antcashmanager.android.analytics.tracker.ErrorTracker
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.analytics.tracker.SegmentationTracker
import com.antcashmanager.android.analytics.tracker.SessionTracker
import com.antcashmanager.android.auth.GoogleSignInManager
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.data.receipt.MlKitReceiptOcrService
import com.antcashmanager.android.drive.DriveUploadManager
import com.antcashmanager.android.ui.widget.GlanceWidgetUpdateNotifier
import com.antcashmanager.android.work.AutoBackupScheduler
import com.antcashmanager.data.local.DatabaseProvider
import com.antcashmanager.data.repository.CategoryRepositoryImpl
import com.antcashmanager.data.repository.SettingsRepositoryImpl
import com.antcashmanager.data.repository.TransactionRepositoryImpl
import com.antcashmanager.data.security.LocalDataCipherImpl
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.security.LocalDataCipher
import com.antcashmanager.domain.service.ReceiptOcrService
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import com.antcashmanager.domain.validation.TransactionValidator
import com.antcashmanager.domain.validation.TransactionValidatorImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Modulo Koin per il Data Layer:
 * - Database Room
 * - LocalDataCipher
 * - Repository (TransactionRepository, SettingsRepository, CategoryRepository)
 * - AnalyticsManager e Tracker
 * - Servizi (BackupService, MlKitReceiptOcrService)
 * - Domain Validation (TransactionValidator)
 */
val repositoryModule = module {
    single { DatabaseProvider.getDatabase(androidApplication()) }
    single<LocalDataCipher> { LocalDataCipherImpl(androidApplication()) }
    single<WidgetUpdateNotifier> { GlanceWidgetUpdateNotifier(androidApplication()) }
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            transactionDao = get<com.antcashmanager.data.local.AppDatabase>().transactionDao(),
            localDataCipher = get(),
            widgetUpdateNotifier = get(),
        )
    }
    single<SettingsRepository> { SettingsRepositoryImpl(androidApplication()) }
    single { AutoBackupScheduler(androidApplication()) }
    single { GoogleSignInManager(androidApplication(), get<SettingsRepository>()) }
    single { DriveUploadManager(androidApplication(), get<GoogleSignInManager>(), get<SettingsRepository>()) }
    single<CategoryRepository> {
        CategoryRepositoryImpl(
            categoryDao = get<com.antcashmanager.data.local.AppDatabase>().categoryDao(),
        )
    }
    single { AnalyticsManager(androidApplication()) }

    // PHASE 2: Performance & Session & Error Tracking
    single { PerformanceTracker(get<AnalyticsManager>()) }
    single { SessionTracker(get<AnalyticsManager>()) }
    single { ErrorTracker(get<AnalyticsManager>()) }

    // PHASE 3: Advanced Analytics (Segmentation & Engagement)
    single { SegmentationTracker(get<AnalyticsManager>()) }
    single { EngagementTracker(get<AnalyticsManager>()) }

    single {
        BackupService(
            transactionRepository = get(),
            categoryRepository = get(),
            settingsRepository = get(),
            widgetUpdateNotifier = get(),
        )
    }
    factory<ReceiptOcrService> { MlKitReceiptOcrService() }

    // Domain validation for transactions
    single<TransactionValidator> { TransactionValidatorImpl() }
}

