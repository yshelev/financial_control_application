package com.example.myapplication.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "cards")
data class Card(
    @PrimaryKey() val id: Long = 0,
    val name: String,
    val maskedNumber: String,
    val date: String,
    val currency: String,
    val balance: Double
)
