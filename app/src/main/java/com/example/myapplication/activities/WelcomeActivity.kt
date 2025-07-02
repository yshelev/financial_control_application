package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class WelcomeActivity : AuthBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
//        lifecycleScope.launch {
//            App.database.userDao().deleteAll()
//        }

        val startButton = findViewById<Button>(R.id.startButton)
        val skipRegistrationButton = findViewById<Button>(R.id.startWithoutRegistrationButton)
        val registerLink = findViewById<TextView>(R.id.registerLink)

        startButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        skipRegistrationButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (authController.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

