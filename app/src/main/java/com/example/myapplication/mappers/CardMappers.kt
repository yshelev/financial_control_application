package com.example.myapplication.mappers

import com.example.myapplication.database.entities.Card
import com.example.myapplication.dto.CardDto

fun CardDto.toEntity(): Card {
    return Card(
        id = this.id,
        name = this.name,
        maskedNumber = this.maskedNumber,
        date = this.date,
        currency = this.currency,
        balance = this.balance
    )
}

fun List<CardDto>.toEntityList(): List<Card> {
    return this.map { it.toEntity() }
}