package com.antcashmanager.data.mapper

import com.antcashmanager.data.local.entity.CategoryEntity
import com.antcashmanager.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryMapperTest {
    @Test
    fun categoryEntityToDomainMapsCorrectly() {
        val entity = CategoryEntity(id = 1, name = "Food", icon = "restaurant", color = 0xFFE57373)
        val domain = entity.toDomain()
        assertEquals(1L, domain.id)
        assertEquals("Food", domain.name)
        assertEquals("restaurant", domain.icon)
        assertEquals(0xFFE57373, domain.color)
    }

    @Test
    fun categoryToEntityMapsCorrectly() {
        val domain = Category(id = 2, name = "Transport", icon = "bus", color = 0xFF4FC3F7)
        val entity = domain.toEntity()
        assertEquals(2L, entity.id)
        assertEquals("Transport", entity.name)
        assertEquals("bus", entity.icon)
        assertEquals(0xFF4FC3F7, entity.color)
    }

    @Test
    fun roundTripMappingPreservesData() {
        val original = Category(id = 3, name = "Entertainment", icon = "movie", color = 0xFFBA68C8)
        val roundTripped = original.toEntity().toDomain()
        assertEquals(original, roundTripped)
    }

    @Test
    fun categoryEntityToDomain_shouldMapIsDefault_whenFieldIsSet() {
        val entity = CategoryEntity(
            id = 4,
            name = "Income",
            icon = "attach_money",
            color = 0xFF66BB6A,
            isDefault = true,
        )
        val domain = entity.toDomain()
        assertEquals(true, domain.isDefault)
    }

    @Test
    fun categoryToDomain_shouldMapSortOrder_whenFieldIsSet() {
        val entity = CategoryEntity(
            id = 5,
            name = "Savings",
            icon = "savings",
            color = 0xFF29B6F6,
            sortOrder = 5,
        )
        val domain = entity.toDomain()
        assertEquals(5, domain.sortOrder)
    }

    @Test
    fun categoryToDomain_shouldMapIsHidden_whenFieldIsSet() {
        val entity = CategoryEntity(
            id = 6,
            name = "Archived",
            icon = "archive",
            color = 0xFF90A4AE,
            isHidden = true,
        )
        val domain = entity.toDomain()
        assertEquals(true, domain.isHidden)
    }

    @Test
    fun categoryToDomain_shouldMapType_whenFieldIsSet() {
        val entity = CategoryEntity(
            id = 7,
            name = "Custom",
            icon = "star",
            color = 0xFFFFB300,
            type = "INCOME",
        )
        val domain = entity.toDomain()
        assertEquals("INCOME", domain.type)
    }

    @Test
    fun categoryToEntity_shouldMapIsDefault_whenFieldIsTrue() {
        val domain = Category(
            id = 8,
            name = "Default Expense",
            icon = "category",
            color = 0xFFF44336,
            isDefault = true,
        )
        val entity = domain.toEntity()
        assertEquals(true, entity.isDefault)
    }

    @Test
    fun categoryToEntity_shouldMapSortOrder_whenFieldIsSet() {
        val domain = Category(
            id = 9,
            name = "Priority Category",
            icon = "priority_high",
            color = 0xFFF57C00,
            sortOrder = 3,
        )
        val entity = domain.toEntity()
        assertEquals(3, entity.sortOrder)
    }

    @Test
    fun categoryToEntity_shouldMapIsHidden_whenFieldIsTrue() {
        val domain = Category(
            id = 10,
            name = "Hidden Category",
            icon = "visibility_off",
            color = 0xFF9E9E9E,
            isHidden = true,
        )
        val entity = domain.toEntity()
        assertEquals(true, entity.isHidden)
    }

    @Test
    fun roundTripMapping_shouldPreserveAllFields_whenCategoryHasComplexData() {
        val original = Category(
            id = 11,
            name = "Complex",
            icon = "complex_icon",
            color = 0xFF7E57C2,
            type = "EXPENSE",
            isDefault = true,
            sortOrder = 7,
            isHidden = false,
        )
        val roundTripped = original.toEntity().toDomain()
        assertEquals(original, roundTripped)
    }

    @Test
    fun roundTripMapping_shouldPreserveAllFields_whenCategoryIsHiddenAndNotDefault() {
        val original = Category(
            id = 12,
            name = "Hidden Non-Default",
            icon = "unknown",
            color = 0xFFC0CA33,
            type = "INCOME",
            isDefault = false,
            sortOrder = 99,
            isHidden = true,
        )
        val roundTripped = original.toEntity().toDomain()
        assertEquals(original, roundTripped)
    }

    @Test
    fun entityToDomain_shouldHandleDefaultValues_whenFieldsAreNotExplicitlySet() {
        val entity = CategoryEntity(
            id = 13,
            name = "Minimal",
            icon = "default_icon",
            color = 0xFF000000,
        )
        val domain = entity.toDomain()
        assertEquals(false, domain.isDefault)
        assertEquals(0, domain.sortOrder)
        assertEquals(false, domain.isHidden)
    }

    @Test
    fun categoryToEntity_shouldMapAllFieldsCorrectly_whenAllFieldsHaveUniqueValues() {
        val domain = Category(
            id = 14,
            name = "Complete Data",
            icon = "complete_icon",
            color = 0xFFE91E63,
            type = "EXPENSE",
            isDefault = false,
            sortOrder = 42,
            isHidden = false,
        )

        val entity = domain.toEntity()

        assertEquals(14L, entity.id)
        assertEquals("Complete Data", entity.name)
        assertEquals("complete_icon", entity.icon)
        assertEquals(0xFFE91E63, entity.color)
        assertEquals("EXPENSE", entity.type)
        assertEquals(false, entity.isDefault)
        assertEquals(42, entity.sortOrder)
        assertEquals(false, entity.isHidden)
    }
}
