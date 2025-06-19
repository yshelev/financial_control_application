// network/ApiService.kt
package com.example.myapplication.network

import com.example.myapplication.database.entities.Card
import com.example.myapplication.database.entities.User
import com.example.myapplication.database.entities.UserTransaction
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): Response<User>

    @GET("users/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<User>

    @POST("users")
    suspend fun registerUser(@Body user: User): Response<User>

    @POST("users/login")
    suspend fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): Response<User>

    @GET("users/{id}/cards")
    suspend fun getCardsByUserId(@Path("id") userId: Int): Response<List<Card>>

    @POST("cards")
    suspend fun addCard(@Body card: Card): Response<Card>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") cardId: Int): Response<Void>

    @GET("users/{id}/transactions")
    suspend fun getUserTransactions(@Path("id") userId: Int): Response<List<UserTransaction>>

    @POST("transactions")
    suspend fun addTransaction(@Body transaction: UserTransaction): Response<UserTransaction>
}