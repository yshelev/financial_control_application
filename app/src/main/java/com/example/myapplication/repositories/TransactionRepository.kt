package com.example.myapplication.repositories

import com.example.myapplication.dto.TransactionDto
import com.example.myapplication.network.ApiService
import com.example.myapplication.schemas.TransactionSchema
import retrofit2.Response

class TransactionRepository(private val apiService: ApiService) {
    suspend fun getTransactions(email: String): List<TransactionDto> {
        return apiService.getUserTransactions(email)
    }

    suspend fun addTransaction(transaction: TransactionSchema): TransactionDto {
        return apiService.addTransaction(transaction)
    }

    suspend fun getTransaction(transactionId: Int): TransactionDto {
        return apiService.getTransaction(transactionId)
    }

    suspend fun deleteTransaction(transactionId: Long): Response<Void> {
        return apiService.deleteTransaction(transactionId)
    }
}