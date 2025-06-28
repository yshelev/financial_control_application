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
import android.util.Log
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit
import com.example.myapplication.schemas.UserSchema

class AuthController(private val context: Context, private val database: MainDatabase) {

    private val prefsFile = "auth_prefs"

    private val userRepository by lazy {
        (context.applicationContext as App).userRepository
    }

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
//            val existingUser = database.userDao().getUserByEmail(email)
            val existingUser_ = userRepository.getUserByEmail(email).body()
            if (existingUser_ != null) {
                Log.d("AuthController", "user not null")
                context.runOnUiThread {
                    onFailure(context.getString(R.string.error_email_registered))
                }
                return@launch
            }

            val hashedPassword = hash(password)
            val newUser = User(username = name, email = email, password = hashedPassword)
            val newUserS = UserSchema(
                username = name,
                email = email,
                password = hashedPassword
            )
            context.lifecycleScope.launch {
                userRepository.registerUser(newUserS)
            }

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
            onFailure(context.getString(R.string.error_email_required))
            return
        }

        if (password.isEmpty()) {
            onFailure(context.getString(R.string.error_password_required))
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val user = database.userDao().getUserByEmail(email)

            if (user == null) {
                context.runOnUiThread { onFailure(context.getString(R.string.error_invalid_email)) }
                return@launch
            }

            if (!verify(user.password, password)) {
                context.runOnUiThread { onFailure(context.getString(R.string.error_invalid_password)) }
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
        prefs.edit { clear() }
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun saveLoginState(email: String) {
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
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
            onFailure(context.getString(R.string.error_no_logged_user))
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val user = database.userDao().getUserByEmail(email)
            if (user == null) {
                context.runOnUiThread { onFailure(context.getString(R.string.error_user_not_found)) }
                return@launch
            }

            val updatedUser = user.copy(username = updatedName)

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
            onFailure(context.getString(R.string.error_passwords_not_match))
            return
        }

        if (newPassword.isEmpty()) {
            onFailure(context.getString(R.string.error_password_empty))
            return
        }

        val email = prefs.getString(KEY_USER_EMAIL, null) ?: run {
            onFailure(context.getString(R.string.error_user_not_authorized))
            return
        }

        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val user = database.userDao().getUserByEmail(email) ?: run {
                context.runOnUiThread { onFailure(context.getString(R.string.error_user_not_found)) }
                return@launch
            }

            if (!verify(user.password, oldPassword)) {
                context.runOnUiThread { onFailure(context.getString(R.string.error_wrong_password)) }
                return@launch
            }

            val updatedUser = user.copy(password = hash(newPassword))
            database.userDao().update(updatedUser)

            context.runOnUiThread {
                Toast.makeText(context, context.getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show()
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
            onFailure(context.getString(R.string.error_name_required))
            return false
        }

        if (email.isEmpty()) {
            onFailure(context.getString(R.string.error_email_required))
            return false
        }

        if (password.isEmpty()) {
            onFailure(context.getString(R.string.error_password_required))
            return false
        }

        if (repeatPassword.isEmpty()) {
            onFailure(context.getString(R.string.error_repeat_password_required))
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onFailure(context.getString(R.string.error_invalid_email_format))
            return false
        }

        if (password != repeatPassword) {
            onFailure(context.getString(R.string.error_passwords_not_match))
            return false
        }
        return true
    }
}
