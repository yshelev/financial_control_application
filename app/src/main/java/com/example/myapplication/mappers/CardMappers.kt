package com.example.myapplication.mappers

import com.example.myapplication.database.entities.Card
import com.example.myapplication.dto.CardDto
import java.math.BigDecimal

fun CardDto.toEntity(): Card {
    return Card(
        id = this.id,
        name = this.name,
        maskedNumber = this.maskedNumber,
        date = this.date,
        currency = this.currency,
        balance = this.balance.setScale(2, BigDecimal.ROUND_HALF_UP)
    )
}

fun List<CardDto>.toEntityList(): List<Card> {
    return this.map { it.toEntity() }
}