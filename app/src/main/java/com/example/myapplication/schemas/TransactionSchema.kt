package com.example.myapplication.schemas

import java.math.BigDecimal

data class TransactionSchema(
    val is_income: Boolean,
    val amount: BigDecimal,
    val currency: String,
    val description: String?,
    val card_id: Long,
    val category_id: Long,
    val date: Long
)