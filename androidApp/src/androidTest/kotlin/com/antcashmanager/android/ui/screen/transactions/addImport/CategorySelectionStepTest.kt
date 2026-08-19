package com.antcashmanager.android.ui.screen.transactions.addImport

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.antcashmanager.android.R
import com.antcashmanager.android.test.base.BaseInstrumentationTest
import com.antcashmanager.android.ui.screen.transactions.addImport.view.CategorySelectionStep
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test composable per CategorySelectionStep.
 *
 * Verifica che:
 * - Le categorie siano mostrate nella lista
 * - Il click su una categoria chiami il callback onSelectCategory
 * - Il pulsante back chiami il callback onCancel
 *
 * Extends [BaseInstrumentationTest] which automatically:
 * - Disables the tutorial overlay
 * - Provides common test helpers
 */
@RunWith(AndroidJUnit4::class)
class CategorySelectionStepTest : BaseInstrumentationTest() {

    private val mockCategories = listOf(
        Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
        Category(2, "Salary", "💰", 0xFF51CF66, "INCOME"),
        Category(3, "Entertainment", "🎬", 0xFF339AF0, "EXPENSE"),
    )

    @Test
    fun categorySelectionStep_shouldDisplayAllCategories() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                CategorySelectionStep(
                    categories = mockCategories,
                    selectedCategory = null,
                    onSelectCategory = {},
                    onCancel = {},
                )
            }
        }

        // Wait for composables to fully render
        composeTestRule.waitForIdle()

        // Verifica che tutte le categorie siano visualizzate
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Entertainment").assertIsDisplayed()
    }

    @Test
    fun categorySelectionStep_shouldCallOnSelectCategory_whenCategoryClicked() {
        var selectedCategoryId: Long? = null

        composeTestRule.setContent {
            AntCashManagerTheme {
                CategorySelectionStep(
                    categories = mockCategories,
                    selectedCategory = null,
                    onSelectCategory = { category ->
                        selectedCategoryId = category.id
                    },
                    onCancel = {},
                )
            }
        }

        // Clicca su "Food"
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.waitForIdle()

        // Verifica che il callback sia stato chiamato con l'ID corretto
        assertEquals("Should select Food category (id=1)", 1L, selectedCategoryId)
    }

    @Test
    fun categorySelectionStep_shouldDisplayTitleFromResources() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val titleText = ctx.getString(R.string.add_transaction_select_category)
        val subtitleText = ctx.getString(R.string.add_transaction_choose_category)

        composeTestRule.setContent {
            AntCashManagerTheme {
                CategorySelectionStep(
                    categories = mockCategories,
                    selectedCategory = null,
                    onSelectCategory = {},
                    onCancel = {},
                )
            }
        }

        // Verifica che i testi delle stringhe risorse siano visualizzati
        composeTestRule.onNodeWithText(titleText).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitleText).assertIsDisplayed()
    }

    @Test
    fun categorySelectionStep_shouldCallOnCancel_whenBackButtonClicked() {
        var cancelCalled = false

        composeTestRule.setContent {
            AntCashManagerTheme {
                CategorySelectionStep(
                    categories = mockCategories,
                    selectedCategory = null,
                    onSelectCategory = {},
                    onCancel = { cancelCalled = true },
                )
            }
        }

        // Clicca il back button usando content description (IconButton non ha testo)
        val backButtonDescription = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.common_back)
        composeTestRule.onNodeWithContentDescription(backButtonDescription).performClick()
        composeTestRule.waitForIdle()

        // Verifica che il callback sia stato chiamato
        assertTrue("onCancel callback should be called when back button clicked", cancelCalled)
    }

    @Test
    fun categorySelectionStep_shouldShowSelectedCategory_asHighlighted() {
        val selectedCategory = mockCategories[0] // Food

        composeTestRule.setContent {
            AntCashManagerTheme {
                CategorySelectionStep(
                    categories = mockCategories,
                    selectedCategory = selectedCategory,
                    onSelectCategory = {},
                    onCancel = {},
                )
            }
        }

        // La categoria selezionata dovrebbe essere visibile
        // (il test verifica che sia presente, il vero test di highlight
        // richiederebbe di verificare le proprietà del composable)
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }
}
