package com.example.myapplication

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.database.entities.UserTransaction
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var dateCard: MaterialCardView
    private lateinit var selectDateText: TextView
    private lateinit var cardSpinner: Spinner
    private lateinit var incomeToggle: Button
    private lateinit var expenseToggle: Button
    private lateinit var typeGroup: MaterialButtonToggleGroup
    private lateinit var categorySpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var currencySpinner: Spinner
    private lateinit var backButton: ImageButton

    private var selectedDate: Calendar = Calendar.getInstance()
    private var isIncomeSelected: Boolean = true

    private val activeIncomeBg = Color.parseColor("#50FF9D")
    private val activeIncomeText = Color.BLACK
    private val activeExpenseBg = Color.parseColor("#FF6E6E")
    private val activeExpenseText = Color.BLACK
    private val inactiveBg = Color.TRANSPARENT
    private val inactiveIncomeText = Color.parseColor("#50FF9D")
    private val inactiveExpenseText = Color.parseColor("#FF6E6E")

    private val incomeCategories = listOf("Salary", "Gift", "Investment")
    private val expenseCategories = listOf(
        "Food", "Transport", "Clothes", "Education", "Health", "Entertainment")

    val db = App.database
    val transactionDao = db.transactionDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_add_transaction)

        dateCard = findViewById(R.id.dateCard)
        selectDateText = findViewById(R.id.selectDateText)
        cardSpinner = findViewById(R.id.cardSpinner)
        incomeToggle = findViewById(R.id.incomeToggle)
        expenseToggle = findViewById(R.id.expenseToggle)
        typeGroup = findViewById(R.id.typeGroup)
        categorySpinner = findViewById(R.id.categorySpinner)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        amountEditText = findViewById(R.id.amountEditText)
        currencySpinner = findViewById(R.id.currencySpinner)
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)

        backButton.setOnClickListener {
            finish()
        }

        val cards = listOf("T-bank 0567", "Sber 8989", "Alfa 6666")
        val cardsAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cards) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#D0B8F5"))
                return view
            }
        }
        cardSpinner.adapter = cardsAdapter

        val currencies = listOf("RUB", "USD", "EUR")
        val currencyAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencies) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#D0B8F5"))
                return view
            }
        }
        currencySpinner.adapter = currencyAdapter

        updateDateLabel()

        dateCard.setOnClickListener {
            showDatePicker()
        }

        incomeToggle.setOnClickListener {
            isIncomeSelected = true
            updateToggleButtons("income")
            updateCategorySpinner()
        }

        expenseToggle.setOnClickListener {
            isIncomeSelected = false
            updateToggleButtons("expense")
            updateCategorySpinner()
        }

        updateToggleButtons("income")
        updateCategorySpinner()

        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }
    }

    private fun updateCategorySpinner() {
        val categories = if (isIncomeSelected) incomeCategories else expenseCategories

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#D0B8F5"))
                return view
            }
        }

        categorySpinner.adapter = adapter
    }

    private fun validateInputs(): Boolean {
        val amountText = amountEditText.text.toString().trim()
        if (amountText.isEmpty()) {
            amountEditText.error = "Please enter an amount"
            return false
        }

        try {
            val amount = amountText.toDouble()
            if (amount <= 0) {
                amountEditText.error = "Amount must be greater than 0"
                return false
            }
        } catch (e: NumberFormatException) {
            amountEditText.error = "Please enter a valid number"
            return false
        }

        return true
    }

    private fun saveTransaction() {
        val amount = amountEditText.text.toString().toDouble()
        val currency = currencySpinner.selectedItem.toString()
        val category = categorySpinner.selectedItem.toString()
        val description = descriptionEditText.text.toString().takeIf { it.isNotBlank() }
        val dateInMillis = selectedDate.timeInMillis

        val iconResId = if (isIncomeSelected) {
            when (category) {
                "Salary" -> R.drawable.ic_salary
                "Gift" -> R.drawable.ic_gift
                "Investment" -> R.drawable.ic_investment
                else -> R.drawable.ic_default
            }
        } else {
            when (category) {
                "Food" -> R.drawable.ic_food
                "Transport" -> R.drawable.ic_transport
                "Clothes" -> R.drawable.ic_clothes
                "Education" -> R.drawable.ic_education
                "Health" -> R.drawable.ic_health
                "Entertainment" -> R.drawable.ic_entertainment
                else -> R.drawable.ic_default
            }
        }

        val transaction = UserTransaction(
            isIncome = isIncomeSelected,
            amount = amount,
            currency = currency,
            category = category,
            description = description,
            date = dateInMillis,
            iconResId = iconResId
        )

        lifecycleScope.launch {
            transactionDao.insert(transaction)
        }

        finish()
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(this, { _, y, m, d ->
            selectedDate.set(y, m, d)
            updateDateLabel()
        }, year, month, day)

        dialog.show()
    }

    private fun updateDateLabel() {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        selectDateText.text = format.format(selectedDate.time)
    }

    private fun updateToggleButtons(selected: String) {
        if (selected == "income") {
            incomeToggle.setBackgroundColor(activeIncomeBg)
            incomeToggle.setTextColor(activeIncomeText)

            expenseToggle.setBackgroundColor(inactiveBg)
            expenseToggle.setTextColor(inactiveExpenseText)
        } else if (selected == "expense") {
            expenseToggle.setBackgroundColor(activeExpenseBg)
            expenseToggle.setTextColor(activeExpenseText)

            incomeToggle.setBackgroundColor(inactiveBg)
            incomeToggle.setTextColor(inactiveIncomeText)
        }
    }
}
