package com.antcashmanager.android

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.antcashmanager.android.di.appModules
import com.antcashmanager.android.work.AutoBackupNotifier
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.security.LocalDataCipher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AntCashManagerApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // In release: suppress Verbose/Debug logs to avoid leaking internal state
        // to logcat and to let R8 dead-code-eliminate the unused log lambdas.
        if (!BuildConfig.DEBUG) Logger.setMinSeverity(Severity.Warn)
        Logger.d(tag = "AntCashManagerApp") { "Application started" }

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AntCashManagerApp)
            modules(appModules)
        }

        // Crea il notification channel per i fallimenti del backup automatico
        AutoBackupNotifier.ensureChannel(this)

        appScope.launch {
            seedDefaultCategories()
            ensureCategorySortOrderInitialized()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            get<LocalDataCipher>().clearCache()
        }
    }

    private suspend fun seedDefaultCategories() {
        val categoryRepository: CategoryRepository = get()
        val count = categoryRepository.getDefaultCategoryCount()

        if (count > 0) {
            // App già inizializzata in passato: non risemina tutto, ma retro-inserisce solo le
            // categorie di default aggiunte in versioni successive dell'app, così chi ha già
            // dati esistenti le riceve comunque con un aggiornamento.
            backfillNewDefaultCategories(categoryRepository)
            return
        }

        Logger.d(tag = "AntCashManagerApp") { "Seeding default categories" }

        val expenseCategories =
            (
                listOf(
                    Category(
                        name = "Non categorizzato",
                        icon = "more_horiz",
                        color = 0xFF90A4AE,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Casa",
                        icon = "home",
                        color = 0xFF4FC3F7,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Trasporti",
                        icon = "directions_car",
                        color = 0xFF64B5F6,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Cibo",
                        icon = "restaurant",
                        color = 0xFFE57373,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Bollette",
                        icon = "receipt_long",
                        color = 0xFFFFB74D,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Pranzi/Cene fuori",
                        icon = "local_dining",
                        color = 0xFFF06292,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Divertimento",
                        icon = "theater_comedy",
                        color = 0xFFBA68C8,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Salute",
                        icon = "local_hospital",
                        color = 0xFF81C784,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Shopping",
                        icon = "shopping_bag",
                        color = 0xFFDCE775,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Istruzione",
                        icon = "school",
                        color = 0xFF7986CB,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                    Category(
                        name = "Altro",
                        icon = "more_horiz",
                        color = 0xFF90A4AE,
                        type = "EXPENSE",
                        isDefault = true,
                    ),
                ) + newDefaultExpenseCategories
            ).mapIndexed { index, category -> category.copy(sortOrder = index) }

        val incomeCategories =
            (
                listOf(
                    Category(
                        name = "Non categorizzato",
                        icon = "more_horiz",
                        color = 0xFF90A4AE,
                        type = "INCOME",
                        isDefault = true,
                    ),
                    Category(
                        name = "Stipendio",
                        icon = "payments",
                        color = 0xFF81C784,
                        type = "INCOME",
                        isDefault = true,
                    ),
                    Category(
                        name = "Paghetta",
                        icon = "savings",
                        color = 0xFF4DB6AC,
                        type = "INCOME",
                        isDefault = true,
                    ),
                    Category(
                        name = "Rimborso",
                        icon = "currency_exchange",
                        color = 0xFF64B5F6,
                        type = "INCOME",
                        isDefault = true,
                    ),
                    Category(
                        name = "Investimenti",
                        icon = "trending_up",
                        color = 0xFFFFD54F,
                        type = "INCOME",
                        isDefault = true,
                    ),
                    Category(
                        name = "Freelance",
                        icon = "work",
                        color = 0xFFA1887F,
                        type = "INCOME",
                        isDefault = true,
                    ),
                    Category(
                        name = "Altro",
                        icon = "more_horiz",
                        color = 0xFF90A4AE,
                        type = "INCOME",
                        isDefault = true,
                    ),
                ) + newDefaultIncomeCategories
            ).mapIndexed { index, category -> category.copy(sortOrder = index) }

        (expenseCategories + incomeCategories).forEach { category ->
            categoryRepository.insertCategory(category)
        }

        // Seed fresco: l'ordine è già assegnato in modo coerente sopra, il backfill
        // one-shot (per chi aggiorna da una versione precedente) non deve rieseguire e
        // sovrascriverlo con l'ordine alfabetico.
        val settingsRepository: SettingsRepository = get()
        settingsRepository.setCategorySortOrderInitialized(true)

        Logger.d(tag = "AntCashManagerApp") { "Default categories seeded successfully" }
    }

    /**
     * Backfill one-shot: assegna `sortOrder` in base all'ordine alfabetico per tipo alle
     * categorie già esistenti su un device che aggiorna da una versione precedente dell'app,
     * prima dell'introduzione del riordino manuale. Senza questo passaggio tutte le categorie
     * erediterebbero `sortOrder = 0` dalla migration Room, rendendo l'ordine visualizzato
     * instabile/arbitrario invece di preservare quello alfabetico già visto dall'utente.
     */
    private suspend fun ensureCategorySortOrderInitialized() {
        val settingsRepository: SettingsRepository = get()
        if (settingsRepository.getCategorySortOrderInitialized().first()) return

        val categoryRepository: CategoryRepository = get()
        categoryRepository
            .getAllCategories()
            .first()
            .groupBy { it.type }
            .values
            .forEach { categoriesOfType ->
                categoriesOfType.sortedBy { it.name }.forEachIndexed { index, category ->
                    if (category.sortOrder != index) {
                        categoryRepository.updateCategory(category.copy(sortOrder = index))
                    }
                }
            }

        settingsRepository.setCategorySortOrderInitialized(true)
        Logger.d(tag = "AntCashManagerApp") { "Category sort order backfilled" }
    }

    /**
     * Categorie di default introdotte dopo il seed iniziale dell'app. Inserite in blocco al
     * primo avvio in assoluto (vedi [seedDefaultCategories]) e retro-inserite singolarmente per
     * chi ha già categorie esistenti (vedi [backfillNewDefaultCategories]).
     */
    private val newDefaultExpenseCategories =
        listOf(
            Category(
                name = "Regali",
                icon = "redeem",
                color = 0xFFFF8A65,
                type = "EXPENSE",
                isDefault = true,
            ),
            Category(
                name = "Abbonamenti",
                icon = "subscriptions",
                color = 0xFFFFD54F,
                type = "EXPENSE",
                isDefault = true,
            ),
            Category(
                name = "Cura personale",
                icon = "spa",
                color = 0xFFA1887F,
                type = "EXPENSE",
                isDefault = true,
            ),
        )

    private val newDefaultIncomeCategories =
        listOf(
            Category(
                name = "Regalo",
                icon = "redeem",
                color = 0xFFFF8A65,
                type = "INCOME",
                isDefault = true,
            ),
            Category(
                name = "Buoni pasto",
                icon = "card_giftcard",
                color = 0xFF66BB6A,
                type = "INCOME",
                isDefault = true,
            ),
        )

    private suspend fun backfillNewDefaultCategories(categoryRepository: CategoryRepository) {
        newDefaultExpenseCategories.forEach { category ->
            if (categoryRepository.getCategoryByName(category.name) == null) {
                Logger.d(tag = "AntCashManagerApp") { "Backfilling new default category: ${category.name}" }
                categoryRepository.insertCategory(category)
            }
        }
        newDefaultIncomeCategories.forEach { category ->
            if (categoryRepository.getCategoryByName(category.name) == null) {
                Logger.d(tag = "AntCashManagerApp") { "Backfilling new default category: ${category.name}" }
                categoryRepository.insertCategory(category)
            }
        }
    }
}
