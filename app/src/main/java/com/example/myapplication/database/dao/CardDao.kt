package com.example.myapplication.database.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.database.entities.Card
import kotlinx.coroutines.flow.Flow


@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Card>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Transaction
    suspend fun refreshCards(cards: List<Card>) {
        deleteAll()
        Log.d("Transaction", "deleted all cards")
        Log.d("Transaction", "entities size: ${cards.size}")
        insertAll(cards)
    }

    @Insert
    suspend fun insert(card: Card): Long

    @Update
    suspend fun update(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Query("SELECT * FROM cards")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE name = :name")
    suspend fun getCardByName(name: String): Card?

    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): Card?

    @Query("SELECT * FROM cards")
    suspend fun getAllCardsOnce(): List<Card>
}
