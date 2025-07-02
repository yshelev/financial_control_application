package com.example.myapplication.repositories
import com.example.myapplication.dto.CardDto
import com.example.myapplication.network.ApiService
import com.example.myapplication.schemas.BalanceCardUpdateSchema
import com.example.myapplication.schemas.CardSchema
import retrofit2.Response

class CardRepository(private val apiService: ApiService) {
    suspend fun getCards(email: String): List<CardDto> {
        return apiService.getCardsByEmail(email)
    }

    suspend fun addCard(card: CardSchema): CardDto {
        return apiService.addCard(card)
    }

    suspend fun getCard(cardId: Long): CardDto {
        return apiService.getCard(cardId)
    }

    suspend fun updateBalanceCard(updateSchema: BalanceCardUpdateSchema): CardDto {
        return apiService.updateCardBalance(updateSchema)
    }

    suspend fun deleteCard(cardId: Long): Response<Void> {
        return apiService.deleteCard(cardId)
    }
    suspend fun deleteUserCards(userId: Long): Response<Void> {
        return apiService.deleteUserCards(userId)
    }
}