package com.example.myapplication.activities

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.myapplication.AuthController
import com.example.myapplication.R

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
            val oldPass = oldPasswordEditText.text.toString().trim()
            val newPass = newPasswordEditText.text.toString().trim()
            val repeatPass = repeatPasswordEditText.text.toString().trim()

            var hasError = false

            if (oldPass.isEmpty()) {
                shakeView(oldPasswordEditText)
                oldPasswordEditText.error = getString(R.string.error_enter_old_password)
                hasError = true
            }
            if (newPass.isEmpty()) {
                shakeView(newPasswordEditText)
                newPasswordEditText.error = getString(R.string.error_enter_password)
                hasError = true
            }
            if (repeatPass.isEmpty()) {
                shakeView(repeatPasswordEditText)
                repeatPasswordEditText.error = getString(R.string.error_repeat_password)
                hasError = true
            }
            if (newPass.isNotEmpty() && repeatPass.isNotEmpty() && newPass != repeatPass) {
                shakeView(repeatPasswordEditText)
                repeatPasswordEditText.error = getString(R.string.error_passwords_no_match)
                hasError = true
            }

            if (hasError) return@setOnClickListener

            authController.changePassword(
                oldPassword = oldPass,
                newPassword = newPass,
                repeatedPassword = repeatPass,
                onSuccess = {
                    dismiss()
                },
                onFailure = { errorMessage ->
                    oldPasswordEditText.text.clear()
                    shakeView(oldPasswordEditText)
                    oldPasswordEditText.error = errorMessage
                }
            )
    }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_password))
            .setView(view)
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> dismiss() }
            .create()

        dialog.setOnShowListener {
            val rootView = dialog.window?.decorView
            rootView?.alpha = 0f
            rootView?.animate()?.alpha(1f)?.setDuration(300)?.start()
        }

        return dialog
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

    override fun dismiss() {
        val rootView = dialog?.window?.decorView
        rootView?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
            super.dismiss()
        }?.start() ?: super.dismiss()
    }
}
