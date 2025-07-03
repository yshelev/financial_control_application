package com.example.myapplication.database.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class UserTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isIncome: Boolean,
    val amount: Double,
    val currency: String,
    val description: String?,
    var date: Long = System.currentTimeMillis(),
    val cardId: Long,
    val categoryId: Long?
)