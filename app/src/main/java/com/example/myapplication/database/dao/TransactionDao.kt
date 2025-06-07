package com.example.myapplication.database.dao

import androidx.room.*

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionDate DESC")
    suspend fun getTransactionsByUser(userId: Long): List<Transaction>

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactionsForUser(userId: Long)

    @Query(
        "SELECT * FROM transactions WHERE userId = :userId AND transactionDate BETWEEN :startDate AND :endDate ORDER BY transactionDate DESC"
    )
    suspend fun getTransactionsByDateRange(
        userId: Long,
        startDate: Long, // timestamp в миллисекундах
        endDate: Long   // timestamp в миллисекундах
    ): List<Transaction>
}