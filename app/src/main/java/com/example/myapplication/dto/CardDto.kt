package com.example.myapplication.dto

import com.google.gson.annotations.SerializedName

data class CardDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("masked_number") val maskedNumber: String,
    @SerializedName("date") val date: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("balance") val balance: Double
)