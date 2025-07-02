package com.example.myapplication.schemas

data class CreateCategorySchema (
    val is_income: Boolean,
    val title: String,
    val user_id: Long,
    val icon_res_id: Int,
    val color: String
)