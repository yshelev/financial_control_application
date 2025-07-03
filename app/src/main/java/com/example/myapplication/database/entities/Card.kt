package com.example.myapplication.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.serializers.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val maskedNumber: String,
    val date: String,
    val currency: String,
    @Serializable(with = BigDecimalSerializer::class)
    val balance: BigDecimal
)
