package com.example.myapplication.schemas

data class TransactionSchema(
    val is_income: Boolean,
    val amount: Double,
    val currency: String,
    val description: String?,
    val card_id: Long,
    val category_id: Long
)