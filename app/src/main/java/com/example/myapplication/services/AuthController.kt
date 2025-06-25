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
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope

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
            onFailure("Email is required")
            return
        }

        if (password.isEmpty()) {
            onFailure("Password is required")
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val user = database.userDao().getUserByEmail(email)

            if (user == null) {
                context.runOnUiThread { onFailure("Invalid email") }
                return@launch
            }

            if (!verify(user.password, password)) {
                context.runOnUiThread { onFailure("Invalid password") }
                return@launch
            }

            saveLoginState(email)
            context.runOnUiThread { onSuccess() }
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

    fun getCurrentUser(callback: (User?) -> Unit) {
        val email = prefs.getString(KEY_USER_EMAIL, null)
        if (email == null) {
            callback(null)
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val user = database.userDao().getUserByEmail(email)
            (context as? AppCompatActivity)?.runOnUiThread {
                callback(user)
            }
        }
    }

    fun updateUser(
        updatedName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val email = prefs.getString(KEY_USER_EMAIL, null)
        if (email == null) {
            onFailure("No logged-in user")
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val user = database.userDao().getUserByEmail(email)
            if (user == null) {
                context.runOnUiThread { onFailure("User not found") }
                return@launch
            }

            val updatedUser = user.copy(
                username = updatedName,
            )

            database.userDao().update(updatedUser)
            context.runOnUiThread { onSuccess() }
        }
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        repeatedPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (newPassword != repeatedPassword) {
            onFailure("New passwords do not match")
            return
        }

        if (newPassword.isEmpty()) {
            onFailure("Password cannot be empty")
            return
        }

        val email = prefs.getString(KEY_USER_EMAIL, null) ?: run {
            onFailure("User is not authorized")
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val user = database.userDao().getUserByEmail(email) ?: run {
                context.runOnUiThread { onFailure("User not found") }
                return@launch
            }

            if (!verify(user.password, oldPassword)) {
                context.runOnUiThread { onFailure("Wrong password") }
                return@launch
            }

            val updatedUser = user.copy(password = hash(newPassword))
            database.userDao().update(updatedUser)

            context.runOnUiThread {
                Toast.makeText(context, "Password successfully changed", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
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