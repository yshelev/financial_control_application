package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
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
    private lateinit var themeIcon: ImageView

    // Инициализатор для синхронизации темы
    init {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            findViewById<ScrollView>(R.id.settingsScroll).scrollTo(0, 0)
        }

        // Привязка UI-элементов
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
        themeIcon = findViewById(R.id.themeIcon)

        // Установка начального состояния UI
        emailTextView.text = "user@example.com"
        nameEditText.setText("Имя пользователя")
        passwordEditText.setText("password")
        nameEditText.isEnabled = false
        passwordEditText.isEnabled = false
        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()

        // Обработчики редактирования
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

        // Переключение темы
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (themeSwitch.isPressed) { // Реагируем только на действие пользователя
                val newMode = if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO

                saveNightMode(newMode)
                AppCompatDelegate.setDefaultNightMode(newMode)
                updateThemeIcon(isChecked)
            }
        }

        // Кнопки
        exportDataButton.setOnClickListener {
            Toast.makeText(this, "Экспорт данных выполнен", Toast.LENGTH_SHORT).show()
        }

        clearDataButton.setOnClickListener {
            Toast.makeText(this, "Все данные очищены", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        saveSettingsButton.setOnClickListener {
            nameEditText.isEnabled = false
            passwordEditText.isEnabled = false
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        // Нижняя навигация
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_wallet -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем UI при возвращении на экран
        updateThemeFromSystem()
    }

    private fun applyThemeFromPreferences() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val mode = sharedPref.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun saveNightMode(mode: Int) {
        getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit {
            putInt("NightMode", mode)
            apply()
        }
    }

    private fun updateThemeFromSystem() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        // Обновляем переключатель только если состояние изменилось
        if (themeSwitch.isChecked != isNightMode) {
            themeSwitch.isChecked = isNightMode
        }

        updateThemeIcon(isNightMode)
    }

    private fun updateThemeIcon(isNightMode: Boolean) {
        themeIcon.setImageResource(
            if (isNightMode) R.drawable.ic_sun else R.drawable.ic_moon
        )
    }
}