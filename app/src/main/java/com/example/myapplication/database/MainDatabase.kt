package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.database.dao.CardDao
import com.example.myapplication.database.dao.TransactionDao
import com.example.myapplication.database.dao.UserDao
import com.example.myapplication.database.entities.UserTransaction
import com.example.myapplication.database.entities.User
import com.example.myapplication.database.entities.Card

@Database(
    entities = [User::class, UserTransaction::class, Card::class],
    version = 7,
    exportSchema = false
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: MainDatabase? = null

        fun getDatabase(context: Context): MainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}