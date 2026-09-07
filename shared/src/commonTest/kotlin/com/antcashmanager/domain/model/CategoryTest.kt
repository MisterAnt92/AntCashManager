package com.antcashmanager.domain.model

import com.antcashmanager.testutil.testCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for Category domain model.
 *
 * Tests cover:
 * - Category creation and properties
 * - Category type (INCOME/EXPENSE)
 * - Color and icon storage
 * - Default category flag
 * - Hidden state
 * - Sort order
 */
class CategoryTest {
    @Test
    fun category_shouldStoreBasicProperties() {
        val category =
            testCategory {
                name = "Food"
                icon = "🍔"
                color = 0xFFFF6B6B
            }

        assertEquals("Food", category.name)
        assertEquals("🍔", category.icon)
        assertEquals(0xFFFF6B6B, category.color)
    }

    @Test
    fun category_shouldHandleExpenseType() {
        val category =
            testCategory {
                name = "Transport"
                type = "EXPENSE"
            }

        assertEquals("Transport", category.name)
        assertEquals("EXPENSE", category.type)
    }

    @Test
    fun category_shouldHandleIncomeType() {
        val category =
            testCategory {
                name = "Salary"
                type = "INCOME"
            }

        assertEquals("Salary", category.name)
        assertEquals("INCOME", category.type)
    }

    @Test
    fun category_shouldHandleDefaultFlag() {
        val defaultCategory =
            testCategory {
                name = "Food"
                isDefault = true
            }
        assertTrue(defaultCategory.isDefault)

        val customCategory =
            testCategory {
                name = "Custom"
                isDefault = false
            }
        assertFalse(customCategory.isDefault)
    }

    @Test
    fun category_shouldHandleHiddenState() {
        val visibleCategory =
            testCategory {
                name = "Food"
                isHidden = false
            }
        assertFalse(visibleCategory.isHidden)

        val hiddenCategory =
            testCategory {
                name = "Archived"
                isHidden = true
            }
        assertTrue(hiddenCategory.isHidden)
    }

    @Test
    fun category_shouldMaintainSortOrder() {
        val categories =
            listOf(
                testCategory {
                    id = 1L
                    name = "Food"
                    sortOrder = 1
                },
                testCategory {
                    id = 2L
                    name = "Transport"
                    sortOrder = 2
                },
                testCategory {
                    id = 3L
                    name = "Entertainment"
                    sortOrder = 3
                },
            )

        assertEquals(1, categories[0].sortOrder)
        assertEquals(2, categories[1].sortOrder)
        assertEquals(3, categories[2].sortOrder)
    }

    @Test
    fun category_shouldHaveUniqueId() {
        val cat1 =
            testCategory {
                id = 1L
                name = "Food"
            }
        val cat2 =
            testCategory {
                id = 2L
                name = "Transport"
            }

        assertEquals(1L, cat1.id)
        assertEquals(2L, cat2.id)
    }

    @Test
    fun category_shouldHandleColorRange() {
        val colors =
            listOf(
                0xFFFF6B6B, // Red
                0xFF51CF66, // Green
                0xFF4DABF7, // Blue
                0xFFFFD93D, // Yellow
            )

        for ((index, colorValue) in colors.withIndex()) {
            val category =
                testCategory {
                    color = colorValue
                }
            assertEquals(colorValue, category.color)
        }
    }

    @Test
    fun category_shouldStoreEmojiIcon() {
        val emojiIcons = listOf("🍔", "🚗", "🎬", "💼", "🏠")

        for (emoji in emojiIcons) {
            val category =
                testCategory {
                    icon = emoji
                }
            assertEquals(emoji, category.icon)
        }
    }

    @Test
    fun category_equalityTest() {
        val cat1 =
            testCategory {
                id = 1L
                name = "Food"
            }

        val cat2 =
            testCategory {
                id = 1L
                name = "Food"
            }

        assertEquals(cat1, cat2)
    }

    @Test
    fun category_inequalityByIdTest() {
        val cat1 =
            testCategory {
                id = 1L
                name = "Food"
            }

        val cat2 =
            testCategory {
                id = 2L
                name = "Food"
            }

        assertFalse(cat1 == cat2)
    }

    @Test
    fun category_inequalityByNameTest() {
        val cat1 =
            testCategory {
                id = 1L
                name = "Food"
            }

        val cat2 =
            testCategory {
                id = 1L
                name = "Transport"
            }

        assertFalse(cat1 == cat2)
    }
}
