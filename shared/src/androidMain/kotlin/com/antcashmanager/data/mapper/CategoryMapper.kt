package com.antcashmanager.data.mapper
import com.antcashmanager.data.local.entity.CategoryEntity
import com.antcashmanager.domain.model.Category
fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
)
fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
)
