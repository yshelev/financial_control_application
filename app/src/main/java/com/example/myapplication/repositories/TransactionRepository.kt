package com.example.myapplication.repositories

import com.example.myapplication.database.entities.UserTransaction
import com.example.myapplication.network.ApiService
import retrofit2.Response

class TransactionRepository(private val apiService: ApiService) {
    suspend fun getTransactions(userId: Int): Response<List<UserTransaction>> {
        return apiService.getUserTransactions(userId)
    }

    suspend fun addTransaction(transaction: UserTransaction): Response<UserTransaction> {
        return apiService.addTransaction(transaction)
    }
}