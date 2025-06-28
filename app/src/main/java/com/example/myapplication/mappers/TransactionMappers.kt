package com.example.myapplication.mappers

import com.example.myapplication.database.entities.UserTransaction
import com.example.myapplication.dto.TransactionDto
import java.text.SimpleDateFormat
import java.util.Locale

fun TransactionDto.toEntity(): UserTransaction {
    return UserTransaction(
        id = this.id,
        isIncome = this.isIncome,
        amount = this.amount,
        category = this.category,
        description = this.description,
        date = this.date.toTimestamp(),
        iconResId = this.iconResId,
        currency = this.currency,
        cardId = this.cardId
    )
}

fun List<TransactionDto>.toEntityList(): List<UserTransaction> {
    return this.map { it.toEntity() }
}

private fun String.toTimestamp(): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.parse(this)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}