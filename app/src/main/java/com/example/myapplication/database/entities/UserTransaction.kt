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
        )
    ]
)
data class UserTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isIncome: Boolean,
    val amount: Double,
    val currency: String,
    val category: String,
    val description: String?,
    val date: Long = System.currentTimeMillis(),
    val iconResId: Int,
    val cardId: Long  // Добавленное поле для связи с картой
)