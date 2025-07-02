package com.example.myapplication.mappers

import com.example.myapplication.database.entities.Category
import com.example.myapplication.dto.CategoryDto

fun CategoryDto.toEntity(): Category {
    return Category(
        id = this.id,
        title = this.title,
        iconResId = this.icon_res_id,
        isIncome = this.is_income,
        color = this.color,
    )
}

fun List<CategoryDto>.toEntityList(): List<Category> {
    return this.map { it.toEntity() }
}