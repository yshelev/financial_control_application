package com.example.myapplication

import android.app.Application
import com.example.myapplication.database.MainDatabase
import com.example.myapplication.network.RetrofitInstance
import com.example.myapplication.repositories.UserRepository
import com.example.myapplication.repositories.CardRepository
import com.example.myapplication.repositories.TransactionRepository

class App : Application() {

    companion object {
        lateinit var database: MainDatabase
            private set
    }

    val apiService by lazy { RetrofitInstance.apiService }
    val userRepository by lazy { UserRepository(apiService) }
    val cardRepository by lazy { CardRepository(apiService) }
    val transactionRepository by lazy { TransactionRepository(apiService) }

    override fun onCreate() {
        super.onCreate()
        database = MainDatabase.getDatabase(this)
    }
}