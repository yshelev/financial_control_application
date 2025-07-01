package com.example.myapplication

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.myapplication.database.MainDatabase
import com.example.myapplication.network.RetrofitInstance
import com.example.myapplication.repositories.UserRepository
import com.example.myapplication.repositories.CardRepository
import com.example.myapplication.repositories.TransactionRepository
import android.content.Context
import com.example.myapplication.repositories.BackupRepository

class App : Application() {

    companion object {
        lateinit var database: MainDatabase
            private set
    }

    val apiService by lazy { RetrofitInstance.apiService }
    val userRepository by lazy { UserRepository(apiService) }
    val cardRepository by lazy { CardRepository(apiService) }
    val transactionRepository by lazy { TransactionRepository(apiService) }
    val backupRepository by lazy { BackupRepository(apiService) }

    override fun onCreate() {
        super.onCreate()
        database = MainDatabase.getDatabase(this)

        // Устанавливаем темную тему по умолчанию
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        // Если тема еще не сохранена в настройках, устанавливаем темную
        if (!sharedPref.contains("NightMode")) {
            sharedPref.edit().putInt("NightMode", AppCompatDelegate.MODE_NIGHT_YES).apply()
        }
        // Применяем сохраненную тему
        val mode = sharedPref.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_YES) // Изменено значение по умолчанию
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}