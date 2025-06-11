package com.example.myapplication

import android.app.Application
import com.example.myapplication.database.MainDatabase

class App : Application() {

    companion object {
        lateinit var database: MainDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = MainDatabase.getDatabase(this)
    }
}