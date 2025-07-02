package com.example.myapplication.network

import com.example.myapplication.dataClasses.ResendVerificationRequest
import com.example.myapplication.dto.BackupData
import com.example.myapplication.dto.CardDto
import com.example.myapplication.dto.CategoryDto
import com.example.myapplication.dto.CreateBackupRequest
import com.example.myapplication.dto.GetBackupRequest
import com.example.myapplication.dto.TransactionDto
import com.example.myapplication.dto.UserDto
import com.example.myapplication.schemas.BalanceCardUpdateSchema
import com.example.myapplication.schemas.CardSchema
import com.example.myapplication.schemas.CreateCategorySchema
import com.example.myapplication.schemas.TransactionSchema
import com.example.myapplication.schemas.UserSchema
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): UserDto

    @PUT("/backup/")
    suspend fun createBackup(@Body backupData: CreateBackupRequest): Response<Any>

    @PUT("/backup/")
    suspend fun getBackup(@Body backupData: GetBackupRequest): Response<BackupData>

    @GET("users/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<UserDto>

    @POST("users")
    suspend fun registerUser(@Body user: UserSchema): UserDto

    @POST("users/login")
    suspend fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): UserDto

    @POST("users/register")
    suspend fun registerWithVerification(@Body user: UserSchema): Response<UserDto>

    @GET("users/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Response<Unit>

    @POST("users/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): Response<Unit>

    @GET("users/check-verified")
    suspend fun checkEmailVerified(@Query("email") email: String): Response<Boolean>

    @GET("users/{email}/cards")
    suspend fun getCardsByEmail(@Path("email") email: String): List<CardDto>

    @GET("users/{email}/categories/")
    suspend fun getCategories(@Path("email") email: String): List<CategoryDto>

    @POST("cards")
    suspend fun addCard(@Body card: CardSchema): CardDto

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") cardId: Long): CardDto

    @PATCH("cards")
    suspend fun updateCardBalance(@Body updateSchema: BalanceCardUpdateSchema): CardDto

    @DELETE("cards/{id}/")
    suspend fun deleteCard(@Path("id") cardId: Long): Response<Void>

    @GET("users/{email}/transactions/")
    suspend fun getUserTransactions(@Path("email") email: String): List<TransactionDto>


    @DELETE("transactions/{id}/")
    suspend fun deleteTransaction(@Path("id") cardId: Long): Response<Void>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") transactionId: Int): TransactionDto

    @POST("transactions/")
    suspend fun addTransaction(@Body transaction: TransactionSchema): TransactionDto

    @POST("categories/")
    suspend fun addCategories(@Body category: CreateCategorySchema): CategoryDto

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") categoryId: Long): CategoryDto

    @GET("categories/")
    suspend fun getCategories(@Path("id") categoryId: Long): CategoryDto

    @DELETE("categories/{id}/")
    suspend fun deleteCategory(@Path("id") categoryId: Long): Response<Void>
}