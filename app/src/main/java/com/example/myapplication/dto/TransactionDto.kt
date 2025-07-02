package com.example.myapplication.dto

import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("is_income") val isIncome: Boolean,
    @SerializedName("amount") val amount: Double,
    @SerializedName("category_id") val categoryId: Long,
    @SerializedName("description") val description: String?,
    @SerializedName("date") val date: String,
    @SerializedName("card_id") val cardId: Long,
    @SerializedName("currency") val currency: String
)