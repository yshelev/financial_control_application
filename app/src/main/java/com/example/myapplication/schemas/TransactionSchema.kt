package com.example.myapplication.schemas

data class TransactionSchema (
    val is_income: Boolean,
    val amount: Double,
    val currency: String?,
    val icon_res_id: Int,
    val description: String?,
    val card_id: Long
)