package com.example.myapplication

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.myapplication.database.MainDatabase
import com.example.myapplication.network.RetrofitInstance
import com.example.myapplication.repositories.UserRepository
import com.example.myapplication.repositories.CardRepository
import com.example.myapplication.repositories.TransactionRepository
import android.content.Context
import com.example.myapplication.database.entities.Category
import com.example.myapplication.repositories.BackupRepository
import com.example.myapplication.repositories.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    companion object {
        lateinit var database: MainDatabase
            private set
    }

    val apiService by lazy { RetrofitInstance.apiService }
    val userRepository by lazy { UserRepository(apiService) }
    val cardRepository by lazy { CardRepository(apiService) }
    val transactionRepository by lazy { TransactionRepository(apiService) }
    val categoryRepository by lazy { CategoryRepository(apiService) }
    val backupRepository by lazy { BackupRepository(apiService) }



    override fun onCreate() {
        super.onCreate()
        database = MainDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            val dao = database.categoryDao()
            if (dao.getAll().isEmpty()) {
                dao.insertAll(
                    listOf(
                        Category(title = "Зарплата", iconResId = R.drawable.ic_salary, color = "#50FF9D", isIncome = true),
                        Category(title = "Подарок", iconResId = R.drawable.ic_gift, color = "#50FF9D", isIncome = true),
                        Category(title = "Инвестиции", iconResId = R.drawable.ic_investment, color = "#50FF9D", isIncome = true),

                        Category(title = "Еда", iconResId = R.drawable.ic_food, color = "#FF6E6E", isIncome = false),
                        Category(title = "Транспорт", iconResId = R.drawable.ic_transport, color = "#FF6E6E", isIncome = false),
                        Category(title = "Одежда", iconResId = R.drawable.ic_clothes, color = "#FF6E6E", isIncome = false),
                        Category(title = "Образование", iconResId = R.drawable.ic_education, color = "#FF6E6E", isIncome = false),
                        Category(title = "Здоровье", iconResId = R.drawable.ic_health, color = "#FF6E6E", isIncome = false),
                        Category(title = "Развлечения", iconResId = R.drawable.ic_entertainment, color = "#FF6E6E", isIncome = false)
                    )
                )
            }
        }

        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        if (!sharedPref.contains("NightMode")) {
            sharedPref.edit().putInt("NightMode", AppCompatDelegate.MODE_NIGHT_YES).apply()
        }

        val mode = sharedPref.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_YES)
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}