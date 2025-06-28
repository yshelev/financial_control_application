package com.example.myapplication.schemas

data class BalanceCardUpdateSchema (
    val card_id: Long,
    val new_balance: Double
)