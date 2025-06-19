package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var emailTextView: TextView
    private lateinit var themeSwitch: Switch
    private lateinit var editNameButton: ImageButton
    private lateinit var editPasswordButton: ImageButton
    private lateinit var saveSettingsButton: Button
    private lateinit var logoutButton: Button
    private lateinit var clearDataButton: Button
    private lateinit var exportDataButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Привязка UI
        nameEditText = findViewById(R.id.nameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        emailTextView = findViewById(R.id.emailTextView)
        themeSwitch = findViewById(R.id.themeSwitch)
        editNameButton = findViewById(R.id.editNameButton)
        editPasswordButton = findViewById(R.id.editPasswordButton)
        saveSettingsButton = findViewById(R.id.saveSettingsButton)
        logoutButton = findViewById(R.id.logoutButton)
        clearDataButton = findViewById(R.id.clearDataButton)
        exportDataButton = findViewById(R.id.exportDataButton)

        emailTextView.text = "user@example.com"
        nameEditText.setText("Имя пользователя")
        passwordEditText.setText("password")

        nameEditText.isEnabled = false
        passwordEditText.isEnabled = false
        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()

        editNameButton.setOnClickListener {
            nameEditText.isEnabled = true
            nameEditText.requestFocus()
            nameEditText.setSelection(nameEditText.text.length)
        }

        editPasswordButton.setOnClickListener {
            passwordEditText.isEnabled = true
            passwordEditText.transformationMethod = null
            passwordEditText.requestFocus()
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)

            val themeIcon = findViewById<ImageView>(R.id.themeIcon)
            themeIcon.setImageResource(
                if (isChecked) R.drawable.ic_sun else R.drawable.ic_moon
            )
        }

        exportDataButton.setOnClickListener {
            Toast.makeText(this, "Экспорт данных выполнен", Toast.LENGTH_SHORT).show()
            // TODO: Реализовать экспорт
        }

        clearDataButton.setOnClickListener {
            Toast.makeText(this, "Все данные очищены", Toast.LENGTH_SHORT).show()
            // TODO: Очистка данных из БД
        }

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        saveSettingsButton.setOnClickListener {
            // TODO: Сохранить имя/пароль
            nameEditText.isEnabled = false
            passwordEditText.isEnabled = false
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_wallet-> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }
}
