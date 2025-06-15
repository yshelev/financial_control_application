package com.example.myapplication

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var dateCard: MaterialCardView
    private lateinit var selectDateText: TextView
    private lateinit var cardName: EditText
    private lateinit var incomeToggle: Button
    private lateinit var expenseToggle: Button
    private lateinit var typeGroup: MaterialButtonToggleGroup
    private lateinit var categorySpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var saveButton: Button

    private var selectedDate: Calendar = Calendar.getInstance()

    private val activeIncomeBg = Color.parseColor("#50FF9D")
    private val activeIncomeText = Color.BLACK

    private val activeExpenseBg = Color.parseColor("#FF6E6E")
    private val activeExpenseText = Color.BLACK

    private val inactiveBg = Color.TRANSPARENT
    private val inactiveIncomeText = Color.parseColor("#50FF9D")
    private val inactiveExpenseText = Color.parseColor("#FF6E6E")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_add_transaction)

        dateCard = findViewById(R.id.dateCard)
        selectDateText = findViewById(R.id.selectDateText)
        cardName = findViewById(R.id.cardName)
        incomeToggle = findViewById(R.id.incomeToggle)
        expenseToggle = findViewById(R.id.expenseToggle)
        typeGroup = findViewById(R.id.typeGroup)
        categorySpinner = findViewById(R.id.categorySpinner)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        amountEditText = findViewById(R.id.amountEditText)
        saveButton = findViewById(R.id.saveButton)

        val categories = listOf("Food", "Transport", "Clothes", "Education") // добавить поменять

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#D0B8F5"))
                return view
            }
        }

        categorySpinner.adapter = adapter


        updateDateLabel()

        dateCard.setOnClickListener {
            showDatePicker()
        }

        incomeToggle.setOnClickListener {
            updateToggleButtons("income")
        }

        expenseToggle.setOnClickListener {
            updateToggleButtons("expense")
        }

        updateToggleButtons("income")


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

