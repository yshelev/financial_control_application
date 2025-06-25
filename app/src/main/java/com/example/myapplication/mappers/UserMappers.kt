package com.example.myapplication.mappers

import com.example.myapplication.database.entities.User
import com.example.myapplication.dto.UserDto
import java.text.SimpleDateFormat
import java.util.Locale

fun UserDto.toEntity(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        password = "",
        registrationDate = this.registrationDate.toTimestamp(),
        isActive = this.isActive
    )
}

fun List<UserDto>.toEntityList(): List<User> {
    return this.map { it.toEntity() }
}

private fun String.toTimestamp(): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
        sdf.parse(this)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}