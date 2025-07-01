package com.example.myapplication.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isIncome: Boolean,
    val name: String,
    val iconResId: Int,
    val color: String
)
