package com.example.myapplication.activities

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
            val oldPass = oldPasswordEditText.text.toString()
            val newPass = newPasswordEditText.text.toString()
            val repeatPass = repeatPasswordEditText.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || repeatPass.isEmpty()) {
                Toast.makeText(context, "Fill in all fields", Toast.LENGTH_SHORT).show()
                shakeView(oldPasswordEditText)
                shakeView(newPasswordEditText)
                shakeView(repeatPasswordEditText)
                return@setOnClickListener
            }

            if (newPass != repeatPass) {
                Toast.makeText(context, "The passwords do not match", Toast.LENGTH_SHORT).show()
                shakeView(newPasswordEditText)
                shakeView(repeatPasswordEditText)
                return@setOnClickListener
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change password")
            .setView(view)
            .setNegativeButton("Cancel") { _, _ -> dismiss() }
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
