package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class RegisterActivity : AuthBaseActivity() {

    private var isPasswordVisible = false
    private var isRepeatPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val repeatPasswordInput = findViewById<EditText>(R.id.repeatPasswordInput)
        val passwordToggle = findViewById<ImageView>(R.id.passwordToggle)
        val repeatPasswordToggle = findViewById<ImageView>(R.id.repeatPasswordToggle)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val goToLogin = findViewById<TextView>(R.id.goToLogin)

        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordInput, passwordToggle, isPasswordVisible)
        }

        repeatPasswordToggle.setOnClickListener {
            isRepeatPasswordVisible = !isRepeatPasswordVisible
            togglePasswordVisibility(repeatPasswordInput, repeatPasswordToggle, isRepeatPasswordVisible)
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                        else it.toString()
                    }
                }
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val repeatPassword = repeatPasswordInput.text.toString().trim()

            var hasError = false
            if (name.isEmpty()) {
                shakeView(nameInput)
                nameInput.error = getString(R.string.error_enter_name)
                hasError = true
            }
            if (email.isEmpty()) {
                shakeView(emailInput)
                emailInput.error = getString(R.string.error_enter_email)
                hasError = true
            }
            if (password.isEmpty()) {
                shakeView(passwordInput)
                passwordInput.error = getString(R.string.error_enter_password)
                hasError = true
            }
            if (repeatPassword.isEmpty()) {
                shakeView(repeatPasswordInput)
                repeatPasswordInput.error = getString(R.string.error_repeat_password)
                hasError = true
            }
            if (password != repeatPassword) {
                shakeView(repeatPasswordInput)
                repeatPasswordInput.error = getString(R.string.error_passwords_no_match)
                hasError = true
            }
            if (hasError) return@setOnClickListener

            authController.createAccount(
                name = name,
                email = email,
                password = password,
                repeatPassword = repeatPassword,
                onSuccess = {
                    Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onFailure = { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
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
