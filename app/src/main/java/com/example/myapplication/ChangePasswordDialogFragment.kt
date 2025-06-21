package com.example.myapplication

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.myapplication.AuthController

class ChangePasswordDialogFragment(
    private val authController: AuthController
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_change_password, null)

        val oldPasswordEditText = view.findViewById<EditText>(R.id.oldPasswordEditText)
        val newPasswordEditText = view.findViewById<EditText>(R.id.newPasswordEditText)
        val repeatPasswordEditText = view.findViewById<EditText>(R.id.repeatPasswordEditText)
        val confirmButton = view.findViewById<Button>(R.id.confirmPasswordChangeButton)

        confirmButton.setOnClickListener {
            val oldPass = oldPasswordEditText.text.toString()
            val newPass = newPasswordEditText.text.toString()
            val repeatPass = repeatPasswordEditText.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || repeatPass.isEmpty()) {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != repeatPass) {
                Toast.makeText(context, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Смена пароля")
            .setView(view)
            .setNegativeButton("Отмена") { _, _ -> dismiss() }
            .create()
    }
}
