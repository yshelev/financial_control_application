package com.example.myapplication.database.dao

import androidx.room.*
import com.example.myapplication.database.entities.UserTransaction

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: UserTransaction): Long

    @Update
    suspend fun update(transaction: UserTransaction)

    @Delete
    suspend fun delete(transaction: UserTransaction)

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): UserTransaction?

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionDate DESC")
    suspend fun getTransactionsByUser(userId: Long): List<UserTransaction>

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactionsForUser(userId: Long)

    @Query(
        "SELECT * FROM transactions WHERE userId = :userId AND transactionDate BETWEEN :startDate AND :endDate ORDER BY transactionDate DESC"
    )
    suspend fun getTransactionsByDateRange(
        userId: Long,
        startDate: Long,
        endDate: Long
    ): List<UserTransaction>
}