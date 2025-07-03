package com.example.myapplication.dataClasses

import com.example.myapplication.database.entities.Card
import com.example.myapplication.database.entities.Category
import com.example.myapplication.database.entities.UserTransaction
import kotlinx.serialization.Serializable

@Serializable
data class ExportData (
    val username: String,
    val categories: List<Category>,
    val transactions: List<UserTransaction>,
    val cards: List<Card>
)