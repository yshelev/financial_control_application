package com.example.myapplication

data class Transaction(
    val category: String,
    val date: String,
    val amount: Int,
    val isIncome: Boolean,
    val iconResId: Int
)
