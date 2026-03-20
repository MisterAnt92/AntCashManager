package com.antcashmanager.data.mapper
import com.antcashmanager.data.local.entity.CategoryEntity
import com.antcashmanager.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Test
class CategoryMapperTest {
    @Test
    fun `CategoryEntity toDomain maps correctly`() {
        val entity = CategoryEntity(id = 1, name = "Food", icon = "restaurant", color = 0xFFE57373)
        val domain = entity.toDomain()
        assertEquals(1L, domain.id)
        assertEquals("Food", domain.name)
        assertEquals("restaurant", domain.icon)
        assertEquals(0xFFE57373, domain.color)
    }
    @Test
    fun `Category toEntity maps correctly`() {
        val domain = Category(id = 2, name = "Transport", icon = "bus", color = 0xFF4FC3F7)
        val entity = domain.toEntity()
        assertEquals(2L, entity.id)
        assertEquals("Transport", entity.name)
        assertEquals("bus", entity.icon)
        assertEquals(0xFF4FC3F7, entity.color)
    }
    @Test
    fun `round-trip mapping preserves data`() {
        val original = Category(id = 3, name = "Entertainment", icon = "movie", color = 0xFFBA68C8)
        val roundTripped = original.toEntity().toDomain()
        assertEquals(original, roundTripped)
    }
}
