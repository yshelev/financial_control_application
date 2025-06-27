package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.schemas.CardSchema
import kotlinx.coroutines.launch

class AddCardActivity : AuthBaseActivity() {

    private lateinit var cardNameEditText: EditText
    private lateinit var last4DigitsEditText: EditText
    private lateinit var expiryDateEditText: EditText
    private lateinit var balanceEditText: EditText
    private lateinit var saveCardButton: Button
    private lateinit var backButton: ImageButton
//    private lateinit var currencySpinner: Spinner

    private val cardRepository by lazy {
        (applicationContext as App).cardRepository
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        cardNameEditText = findViewById(R.id.cardNameEditText)
        last4DigitsEditText = findViewById(R.id.last4DigitsEditText)
        expiryDateEditText = findViewById(R.id.expiryDateEditText)
        balanceEditText = findViewById(R.id.balanceEditText)
        saveCardButton = findViewById(R.id.saveCardButton)
        backButton = findViewById(R.id.backButton)
//        currencySpinner = findViewById(R.id.currencySpinner)

//        val currencies = listOf("₽", "$", "€")
//        val spinnerAdapter = object : ArrayAdapter<String>(
//            this,
//            android.R.layout.simple_spinner_item,
//            currencies
//        ) {
//            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//                val view = super.getView(position, convertView, parent) as TextView
//                view.setTextColor(Color.WHITE)
//                return view
//            }
//
//            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
//                val view = super.getDropDownView(position, convertView, parent) as TextView
//                view.setTextColor(Color.WHITE)
//                return view
//            }
//        }
//
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        currencySpinner.adapter = spinnerAdapter

        expiryDateEditText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                s?.let {
                    val digits = it.toString().replace("[^\\d/]".toRegex(), "")
                    val newText = when {
                        digits.length == 2 && !digits.endsWith("/") -> digits + "/"
                        digits.length > 2 && !digits.contains("/") ->
                            digits.substring(0, 2) + "/" + digits.substring(2)
                        else -> digits
                    }
                    expiryDateEditText.setText(newText)
                    expiryDateEditText.setSelection(newText.length)
                }
                isEditing = false
            }
        })

        listOf(cardNameEditText, last4DigitsEditText, expiryDateEditText, balanceEditText).forEach { editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    editText.error = null
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        backButton.setOnClickListener {
            finish()
        }

        saveCardButton.setOnClickListener {
            val name = cardNameEditText.text.toString().trim()
            val last4 = last4DigitsEditText.text.toString().trim()
            val date = expiryDateEditText.text.toString().trim()
            val balanceStr = balanceEditText.text.toString().trim()
//            val currency = currencySpinner.selectedItem.toString()

            if (!validateInputs(name, last4, date, balanceStr)) {
                return@setOnClickListener
            }

            try {
                val balance = if (balanceStr.isNotEmpty()) balanceStr.toDouble() else 0.0

                authController.getCurrentUser {
                    user ->
                    if (user?.email == null) {
                        return@getCurrentUser
                    }
                    val card = CardSchema(
                        name = name,
                        balance = balance,
                        masked_number = last4,
                        date = date,
//                    currency = currency,
                        owner_email = user.email
                    )
                    lifecycleScope.launch{
                        cardRepository.addCard(card)
                        finish()
                    }

                }

            } catch (e: Exception) {
                balanceEditText.error = "Ошибка при сохранении: ${e.message}"
            }
        }
    }

    private fun validateInputs(name: String, last4: String, date: String, balanceStr: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            cardNameEditText.error = "Введите название карты"
            isValid = false
        }

        if (last4.length != 4 || !last4.all { it.isDigit() }) {
            last4DigitsEditText.error = "Введите 4 цифры"
            isValid = false
        }

        if (!date.matches(Regex("""^(0[1-9]|1[0-2])\/\d{2}$"""))) {
            expiryDateEditText.error = "Формат MM/YY"
            isValid = false
        }

        if (balanceStr.isNotEmpty()) {
            try {
                balanceStr.toDouble()
            } catch (e: NumberFormatException) {
                balanceEditText.error = "Баланс должен быть числом"
                isValid = false
            }
        }

        return isValid
    }
}
