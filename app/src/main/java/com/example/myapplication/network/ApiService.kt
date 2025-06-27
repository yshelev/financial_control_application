package com.example.myapplication.network

import com.example.myapplication.database.entities.User
import com.example.myapplication.dto.CardDto
import com.example.myapplication.dto.TransactionDto
import com.example.myapplication.dto.UserDto
import com.example.myapplication.schemas.CardSchema
import com.example.myapplication.schemas.TransactionSchema
import com.example.myapplication.schemas.UserSchema
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): UserDto

    @GET("users/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<UserDto>

    @POST("users")
    suspend fun registerUser(@Body user: UserSchema): UserDto

    @POST("users/login")
    suspend fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): UserDto

    @GET("users/{email}/cards")
    suspend fun getCardsByEmail(@Path("email") email: String): List<CardDto>

    @POST("cards")
    suspend fun addCard(@Body card: CardSchema): CardDto

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") cardId: Int): CardDto

    @DELETE("cards/{id}/")
    suspend fun deleteCard(@Path("id") cardId: Int): Void

    @GET("users/{email}/transactions/")
    suspend fun getUserTransactions(@Path("email") email: String): List<TransactionDto>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") transactionId: Int): TransactionDto

    @POST("transactions/")
    suspend fun addTransaction(@Body transaction: TransactionSchema): TransactionDto
}