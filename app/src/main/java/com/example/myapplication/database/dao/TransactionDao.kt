package com.example.myapplication.database.dao

import androidx.room.*
import com.example.myapplication.CategoryCurrencySum
import com.example.myapplication.CategorySum
import com.example.myapplication.StatsFragment
import com.example.myapplication.dataClasses.PeriodCurrencyTransaction
import com.example.myapplication.dataClasses.PeriodTransaction
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

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsOnce(): List<UserTransaction>

    @Query(
        "SELECT * FROM transactions " +
                "WHERE date BETWEEN :startDate AND :endDate " +
                "ORDER BY date DESC"
    )
    suspend fun getTransactionsByDateRange(
        startDate: Long,
        endDate: Long
    ): List<UserTransaction>

    @Query("SELECT categories.title as category, SUM(amount) as category_sum " +
            "FROM transactions " +
            "LEFT JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE (date BETWEEN :startDate AND :endDate)" +
            "AND transactions.isIncome = 1 " +
            "GROUP BY categories.id " +
            "ORDER BY category_sum DESC")
    suspend fun getSumIncomeForChart(
        startDate: Long,
        endDate: Long
    ): List<CategorySum>

    @Query("SELECT categories.title as category, SUM(amount) as category_sum " +
            "FROM transactions " +
            "LEFT JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE (date BETWEEN :startDate AND :endDate)" +
            "AND transactions.isIncome = 0 " +
            "GROUP BY categories.id "+
            "ORDER BY category_sum DESC")
    suspend fun getSumExpenseForChart(
        startDate: Long,
        endDate: Long
    ): List<CategorySum>

    @Query("SELECT categories.title as category, " +
                "SUM(transactions.amount) as category_sum, " +
                "cards.currency as currency " +
            "FROM transactions " +
            "LEFT JOIN categories ON transactions.categoryId = categories.id " +
            "LEFT JOIN cards ON transactions.cardId = cards.id " +
            "WHERE (transactions.date BETWEEN :startDate AND :endDate)" +
                "AND transactions.isIncome = 1 " +
            "GROUP BY categories.id, cards.currency " +
            "ORDER BY category_sum DESC")
    suspend fun getSumIncomeCurForChart(
        startDate: Long,
        endDate: Long
    ): List<CategoryCurrencySum>

    @Query("SELECT categories.title as category, " +
                "SUM(transactions.amount) as category_sum, " +
                "cards.currency as currency " +
            "FROM transactions " +
            "LEFT JOIN categories ON transactions.categoryId = categories.id " +
            "LEFT JOIN cards ON transactions.cardId = cards.id " +
            "WHERE (transactions.date BETWEEN :startDate AND :endDate)" +
                "AND transactions.isIncome = 0 " +
            "GROUP BY categories.id, cards.currency " +
            "ORDER BY category_sum DESC")
    suspend fun getSumExpenseCurForChart(
        startDate: Long,
        endDate: Long
    ): List<CategoryCurrencySum>

    @Query("SELECT " +
            "    strftime('%d.%m.%Y', datetime(date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum " +
            "FROM transactions " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 0 " +
            "GROUP BY period " +
            "ORDER BY MIN(date)")
    suspend fun getExpenseForDays(
        startDate: Long,
        endDate: Long
    ): List<PeriodTransaction>

    @Query("SELECT " +
            "    strftime('%m.%Y', datetime(date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum " +
            "FROM transactions " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 0 " +
            "GROUP BY period " +
            "ORDER BY MIN(date)")
    suspend fun getExpenseForMonth(
        startDate: Long,
        endDate: Long
    ): List<PeriodTransaction>

    @Query("SELECT " +
            "    strftime('%Y', datetime(date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum " +
            "FROM transactions " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 0 " +
            "GROUP BY period " +
            "ORDER BY MIN(date)")
    suspend fun getExpenseForYears(
        startDate: Long,
        endDate: Long
    ): List<PeriodTransaction>

    @Query("SELECT " +
            "    strftime('%d.%m.%Y', datetime(date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum " +
            "FROM transactions " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 1 " +
            "GROUP BY period " +
            "ORDER BY MIN(date)")
    suspend fun getIncomeForDays(
        startDate: Long,
        endDate: Long
    ): List<PeriodTransaction>

    @Query("SELECT " +
            "    strftime('%m.%Y', datetime(date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum " +
            "FROM transactions " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 1 " +
            "GROUP BY period " +
            "ORDER BY MIN(date)")
    suspend fun getIncomeForMonth(
        startDate: Long,
        endDate: Long
    ): List<PeriodTransaction>

    @Query("SELECT " +
            "    strftime('%Y', datetime(date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum " +
            "FROM transactions " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 1 " +
            "GROUP BY period " +
            "ORDER BY MIN(date)")
    suspend fun getIncomeForYears(
        startDate: Long,
        endDate: Long
    ): List<PeriodTransaction>

    @Query("SELECT " +
            "    strftime('%d.%m.%Y', datetime(transactions.date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum, " +
            "   cards.currency as currency " +
            "FROM transactions " +
            "LEFT JOIN cards ON transactions.cardId = cards.id " +
            "WHERE transactions.date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 0 " +
            "GROUP BY period, cards.currency " +
            "ORDER BY MIN(transactions.date)")
    suspend fun getCurExpenseForDays(
        startDate: Long,
        endDate: Long
    ): List<PeriodCurrencyTransaction>

    @Query("SELECT " +
            "    strftime('%m.%Y', datetime(transactions.date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum, " +
            "   cards.currency as currency " +
            "FROM transactions " +
            "LEFT JOIN cards ON transactions.cardId = cards.id " +
            "WHERE transactions.date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 0 " +
            "GROUP BY period, cards.currency " +
            "ORDER BY MIN(transactions.date)")
    suspend fun getCurExpenseForMonth(
        startDate: Long,
        endDate: Long
    ): List<PeriodCurrencyTransaction>

    @Query("SELECT " +
            "    strftime('%Y', datetime(transactions.date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum, " +
            "   cards.currency as currency " +
            "FROM transactions " +
            "LEFT JOIN cards ON transactions.cardId = cards.id " +
            "WHERE transactions.date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 0 " +
            "GROUP BY period, cards.currency " +
            "ORDER BY MIN(transactions.date)")
    suspend fun getCurExpenseForYears(
        startDate: Long,
        endDate: Long
    ): List<PeriodCurrencyTransaction>

    @Query("SELECT " +
            "    strftime('%d.%m.%Y', datetime(transactions.date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum, " +
            "   cards.currency as currency " +
            "FROM transactions " +
            "LEFT JOIN cards ON transactions.cardId = cards.id " +
            "WHERE transactions.date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 1 " +
            "GROUP BY period, cards.currency " +
            "ORDER BY MIN(transactions.date)")
    suspend fun getCurIncomeForDays(
        startDate: Long,
        endDate: Long
    ): List<PeriodCurrencyTransaction>

    @Query("SELECT " +
            "    strftime('%m.%Y', datetime(transactions.date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum, " +
            "   cards.currency as currency " +
            "FROM transactions " +
            "LEFT JOIN cards ON transactions.cardId = cards.id " +
            "WHERE transactions.date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 1 " +
            "GROUP BY period, cards.currency " +
            "ORDER BY MIN(transactions.date)")
    suspend fun getCurIncomeForMonth(
        startDate: Long,
        endDate: Long
    ): List<PeriodCurrencyTransaction>

    @Query("SELECT " +
            "    strftime('%Y', datetime(transactions.date / 1000, 'unixepoch')) AS period, " +
            "    SUM(amount) AS sum, " +
            "   cards.currency as currency " +
            "FROM transactions " +
            "LEFT JOIN cards ON transactions.cardId = cards.id " +
            "WHERE transactions.date BETWEEN :startDate AND :endDate " +
            "AND isIncome = 1 " +
            "GROUP BY period, cards.currency " +
            "ORDER BY MIN(transactions.date)")
    suspend fun getCurIncomeForYears(
        startDate: Long,
        endDate: Long
    ): List<PeriodCurrencyTransaction>
}