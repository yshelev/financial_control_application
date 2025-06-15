package com.example.myapplication
import Card
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddCardActivity : AppCompatActivity() {

    private lateinit var cardNameEditText: EditText
    private lateinit var last4DigitsEditText: EditText
    private lateinit var expiryDateEditText: EditText
    private lateinit var balanceEditText: EditText
    private lateinit var saveCardButton: Button
    private lateinit var backButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        cardNameEditText = findViewById(R.id.cardNameEditText)
        last4DigitsEditText = findViewById(R.id.last4DigitsEditText)
        expiryDateEditText = findViewById(R.id.expiryDateEditText)
        balanceEditText = findViewById(R.id.balanceEditText)
        saveCardButton = findViewById(R.id.saveCardButton)
        backButton = findViewById(R.id.backButton)
        val currencySpinner: Spinner = findViewById(R.id.currencySpinner)

        val currencies = listOf("₽", "$", "€")
        val spinnerAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            currencies
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#FFFFFF"))
                view.setHintTextColor(Color.parseColor("#AAAAAA"))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#FFFFFF"))
                return view
            }
        }

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = spinnerAdapter

        expiryDateEditText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                s?.let {
                    val text = it.toString()
                    val digits = text.replace("[^\\d/]".toRegex(), "")
                    if (digits.length == 2 && !digits.endsWith("/")) {
                        val newText = digits + "/"
                        expiryDateEditText.setText(newText)
                        expiryDateEditText.setSelection(newText.length)
                    } else if (digits.length > 2 && !digits.contains("/")) {
                        val newText = digits.substring(0, 2) + "/" + digits.substring(2)
                        expiryDateEditText.setText(newText)
                        expiryDateEditText.setSelection(newText.length)
                    } else {
                        expiryDateEditText.setSelection(digits.length)
                    }
                }
                isEditing = false
            }
        })
        backButton.setOnClickListener {
            finish()
        }

        saveCardButton.setOnClickListener {
            val name = cardNameEditText.text.toString().trim()
            val last4 = last4DigitsEditText.text.toString().trim()
            val date = expiryDateEditText.text.toString().trim()
            val balanceStr = balanceEditText.text.toString().trim()
        }
    }

    private fun validateInputs(name: String, last4: String, date: String, balanceStr: String): Boolean {
        if (name.isEmpty()) {
            showToast("Введите название карты")
            return false
        }
        if (last4.length != 4 || !last4.all { it.isDigit() }) {
            showToast("Введите последние 4 цифры карты")
            return false
        }
        if (!date.matches(Regex("""^(0[1-9]|1[0-2])\/\d{2}$"""))) {
            showToast("Введите дату в формате MM/YY")
            return false
        }
        if (balanceStr.isEmpty()) {
            showToast("Введите баланс карты")
            return false
        }
        try {
            balanceStr.toDouble()
        } catch (e: NumberFormatException) {
            showToast("Баланс должен быть числом")
            return false
        }
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
