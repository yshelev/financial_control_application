package com.example.myapplication.database.dao

import androidx.room.*
import com.example.myapplication.CategorySum
import com.example.myapplication.StatsFragment
import com.example.myapplication.database.entities.Card
import com.example.myapplication.database.entities.UserTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
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


    @Query("SELECT category, SUM(amount) as category_sum " +
            "FROM transactions " +
            "WHERE (date BETWEEN :startDate AND :endDate)" +
            "AND isIncome = 1 " +
            "GROUP BY category")
    suspend fun getSumIncomeForChart(
        startDate: Long,
        endDate: Long
    ): List<CategorySum>

    @Query("SELECT category, SUM(amount) as category_sum " +
            "FROM transactions " +
            "WHERE (date BETWEEN :startDate AND :endDate) " +
            "AND isIncome = 0 " +
            "GROUP BY category")
    suspend fun getSumExpenseForChart(
        startDate: Long,
        endDate: Long
    ): List<CategorySum>
}