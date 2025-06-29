package com.example.myapplication.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("registration_date") val registrationDate: String,
    @SerializedName("is_active") val isActive: Boolean
)
