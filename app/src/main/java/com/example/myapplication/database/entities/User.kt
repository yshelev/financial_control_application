package com.example.myapplication.database.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val registrationDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)