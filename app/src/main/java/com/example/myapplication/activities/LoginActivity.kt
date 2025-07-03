package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.database.MainDatabase
import com.example.myapplication.mappers.toEntityList
import kotlinx.coroutines.launch
import android.util.Log

class LoginActivity : AuthBaseActivity() {

    private var isPasswordVisible = false
    private lateinit var db: MainDatabase
    val transactionRepository by lazy {
        (applicationContext as App).transactionRepository
    }

    val cardRepository by lazy {
        (applicationContext as App).cardRepository
    }

    val categoryRepository by lazy {
        (applicationContext as App).categoryRepository
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = App.database

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val passwordToggle = findViewById<ImageView>(R.id.passwordToggle)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val goToRegister = findViewById<TextView>(R.id.goToRegister)

        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordInput, passwordToggle, isPasswordVisible)
        }

        loginButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty()) {
                shakeView(emailInput)
                emailInput.error = getString(R.string.error_enter_email)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                shakeView(passwordInput)
                passwordInput.error = getString(R.string.error_enter_password)
                return@setOnClickListener
            }

            authController.loginAccount(
                email = email,
                password = password,
                onSuccess = {
                    Log.d("chhh", "not fail")
                    lifecycleScope.launch {
                        Log.d("chhh", "not fail 2")

//                        val categories = categoryRepository.getCategories(email)
//                        val categoriesEntity = categories.toEntityList()
//                        db.categoryDao().refreshCategories(categoriesEntity)

                        Log.d("chhh", "not fail 3")

                        val cards = cardRepository.getCards(email)
                        val cardsEntity = cards.toEntityList()
                        db.cardDao().refreshCards(cardsEntity)
                        Log.d("chhh", "not fail 4")
                        val transactions = transactionRepository.getTransactions(email)
                        Log.d("chhh", transactions.toString())
                        val transactionsEntity = transactions.toEntityList()
                        db.transactionDao().refreshTransactions(transactionsEntity)
                        Log.d("chhh", "not fail 5")

                    }
                    Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onFailure = { errorMessage ->
                    Log.d("chhh", "fail")
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }

        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggle: ImageView, visible: Boolean) {
        if (visible) {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggle.setImageResource(R.drawable.ic_eye)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggle.setImageResource(R.drawable.ic_eye_closed)
        }
        editText.setSelection(editText.text.length)
    }

    private fun shakeView(view: EditText) {
        view.animate()
            .translationX(10f)
            .setDuration(50)
            .withEndAction {
                view.animate()
                    .translationX(-10f)
                    .setDuration(50)
                    .withEndAction {
                        view.animate()
                            .translationX(0f)
                            .setDuration(50)
                            .start()
                    }
                    .start()
            }
            .start()
    }
}

