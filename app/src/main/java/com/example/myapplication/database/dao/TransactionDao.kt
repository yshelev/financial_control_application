package com.example.myapplication.database.dao

import androidx.room.*
import com.example.myapplication.database.entities.Card
import com.example.myapplication.database.entities.UserTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<UserTransaction>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Transaction
    suspend fun refreshTransactions(transactions: List<UserTransaction>) {
        deleteAll()
        insertAll(transactions)
    }

    @Insert
    suspend fun insert(transaction: UserTransaction)

    @Update
    suspend fun update(transaction: UserTransaction)

    @Delete
    suspend fun delete(transaction: UserTransaction)

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): UserTransaction?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<UserTransaction>>

    @Query(
        "SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC"
    )
    suspend fun getTransactionsByDateRange(
        startDate: Long,
        endDate: Long
    ): List<UserTransaction>
}