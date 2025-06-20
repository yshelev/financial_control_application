package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.App
import com.example.myapplication.AuthController

abstract class AuthBaseActivity : AppCompatActivity() {

    protected lateinit var authController: AuthController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authController = AuthController(this, App.database)
    }
}