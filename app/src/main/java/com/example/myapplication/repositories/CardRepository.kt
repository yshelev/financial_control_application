package com.example.myapplication.repositories
import com.example.myapplication.database.entities.Card
import com.example.myapplication.dto.CardDto
import com.example.myapplication.network.ApiService
import com.example.myapplication.schemas.CardSchema
import kotlinx.coroutines.flow.Flow
import retrofit2.Call

class CardRepository(private val apiService: ApiService) {
    suspend fun getCards(email: String): List<CardDto> {
        return apiService.getCardsByEmail(email)
    }

    suspend fun addCard(card: CardSchema): CardDto {
        return apiService.addCard(card)
    }

    suspend fun getCard(cardId: Int): CardDto {
        return apiService.getCard(cardId)
    }

    suspend fun deleteCard(cardId: Int): Void {
        return apiService.deleteCard(cardId)
    }
}