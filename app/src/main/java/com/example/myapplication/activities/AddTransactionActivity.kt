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
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AuthBaseActivity() {

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

    // Хранит объекты категорий из базы (для текущего типа)
    private var categoriesList: List<com.example.myapplication.database.entities.Category> = emptyList()

    private val db = App.database
    private val transactionDao = db.transactionDao()
    private val cardDao = db.cardDao()
    private val categoryDao = db.categoryDao()

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

        updateCategoriesFromDb()

        Log.d("ON_CREATE", "Начальная дата: ${selectedDate.time}")

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
                val adapter = ArrayAdapter(
                    this@AddTransactionActivity,
                    android.R.layout.simple_spinner_item,
                    cardsMap.keys.toList()
                )
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
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Если пользователь выбрал "Добавить новую категорию"
                if (position == categoriesList.size) {
                    openCreateCategoryScreen()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        updateToggleButtons()
        updateCategorySpinner()

        saveButton.setOnClickListener {
            Log.d("SAVE_BUTTON", "Дата при нажатии Сохранить: ${selectedDate.time}")
            if (validateInputs()) saveTransaction()
        }
    }

    private fun updateCategoriesFromDb() {
        lifecycleScope.launch {
            // Загружаем категории из базы по типу
            val categoriesFromDb = categoryDao.getCategoriesByType(isIncomeSelected).first()

            categoriesList = categoriesFromDb

            // Локализуем категории по коду
            val categoryTitles = categoriesList.map { category ->
                if (!category.code.isNullOrEmpty()) {
                    val resId = resources.getIdentifier("category_${category.code}", "string", packageName)
                    if (resId != 0) getString(resId) else category.title
                } else {
                    category.title
                }
            }.toMutableList()

            // Добавляем опцию "Добавить новую категорию"
            categoryTitles.add(getString(R.string.category_add_new))

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    this@AddTransactionActivity,
                    android.R.layout.simple_spinner_item,
                    categoryTitles
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter

                // Если после создания новой категории нужно её выбрать
                newCategoryToSelect?.let { newCategoryName ->
                    val index = categoryTitles.indexOf(newCategoryName)
                    if (index >= 0) categorySpinner.setSelection(index)
                    newCategoryToSelect = null
                }
            }
        }
    }

    private fun updateCategorySpinner() {
        val categoryTitles = categoriesList.map { category ->
            if (!category.code.isNullOrEmpty()) {
                val resId = resources.getIdentifier("category_${category.code}", "string", packageName)
                if (resId != 0) getString(resId) else category.title
            } else {
                category.title
            }
        }.toMutableList()
        categoryTitles.add(getString(R.string.category_add_new))

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryTitles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun openCreateCategoryScreen() {
        val intent = Intent(this, CreateCategoryActivity::class.java)
        intent.putExtra("isIncome", isIncomeSelected)
        createCategoryLauncher.launch(intent)
    }

    private fun validateInputs(): Boolean {
        val amountText = amountEditText.text.toString()
        val amount = try {
            BigDecimal(amountText).setScale(2, RoundingMode.HALF_UP)
        } catch (e: NumberFormatException) {
            null
        }
        if (amount == null || amount <= BigDecimal.ZERO) {
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
        val description = descriptionEditText.text.toString().takeIf { it.isNotBlank() }
        val cardId = cardsMap[cardSpinner.selectedItem.toString()] ?: return
        val amount = try {
            BigDecimal(amountEditText.text.toString()).setScale(2, RoundingMode.HALF_UP)
        } catch (e: NumberFormatException) {
            amountEditText.error = getString(R.string.error_enter_amount)
            return
        }

        if (amount <= BigDecimal.ZERO) {
            amountEditText.error = getString(R.string.error_enter_amount)
            return
        }

        val selectedCardPosition = cardSpinner.selectedItemPosition
        val selectedCategoryPosition = categorySpinner.selectedItemPosition

        lifecycleScope.launch {
            val cards = cardDao.getAllCards().first()
            if (cards.isEmpty() || selectedCardPosition !in cards.indices) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddTransactionActivity, "Ошибка: карта не найдена", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val selectedCard = cards[selectedCardPosition]

            // Проверяем, что выбрана корректная категория, и не "Добавить новую"
            if (selectedCategoryPosition !in categoriesList.indices) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddTransactionActivity, "Ошибка: категория не найдена", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val selectedCategory = categoriesList[selectedCategoryPosition]

            val description = descriptionEditText.text.toString().takeIf { it.isNotBlank() }

            val loaderContainer = findViewById<View>(R.id.loaderContainer)
            withContext(Dispatchers.Main) {
                setUiEnabled(false)
                loaderContainer.visibility = View.VISIBLE
            }

            try {
                val updatedBalance = if (isIncomeSelected) selectedCard.balance + amount else selectedCard.balance - amount
                cardDao.update(selectedCard.copy(balance = updatedBalance))

                val transaction = UserTransaction(
                    isIncome = isIncomeSelected,
                    amount = amount,
                    description = description,
                    cardId = selectedCard.id,
                    currency = selectedCard.currency,
                    categoryId = selectedCategory.id,
                    date = selectedDate.timeInMillis
                )
                transactionDao.insert(transaction)

                withContext(Dispatchers.Main) {
                    finish()
                }

            } catch (e: Exception) {
                Log.e("SAVE_TRANSACTION", "Ошибка при сохранении: $e")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddTransactionActivity, "Ошибка при сохранении транзакции", Toast.LENGTH_SHORT).show()
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
        DatePickerDialog(
            this,
            { _, y, m, d ->
                selectedDate.set(Calendar.YEAR, y)
                selectedDate.set(Calendar.MONTH, m)
                selectedDate.set(Calendar.DAY_OF_MONTH, d)
                selectedDate.set(Calendar.HOUR_OF_DAY, 0)
                selectedDate.set(Calendar.MINUTE, 0)
                selectedDate.set(Calendar.SECOND, 0)
                selectedDate.set(Calendar.MILLISECOND, 0)

                Log.d("DATE_PICKER", "Выбрана дата: ${selectedDate.time}")

                updateDateText()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
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

