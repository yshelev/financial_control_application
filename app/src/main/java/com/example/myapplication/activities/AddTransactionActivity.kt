package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
//    private lateinit var currencySpinner: Spinner
    private lateinit var backButton: ImageButton

    private lateinit var createCategoryLauncher: ActivityResultLauncher<Intent>

    private var selectedDate: Calendar = Calendar.getInstance()
    private var isIncomeSelected: Boolean = true

    private var cardsMap: Map<String, Long> = emptyMap()

    private val activeIncomeBg = Color.parseColor("#50FF9D")
    private val activeIncomeText = Color.BLACK
    private val activeExpenseBg = Color.parseColor("#FF6E6E")
    private val activeExpenseText = Color.BLACK
    private val inactiveBg = Color.TRANSPARENT
    private val inactiveIncomeText = Color.parseColor("#50FF9D")
    private val inactiveExpenseText = Color.parseColor("#FF6E6E")

    private val incomeCategories = mutableListOf("Salary", "Gift", "Investment", "Add new category")
    private val expenseCategories = mutableListOf(
        "Food", "Transport", "Clothes", "Education", "Health", "Entertainment", "Add new category")

    val db = App.database
    val transactionDao = db.transactionDao()
    val cardDao = db.cardDao()

    private val transactionRepository by lazy {
        (applicationContext as App).transactionRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)

        // Плавное появление элементов
        listOf(
            dateCard, cardSpinner, incomeToggle, expenseToggle,
            categorySpinner, descriptionEditText, amountEditText, saveButton
        ).forEach {
            it.alpha = 0f
            it.animate().alpha(1f).setDuration(500).start()
        }

        backButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        createCategoryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val newCategory = result.data?.getStringExtra("newCategory")
                newCategory?.let {
                    if (isIncomeSelected) {
                        incomeCategories.add(incomeCategories.size - 1, it)
                    } else {
                        expenseCategories.add(expenseCategories.size - 1, it)
                    }
                    updateCategorySpinner()
                    val index = (categorySpinner.adapter as ArrayAdapter<String>).getPosition(it)
                    categorySpinner.setSelection(index)
                }
            }
        }

        lifecycleScope.launch {
            cardDao.getAllCards().collect { cards ->
                cardsMap = cards.associate { "${it.name} (${it.maskedNumber})" to it.id }
                val cardsList = cardsMap.keys.toList()
                val cardsAdapter = object : ArrayAdapter<String>(
                    this@AddTransactionActivity, android.R.layout.simple_spinner_item, cardsList
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent) as TextView
                        view.setTextColor(ContextCompat.getColor(context, R.color.buttonTextColor))
                        return view
                    }
                }
                cardSpinner.adapter = cardsAdapter
            }
        }

        updateDateLabel()
        dateCard.setOnClickListener {
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                showDatePicker()
            }.start()
        }

        incomeToggle.setOnClickListener {
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()
            isIncomeSelected = true
            updateToggleButtons("income")
            updateCategorySpinner()
        }

        expenseToggle.setOnClickListener {
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()
            isIncomeSelected = false
            updateToggleButtons("expense")
            updateCategorySpinner()
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = categorySpinner.selectedItem.toString()
                if (selected == "Add new category") {
                    openCreateCategoryScreen()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        updateToggleButtons("income")
        updateCategorySpinner()

        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            } else {
                amountEditText.animate().translationX(10f).setDuration(50).withEndAction {
                    amountEditText.animate().translationX(-10f).setDuration(50).withEndAction {
                        amountEditText.animate().translationX(0f).setDuration(50).start()
                    }.start()
                }.start()
            }
        }
    }

    private fun updateCategorySpinner() {
        val categories = if (isIncomeSelected) incomeCategories else expenseCategories

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(
                    ContextCompat.getColor(context, R.color.buttonTextColor)
                )
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun openCreateCategoryScreen() {
        val intent = Intent(this, CreateCategoryActivity::class.java)
        intent.putExtra("isIncome", isIncomeSelected)
        createCategoryLauncher.launch(intent)
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

        if (cardsMap.isEmpty()) {
            Toast.makeText(this, "No cards available", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveTransaction() {
        val amount = amountEditText.text.toString().toDouble()
//        val currency = currencySpinner.selectedItem.toString()
        val category = categorySpinner.selectedItem.toString()
        val description = descriptionEditText.text.toString().takeIf { it.isNotBlank() }
        val dateInMillis = selectedDate.timeInMillis
        val selectedCardName = cardSpinner.selectedItem.toString()

        val cardId = cardsMap[selectedCardName]

        if (cardId == null) {
            Toast.makeText(this, "Selected card not found in database", Toast.LENGTH_SHORT).show()
            return
        }

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

        lifecycleScope.launch {
            val transaction = UserTransaction(
                isIncome = isIncomeSelected,
                amount = amount,
//                currency = currency,
                category = category,
                description = description,
                date = dateInMillis,
                iconResId = iconResId,
                cardId = cardId
            )

            transactionDao.insert(transaction)
            val card = cardDao.getCardById(cardId)
            if (card != null) {
                val newBalance = if (isIncomeSelected) {
                    card.balance + amount
                } else {
                    card.balance - amount
                }
                cardDao.update(card.copy(balance = newBalance))
            }

            finish()
        }
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

