package com.example.myapplication.schemas

import java.math.BigDecimal

data class CardSchema (
    val name: String,
    val balance: BigDecimal,
    val masked_number: String,
    val date: String,
    val currency: String,
    val owner_id: Long
)