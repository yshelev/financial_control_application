package com.example.myapplication.dto

import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("is_income") val isIncome: Boolean,
    @SerializedName("amount") val amount: Double,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String?,
    @SerializedName("date") val date: String,
    @SerializedName("icon_res_id") val iconResId: Int,
    @SerializedName("card_id") val cardId: Long
)