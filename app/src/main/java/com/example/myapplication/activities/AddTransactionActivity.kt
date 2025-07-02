package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.database.entities.UserTransaction
import com.example.myapplication.mappers.toEntity
import com.example.myapplication.schemas.BalanceCardUpdateSchema
import com.example.myapplication.schemas.TransactionSchema
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var backButton: ImageButton

    private lateinit var createCategoryLauncher: ActivityResultLauncher<Intent>

    private var selectedDate: Calendar = Calendar.getInstance()
    private var isIncomeSelected: Boolean = true

    private var cardsMap: Map<String, Long> = emptyMap()

    private var newCategoryToSelect: String? = null

    private val activeIncomeBg = Color.parseColor("#50FF9D")
    private val activeIncomeText = Color.BLACK
    private val activeExpenseBg = Color.parseColor("#FF6E6E")
    private val activeExpenseText = Color.BLACK
    private val inactiveBg = Color.TRANSPARENT
    private val inactiveIncomeText = Color.parseColor("#50FF9D")
    private val inactiveExpenseText = Color.parseColor("#FF6E6E")

    private lateinit var incomeCategories: MutableList<String>
    private lateinit var expenseCategories: MutableList<String>

    val transactionRepository by lazy {
        (applicationContext as App).transactionRepository
    }

    val cardRepository by lazy {
        (applicationContext as App).cardRepository
    }

    private val db = App.database
    private val transactionDao = db.transactionDao()
    private val cardDao = db.cardDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

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

        incomeCategories = mutableListOf()
        expenseCategories = mutableListOf()

        updateCategoriesFromDb()

        updateDateText()

        backButton.setOnClickListener { finish() }

        createCategoryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val newCategory = result.data?.getStringExtra("newCategory")
                val iconResId = result.data?.getIntExtra("iconResId", R.drawable.ic_default)

                if (newCategory != null && iconResId != null) {
                    newCategoryToSelect = newCategory
                    updateCategoriesFromDb()
                }
            }
        }

        lifecycleScope.launch {
            cardDao.getAllCards().collect { cards ->
                cardsMap = cards.associate { "${it.name} (${it.maskedNumber})" to it.id }
                val adapter = ArrayAdapter(this@AddTransactionActivity, android.R.layout.simple_spinner_item, cardsMap.keys.toList())
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                cardSpinner.adapter = adapter
            }
        }

        dateCard.setOnClickListener { showDatePicker() }
        incomeToggle.setOnClickListener {
            isIncomeSelected = true
            updateToggleButtons()
            updateCategoriesFromDb()
        }
        expenseToggle.setOnClickListener {
            isIncomeSelected = false
            updateToggleButtons()
            updateCategoriesFromDb()
        }
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (extractCategoryName(getCategoriesList()[position]) == getString(R.string.category_add_new)) {
                    openCreateCategoryScreen()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        updateToggleButtons()
        updateCategorySpinner()

        saveButton.setOnClickListener {
            if (validateInputs()) saveTransaction()
        }
    }

    private fun getCategoriesList(): List<String> = if (isIncomeSelected) incomeCategories else expenseCategories

    private fun extractCategoryName(full: String) = full.substringBefore("|")

    private fun updateCategoriesFromDb() {
        lifecycleScope.launch {
            App.database.categoryDao()
                .getCategoriesByType(isIncomeSelected)
                .collect { categories ->
                    val list = categories.map { "${it.title}|${it.iconResId}" }.toMutableList()
                    list.add("${getString(R.string.category_add_new)}|${R.drawable.ic_default}")
                    if (isIncomeSelected) incomeCategories = list else expenseCategories = list

                    val categoriesNames = getCategoriesList().map { extractCategoryName(it) }
                    val adapter = ArrayAdapter(this@AddTransactionActivity, android.R.layout.simple_spinner_item, categoriesNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter

                    newCategoryToSelect?.let { categoryName ->
                        val index = categoriesNames.indexOfFirst { it == categoryName }
                        if (index >= 0) categorySpinner.setSelection(index)
                        newCategoryToSelect = null
                    }
                }
        }
    }

    private fun updateCategorySpinner() {
        val categories = getCategoriesList().map { extractCategoryName(it) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun openCreateCategoryScreen() {
        val intent = Intent(this, CreateCategoryActivity::class.java)
        intent.putExtra("isIncome", isIncomeSelected)
        createCategoryLauncher.launch(intent)
    }

    private fun validateInputs(): Boolean {
        val amount = amountEditText.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountEditText.error = getString(R.string.error_enter_amount)
            return false
        }
        if (cardsMap.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_no_cards), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveTransaction() {
        val amount = amountEditText.text.toString().toDouble()
        val selected = getCategoriesList()[categorySpinner.selectedItemPosition]
        val categoryName = extractCategoryName(selected)
        val description = descriptionEditText.text.toString().takeIf { it.isNotBlank() }
        val cardId = cardsMap[cardSpinner.selectedItem.toString()] ?: return

        val loaderContainer = findViewById<View>(R.id.loaderContainer)
        setUiEnabled(false)
        loaderContainer.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {

                val card = App.database.cardDao().getCardById(cardId)
                val category = App.database.categoryDao()
                    .getCategoryByNameAndType(categoryName, isIncomeSelected)

                if (card != null && category != null) {
                    val updatedBalance =
                        if (isIncomeSelected) card.balance + amount else card.balance - amount
                    cardDao.update(card.copy(balance = updatedBalance))

                    val ubs = BalanceCardUpdateSchema(
                        card_id = card.id,
                        new_balance = updatedBalance
                    )
                    try {
                        cardRepository.updateBalanceCard(ubs)
                    } catch (e: Exception) {
                        Log.d(
                            "add transaction", "cannot sync update balance with" +
                                    " backend \n error: $e"
                        )
                    }

                    val transactionSchema = TransactionSchema(
                        is_income = isIncomeSelected,
                        amount = amount,
                        description = description,
                        card_id = cardId,
                        currency = card.currency,
                        category_id = category.id,
                        date = selectedDate.timeInMillis
                    )
                    var transaction: UserTransaction
                    try {
                        val responseTransaction =
                            transactionRepository.addTransaction(transactionSchema)
                        transaction = responseTransaction.toEntity()
                    } catch (e: Exception) {
                        Log.d(
                            "add transaction", "cannot sync create transaction with" +
                                    " backend \n error: $e"
                        )

                        transaction = UserTransaction(
                            isIncome = isIncomeSelected,
                            amount = amount,
                            description = description,
                            cardId = cardId,
                            currency = card.currency,
                            categoryId = category.id,
                            date = selectedDate.timeInMillis
                        )
                    }
                    transactionDao.insert(transaction)
                    finish()
                } else {
                    Toast.makeText(
                        this@AddTransactionActivity,
                        "Ошибка: карта или категория не найдены",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    loaderContainer.visibility = View.GONE
                    setUiEnabled(true)
                }
            }
        }

    }

    private fun setUiEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1f else 0.5f
        dateCard.isEnabled = enabled
        dateCard.alpha = alpha
        cardSpinner.isEnabled = enabled
        cardSpinner.alpha = alpha
        incomeToggle.isEnabled = enabled
        incomeToggle.alpha = alpha
        expenseToggle.isEnabled = enabled
        expenseToggle.alpha = alpha
        categorySpinner.isEnabled = enabled
        categorySpinner.alpha = alpha
        descriptionEditText.isEnabled = enabled
        descriptionEditText.alpha = alpha
        amountEditText.isEnabled = enabled
        amountEditText.alpha = alpha
        backButton.isEnabled = enabled
        backButton.alpha = alpha
        saveButton.isEnabled = enabled
        saveButton.alpha = alpha
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, y, m, d ->
            selectedDate.set(y, m, d)
            updateDateText()
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateText() {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        selectDateText.text = format.format(selectedDate.time)
    }

    private fun updateToggleButtons() {
        if (isIncomeSelected) {
            incomeToggle.setBackgroundColor(activeIncomeBg)
            incomeToggle.setTextColor(activeIncomeText)
            expenseToggle.setBackgroundColor(inactiveBg)
            expenseToggle.setTextColor(inactiveExpenseText)
        } else {
            expenseToggle.setBackgroundColor(activeExpenseBg)
            expenseToggle.setTextColor(activeExpenseText)
            incomeToggle.setBackgroundColor(inactiveBg)
            incomeToggle.setTextColor(inactiveIncomeText)
        }
    }
}
