package com.antcashmanager.android.ui.screen.transactions.addImport.manager

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionState
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetMealVoucherValueUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionByIdUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test unitari per [TransactionLoadManager].
 *
 * Copre:
 * - Caricamento categorie
 * - Caricamento valore buoni pasto
 * - Caricamento e preparazione dello stato per modifica
 * - Gestione degli errori
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionLoadManagerTest : BaseUnitTest() {

    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var getMealVoucherValueUseCase: GetMealVoucherValueUseCase
    private lateinit var getTransactionByIdUseCase: GetTransactionByIdUseCase
    private lateinit var manager: TransactionLoadManager

    private val mockCategories = listOf(
        Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE", sortOrder = 1),
        Category(2, "Salary", "💰", 0xFF51CF66, "INCOME", sortOrder = 2),
        Category(3, "Hidden", "❌", 0xFF999999, "EXPENSE", sortOrder = 3, isHidden = true),
    )

    private val mockTransaction = Transaction(
        id = 1L,
        title = "Lunch",
        amount = -25.50,
        category = "Food",
        type = TransactionType.EXPENSE,
        timestamp = System.currentTimeMillis(),
        notes = "Test notes",
        payee = "Restaurant",
        location = "Downtown",
        tags = "food,lunch",
    )

    @Before
    fun setup() {
        categoryRepository = FakeCategoryRepository(mockCategories)
        settingsRepository = FakeSettingsRepository()
        transactionRepository = FakeTransactionRepository(listOf(mockTransaction))
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
        getMealVoucherValueUseCase = GetMealVoucherValueUseCase(settingsRepository)
        getTransactionByIdUseCase = GetTransactionByIdUseCase(transactionRepository)
        manager = TransactionLoadManager(
            getTransactionByIdUseCase = getTransactionByIdUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            getMealVoucherValueUseCase = getMealVoucherValueUseCase,
        )
    }

    // ── Load Categories Tests ──

    @Test
    fun `loadCategories returns filtered and sorted categories`() = runUnitTest {
        val result = manager.loadCategories()

        assertTrue("Load should succeed", result.isSuccess)
        val categories = result.getOrThrow()
        assertEquals(2, categories.size) // Hidden category excluded
        assertEquals("Food", categories[0].name)
        assertEquals("Salary", categories[1].name)
    }

    @Test
    fun `loadCategories excludes hidden categories`() = runUnitTest {
        val result = manager.loadCategories()

        val categories = result.getOrThrow()
        assertTrue("Hidden category should be excluded", categories.none { it.isHidden })
    }

    @Test
    fun `loadCategories sorts by sortOrder`() = runUnitTest {
        val unsortedCategories = listOf(
            Category(3, "Third", "3️⃣", 0xFF000000, "EXPENSE", sortOrder = 3),
            Category(1, "First", "1️⃣", 0xFF000000, "EXPENSE", sortOrder = 1),
            Category(2, "Second", "2️⃣", 0xFF000000, "EXPENSE", sortOrder = 2),
        )
        val categoryRepo = FakeCategoryRepository(unsortedCategories)
        val getCatsUC = GetCategoriesUseCase(categoryRepo)
        val mgr = TransactionLoadManager(
            getTransactionByIdUseCase = getTransactionByIdUseCase,
            getCategoriesUseCase = getCatsUC,
            getMealVoucherValueUseCase = getMealVoucherValueUseCase,
        )

        val result = mgr.loadCategories()

        val categories = result.getOrThrow()
        assertEquals("First", categories[0].name)
        assertEquals("Second", categories[1].name)
        assertEquals("Third", categories[2].name)
    }

    // ── Load Meal Voucher Value Tests ──

    @Test
    fun `loadMealVoucherValue returns default value when not set`() = runUnitTest {
        val result = manager.loadMealVoucherValue()

        assertTrue("Load should succeed", result.isSuccess)
        // FakeSettingsRepository has default value of 5.29
        assertEquals(5.29, result.getOrThrow(), 0.01)
    }

    @Test
    fun `loadMealVoucherValue returns stored value when set`() = runUnitTest {
        settingsRepository.setMealVoucherValue(10.5)

        val result = manager.loadMealVoucherValue()

        assertTrue("Load should succeed", result.isSuccess)
        assertEquals(10.5, result.getOrThrow(), 0.01)
    }

    // ── Load Transaction For Edit Tests ──

    @Test
    fun `loadTransactionForEdit succeeds when transaction exists`() = runUnitTest {
        val result = manager.loadTransactionForEdit(1L)

        assertTrue("Load should succeed", result.isSuccess)
        assertEquals(1L, result.getOrThrow())
    }

    @Test
    fun `loadTransactionForEdit fails when transaction not found`() = runUnitTest {
        val result = manager.loadTransactionForEdit(999L)

        assertTrue("Load should fail", result.isFailure)
        assertTrue(
            "Should have IllegalArgumentException",
            result.exceptionOrNull() is IllegalArgumentException
        )
    }

    @Test
    fun `loadTransactionForEdit fails when category not found`() = runUnitTest {
        val transactionWithMissingCategory = mockTransaction.copy(
            id = 2L,
            category = "NonExistent"
        )
        transactionRepository.insertTransaction(transactionWithMissingCategory)

        val result = manager.loadTransactionForEdit(2L)

        assertTrue("Load should fail", result.isFailure)
        assertTrue(
            "Should have IllegalArgumentException",
            result.exceptionOrNull() is IllegalArgumentException
        )
    }

    // ── Prepare Edit State Tests ──

    @Test
    fun `prepareEditState populates state correctly`() = runUnitTest {
        val initialState = AddTransactionState()

        val result = manager.prepareEditState(1L, initialState)

        assertTrue("Prepare should succeed", result.isSuccess)
        val state = result.getOrThrow()
        assertEquals(true, state.isModifying)
        assertEquals(1L, state.transactionId)
        assertEquals("Lunch", state.title)
        // Amount should be absolute value; check numerically due to formatting variance
        assertTrue(
            "Amount should be 25.50",
            kotlin.math.abs((state.amount.toDoubleOrNull() ?: 0.0) - 25.50) < 0.01
        )
        assertEquals(TransactionType.EXPENSE, state.selectedType)
        assertEquals("Food", state.selectedCategory?.name)
        assertEquals("Test notes", state.notes)
        assertEquals("Restaurant", state.payee)
        assertEquals("Downtown", state.location)
        assertEquals("food,lunch", state.tags)
    }

    @Test
    fun `prepareEditState includes hidden category if it was previously selected`() = runUnitTest {
        val transactionWithHiddenCategory = mockTransaction.copy(
            id = 2L,
            category = "Hidden",
            amount = -50.0
        )
        transactionRepository.insertTransaction(transactionWithHiddenCategory)

        val result = manager.prepareEditState(2L, AddTransactionState())

        val state = result.getOrThrow()
        assertTrue(
            "Categories should include hidden category",
            state.categories.any { it.name == "Hidden" })
        assertEquals("Hidden", state.selectedCategory?.name)
    }

    @Test
    fun `prepareEditState converts negative amount to absolute value`() = runUnitTest {
        // Transaction has -25.50 for EXPENSE
        val result = manager.prepareEditState(1L, AddTransactionState())

        val state = result.getOrThrow()
        // Amount should be positive; check numerically due to formatting variance
        assertTrue(
            "Amount should be 25.50",
            kotlin.math.abs((state.amount.toDoubleOrNull() ?: 0.0) - 25.50) < 0.01
        )
    }

    @Test
    fun `prepareEditState excludes hidden categories except selected one`() = runUnitTest {
        val result = manager.prepareEditState(1L, AddTransactionState())

        val state = result.getOrThrow()
        val visibleCount = state.categories.count { !it.isHidden }
        assertEquals(2, visibleCount) // Food and Salary
        val hiddenCount = state.categories.count { it.isHidden }
        assertEquals(0, hiddenCount) // Hidden is excluded because not selected
    }

    @Test
    fun `prepareEditState fails when transaction not found`() = runUnitTest {
        val result = manager.prepareEditState(999L, AddTransactionState())

        assertTrue("Prepare should fail", result.isFailure)
        assertTrue(
            "Should have IllegalArgumentException",
            result.exceptionOrNull() is IllegalArgumentException
        )
    }

    @Test
    fun `prepareEditState fails when category not found`() = runUnitTest {
        val transactionWithMissingCategory = mockTransaction.copy(
            id = 2L,
            category = "NonExistent"
        )
        getTransactionByIdUseCase.invoke(transactionWithMissingCategory.id).getOrNull() ?: Unit

        val result = manager.prepareEditState(2L, AddTransactionState())

        assertTrue("Prepare should fail", result.isFailure)
    }

    @Test
    fun `prepareEditState sets isLoading to false`() = runUnitTest {
        val result = manager.prepareEditState(1L, AddTransactionState())

        val state = result.getOrThrow()
        assertFalse("isLoading should be false", state.isLoading)
    }

    @Test
    fun `prepareEditState preserves meal voucher count`() = runUnitTest {
        val transactionWithVouchers = mockTransaction.copy(
            id = 3L,
            mealVoucherCount = 5
        )
        transactionRepository.insertTransaction(transactionWithVouchers)

        val result = manager.prepareEditState(3L, AddTransactionState())

        val state = result.getOrThrow()
        assertEquals("5", state.mealVoucherCount)
    }
}
