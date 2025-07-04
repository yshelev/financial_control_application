package com.example.myapplication.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isIncome: Boolean,
    val title: String,
    val iconResId: Int,
    val color: String,
    val code: String? = null
)
