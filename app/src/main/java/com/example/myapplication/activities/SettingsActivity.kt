package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.example.myapplication.database.MainDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var authController: AuthController
    private lateinit var nameEditText: EditText
    private lateinit var emailTextView: TextView
    private lateinit var themeSwitch: Switch
    private lateinit var editNameButton: ImageButton
    private lateinit var saveSettingsButton: Button
    private lateinit var changePass: Button
    private lateinit var logoutButton: Button
    private lateinit var clearDataButton: Button
    private lateinit var exportDataButton: Button
    private lateinit var themeIcon: ImageView
    private lateinit var currencySpinner: Spinner

    // Новые поля для смены пароля
    private lateinit var oldPasswordLayout: View
    private lateinit var oldPasswordEditText: EditText
    private lateinit var oldPasswordToggle: ImageButton
    private lateinit var verifyOldPasswordButton: Button

    private lateinit var newPasswordLayout: View
    private lateinit var newPasswordEditText: EditText
    private lateinit var newPasswordToggle: ImageButton

    private lateinit var repeatPasswordLayout: View
    private lateinit var repeatPasswordEditText: EditText
    private lateinit var repeatPasswordToggle: ImageButton

    private lateinit var confirmPasswordChangeButton: Button

    private var oldPasswordVisible = false
    private var newPasswordVisible = false
    private var repeatPasswordVisible = false

    private var currentEmail: String? = null

    init {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val database = MainDatabase.getDatabase(this)
        authController = AuthController(this, database)

        if (savedInstanceState == null) {
            findViewById<ScrollView>(R.id.settingsScroll).scrollTo(0, 0)
        }

        // Привязка UI-элементов
        nameEditText = findViewById(R.id.nameEditText)
        emailTextView = findViewById(R.id.emailTextView)
        themeSwitch = findViewById(R.id.themeSwitch)
        editNameButton = findViewById(R.id.editNameButton)
        saveSettingsButton = findViewById(R.id.saveSettingsButton)
        changePass = findViewById(R.id.changePass)
        logoutButton = findViewById(R.id.logoutButton)
        clearDataButton = findViewById(R.id.clearDataButton)
        exportDataButton = findViewById(R.id.exportDataButton)
        themeIcon = findViewById(R.id.themeIcon)
        currencySpinner = findViewById(R.id.currencySpinner)

        // Инициализация новых элементов для смены пароля
        oldPasswordLayout = findViewById(R.id.oldPasswordLayout)
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        oldPasswordToggle = findViewById(R.id.oldPasswordToggle)
        verifyOldPasswordButton = findViewById(R.id.verifyOldPasswordButton)

        newPasswordLayout = findViewById(R.id.newPasswordLayout)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        newPasswordToggle = findViewById(R.id.newPasswordToggle)

        repeatPasswordLayout = findViewById(R.id.repeatPasswordLayout)
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText)
        repeatPasswordToggle = findViewById(R.id.repeatPasswordToggle)

        confirmPasswordChangeButton = findViewById(R.id.confirmPasswordChangeButton)

        // Скрываем поля смены пароля по умолчанию
        oldPasswordLayout.visibility = View.GONE
        verifyOldPasswordButton.visibility = View.GONE
        newPasswordLayout.visibility = View.GONE
        repeatPasswordLayout.visibility = View.GONE
        confirmPasswordChangeButton.visibility = View.GONE

        // Установка начального состояния UI
        nameEditText.isEnabled = false

        authController.getCurrentUser { user ->
            if (user != null) {
                currentEmail = user.email
                emailTextView.text = user.email
                nameEditText.setText(user.username)
            } else {
                emailTextView.text = "user@example.com"
                nameEditText.setText("")
            }
        }

        editNameButton.setOnClickListener {
            nameEditText.isEnabled = true
            nameEditText.requestFocus()
            nameEditText.setSelection(nameEditText.text.length)
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (themeSwitch.isPressed) {
                val newMode = if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO

                saveNightMode(newMode)
                AppCompatDelegate.setDefaultNightMode(newMode)
                updateThemeIcon(isChecked)
            }
        }

        val currencies = listOf("RUB", "USD", "EUR")
        val currencyAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencies) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#D0B8F5"))
                return view
            }
        }
        currencySpinner.adapter = currencyAdapter

        exportDataButton.setOnClickListener {
            Toast.makeText(this, "Экспорт данных выполнен", Toast.LENGTH_SHORT).show()
        }

        clearDataButton.setOnClickListener {
            Toast.makeText(this, "Все данные очищены", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            authController.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        saveSettingsButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authController.updateUser(newName,
                onSuccess = {
                    runOnUiThread {
                        nameEditText.isEnabled = false
                        Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = {
                    runOnUiThread {
                        Toast.makeText(this, "Ошибка: $it", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

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

        // Логика кнопок смены пароля и глазиков

        changePass.setOnClickListener {
            oldPasswordLayout.visibility = View.VISIBLE
            verifyOldPasswordButton.visibility = View.VISIBLE

            newPasswordLayout.visibility = View.GONE
            repeatPasswordLayout.visibility = View.GONE
            confirmPasswordChangeButton.visibility = View.GONE
        }

        verifyOldPasswordButton.setOnClickListener {
            // Здесь можно добавить проверку старого пароля перед открытием новых полей

            newPasswordLayout.visibility = View.VISIBLE
            repeatPasswordLayout.visibility = View.VISIBLE
            confirmPasswordChangeButton.visibility = View.VISIBLE
        }

        oldPasswordToggle.setOnClickListener {
            oldPasswordVisible = !oldPasswordVisible
            togglePasswordVisibility(oldPasswordEditText, oldPasswordVisible, oldPasswordToggle)
        }

        newPasswordToggle.setOnClickListener {
            newPasswordVisible = !newPasswordVisible
            togglePasswordVisibility(newPasswordEditText, newPasswordVisible, newPasswordToggle)
        }

        repeatPasswordToggle.setOnClickListener {
            repeatPasswordVisible = !repeatPasswordVisible
            togglePasswordVisibility(repeatPasswordEditText, repeatPasswordVisible, repeatPasswordToggle)
        }

        confirmPasswordChangeButton.setOnClickListener {
            // Здесь добавьте логику смены пароля с валидацией
            val newPass = newPasswordEditText.text.toString()
            val repeatPass = repeatPasswordEditText.text.toString()
            if (newPass.isEmpty() || repeatPass.isEmpty()) {
                Toast.makeText(this, "Введите новый пароль и повторите его", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != repeatPass) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

        }
    }

    private fun togglePasswordVisibility(editText: EditText, visible: Boolean, toggleButton: ImageButton) {
        if (visible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.ic_eye)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.ic_eye_closed)
        }
        editText.setSelection(editText.text.length)
    }

    override fun onResume() {
        super.onResume()
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
