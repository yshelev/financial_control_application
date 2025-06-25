package com.example.myapplication

import com.example.myapplication.AuthController
import com.example.myapplication.LoginActivity
import com.example.myapplication.R
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.myapplication.database.MainDatabase

class SettingsFragment : Fragment() {

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
    private lateinit var themeNameTextView: TextView

    private fun applyThemeFromPreferences() {
        val prefs = requireActivity()
            .getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val mode = prefs.getInt(
            "NightMode",
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        applyThemeFromPreferences()
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = MainDatabase.getDatabase(requireContext())
        authController = AuthController(requireActivity(), db)

        nameEditText       = view.findViewById(R.id.nameEditText)
        emailTextView      = view.findViewById(R.id.emailTextView)
        themeSwitch        = view.findViewById(R.id.themeSwitch)
        editNameButton     = view.findViewById(R.id.editNameButton)
        saveSettingsButton = view.findViewById(R.id.saveSettingsButton)
        changePass         = view.findViewById(R.id.changePass)
        logoutButton       = view.findViewById(R.id.logoutButton)
        clearDataButton    = view.findViewById(R.id.clearDataButton)
        exportDataButton   = view.findViewById(R.id.exportDataButton)
        themeIcon          = view.findViewById(R.id.themeIcon)
        currencySpinner    = view.findViewById(R.id.currencySpinner)
        themeNameTextView  = view.findViewById(R.id.themeText)

        nameEditText.isEnabled = false
        view.findViewById<ScrollView>(R.id.settingsScroll).scrollTo(0, 0)

        authController.getCurrentUser { user ->
            requireActivity().runOnUiThread {
                emailTextView.text = user?.email.orEmpty()
                nameEditText.setText(user?.username.orEmpty())
            }
        }

        // Редактирование имени с автокурсором и клавиатурой
        editNameButton.setOnClickListener {
            nameEditText.isEnabled = true
            nameEditText.requestFocus()
            nameEditText.setSelection(nameEditText.text.length)

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(nameEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        // Сохранение имени
        saveSettingsButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authController.updateUser(newName,
                onSuccess = {
                    requireActivity().runOnUiThread {
                        nameEditText.isEnabled = false
                        Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Ошибка: $it", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Плавная анимация переключения темы с затемнением
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (themeSwitch.isPressed) {
                val rootView = requireView()

                rootView.animate().alpha(0f).setDuration(200).withEndAction {
                    val newMode = if (isChecked)
                        AppCompatDelegate.MODE_NIGHT_YES
                    else
                        AppCompatDelegate.MODE_NIGHT_NO

                    requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                        .edit { putInt("NightMode", newMode) }
                    AppCompatDelegate.setDefaultNightMode(newMode)

                    rootView.animate().alpha(1f).setDuration(200).start()
                }.start()

                updateThemeIcon(isChecked)
                updateThemeName(isChecked)
            }
        }

        val currencies = listOf("RUB", "USD", "EUR")
        val currencyAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            currencies
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.setTextColor(ContextCompat.getColor(context, R.color.buttonTextColor))
                return tv
            }
        }
        currencySpinner.adapter = currencyAdapter

        exportDataButton.setOnClickListener {
            Toast.makeText(requireContext(), "Экспорт данных выполнен", Toast.LENGTH_SHORT).show()
        }
        clearDataButton.setOnClickListener {
            Toast.makeText(requireContext(), "Все данные очищены", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            authController.logout()
            startActivity(
                Intent(requireActivity(), LoginActivity::class.java)
                    .apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
            )
            requireActivity().finish()
        }

        changePass.setOnClickListener { showChangePasswordDialog() }

        updateThemeFromSystem()
        updateThemeName(themeSwitch.isChecked)  // Обновляем текст при загрузке
    }

    private fun updateThemeFromSystem() {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNight = (mode == Configuration.UI_MODE_NIGHT_YES)
        themeSwitch.isChecked = isNight
        updateThemeIcon(isNight)
    }

    private fun updateThemeIcon(isNightMode: Boolean) {
        themeIcon.animate().rotationBy(360f).setDuration(600).start()
        themeIcon.setImageResource(
            if (isNightMode) R.drawable.ic_sun else R.drawable.ic_moon
        )
    }


    private fun updateThemeName(isNightMode: Boolean) {
        themeNameTextView.text = if (isNightMode) "Light theme" else "Dark theme"
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val oldPasswordEditText    = dialogView.findViewById<EditText>(R.id.oldPasswordEditText)
        val oldPasswordToggle      = dialogView.findViewById<ImageButton>(R.id.oldPasswordToggle)
        val newPasswordEditText    = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
        val newPasswordToggle      = dialogView.findViewById<ImageButton>(R.id.newPasswordToggle)
        val repeatPasswordEditText = dialogView.findViewById<EditText>(R.id.repeatPasswordEditText)
        val repeatPasswordToggle   = dialogView.findViewById<ImageButton>(R.id.repeatPasswordToggle)
        val confirmButton          = dialogView.findViewById<Button>(R.id.confirmPasswordChangeButton)

        fun toggle(editText: EditText, visible: Boolean, btn: ImageButton) {
            editText.inputType = if (visible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btn.setImageResource(if (visible) R.drawable.ic_eye else R.drawable.ic_eye_closed)
            editText.setSelection(editText.text.length)
        }

        var oldVisible = false
        oldPasswordToggle.setOnClickListener {
            oldVisible = !oldVisible
            toggle(oldPasswordEditText, oldVisible, oldPasswordToggle)
        }

        var newVisible = false
        newPasswordToggle.setOnClickListener {
            newVisible = !newVisible
            toggle(newPasswordEditText, newVisible, newPasswordToggle)
        }

        var repeatVisible = false
        repeatPasswordToggle.setOnClickListener {
            repeatVisible = !repeatVisible
            toggle(repeatPasswordEditText, repeatVisible, repeatPasswordToggle)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        confirmButton.setOnClickListener {
            val oldPass = oldPasswordEditText.text.toString()
            val newPass = newPasswordEditText.text.toString()
            val repeatPass = repeatPasswordEditText.text.toString()

            authController.changePassword(
                oldPassword = oldPass,
                newPassword = newPass,
                repeatedPassword = repeatPass,
                onSuccess = {
                    dialog.dismiss()
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        dialog.setOnShowListener {
            val dialogView = dialog.window?.decorView
            dialogView?.apply {
                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
        }


        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
