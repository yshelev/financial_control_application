package com.example.myapplication.schemas

data class CardSchema (
    val name: String,
    val balance: Double,
    val masked_number: String,
    val date: String,
    val currency: String,
    val owner_email: String
)