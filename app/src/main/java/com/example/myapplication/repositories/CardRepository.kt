package com.example.myapplication.repositories
import com.example.myapplication.database.entities.Card
import com.example.myapplication.network.ApiService
import retrofit2.Response

class CardRepository(private val apiService: ApiService) {
    suspend fun getCards(userId: Int): Response<List<Card>> {
        return apiService.getCardsByUserId(userId)
    }

    suspend fun addCard(card: Card): Response<Card> {
        return apiService.addCard(card)
    }

    suspend fun deleteCard(cardId: Int): Response<Void> {
        return apiService.deleteCard(cardId)
    }
}