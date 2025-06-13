package com.example.myapplication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.database.MainDatabase
import com.example.myapplication.database.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.MessageDigest
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit

class AuthController(private val context: Context, private val database: MainDatabase) {

    private val prefsFile = "auth_prefs"

    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            prefsFile,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val KEY_IS_LOGGED_IN = "is_logged_in"
    private val KEY_USER_EMAIL = "user_email"

    fun createAccount(
        name: String,
        email: String,
        password: String,
        repeatPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!validateRegistrationInput(name, email, password, repeatPassword, onFailure)) {
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val existingUser = database.userDao().getUserByEmail(email)
            if (existingUser != null) {
                context.runOnUiThread {
                    onFailure("Email already registered")
                }
                return@launch
            }

            val hashedPassword = hash(password)
            val newUser = User(username = name, email = email, password = hashedPassword)

            database.userDao().insert(newUser)
            loginAccount(email, password, onSuccess, onFailure)
        }
    }

    fun loginAccount(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (email.isEmpty()) {
            onFailure("Email are required")
            return
        }

        if (password.isEmpty()) {
            onFailure("Password are required")
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val user = database.userDao().getUserByEmail(email)
            if (user == null) {
                context.runOnUiThread {
                    onFailure("Invalid email")
                }
            }
            if (user != null && !verify(user.password, password)) {
                context.runOnUiThread {
                    onFailure("Invalid password")
                }
                return@launch
            }

            context.runOnUiThread {
                onSuccess()
            }

            if (user != null && verify(user.password, password)) {
                saveLoginState(email)
                context.runOnUiThread { onSuccess() }
            }
        }
    }

    private fun hash(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest((password + "KOMFORTIK_VIBE").toByteArray(Charsets.UTF_8))
        return String.format("%064x", BigInteger(1, digest))
    }

    private fun verify(password: String, other: String): Boolean {
        return password == hash(other)
    }

    fun logout() {
        prefs.edit() { clear() }
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun saveLoginState(email: String) {
        prefs.edit() {
            putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_EMAIL, email)
        }
    }

    private fun validateRegistrationInput(
        name: String,
        email: String,
        password: String,
        repeatPassword: String,
        onFailure: (String) -> Unit
    ): Boolean {
        if (name.isEmpty()) {
            onFailure("Name is required")
            return false
        }

        if (email.isEmpty()) {
            onFailure("Email is required")
            return false
        }

        if (password.isEmpty()) {
            onFailure("Password is required")
            return false
        }

        if (repeatPassword.isEmpty()) {
            onFailure("Repeated password is required")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onFailure("Invalid email format")
            return false
        }

        if (password != repeatPassword) {
            onFailure("Passwords do not match")
            return false
        }
        return true
    }
}