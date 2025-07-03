package com.example.myapplication.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal


@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val maskedNumber: String,
    val date: String,
    val currency: String,
    val balance: BigDecimal
)
