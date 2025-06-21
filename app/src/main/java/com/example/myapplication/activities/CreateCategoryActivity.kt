package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var iconGridView: GridView
    private lateinit var saveButton: Button
    private lateinit var incomeToggle: Button
    private lateinit var expenseToggle: Button
    private lateinit var typeGroup: MaterialButtonToggleGroup

    private val activeIncomeBg = Color.parseColor("#50FF9D")
    private val activeIncomeText = Color.BLACK
    private val activeExpenseBg = Color.parseColor("#FF6E6E")
    private val activeExpenseText = Color.BLACK
    private val inactiveBg = Color.TRANSPARENT
    private val inactiveIncomeText = Color.parseColor("#50FF9D")
    private val inactiveExpenseText = Color.parseColor("#FF6E6E")

    private var selectedIconResId: Int = R.drawable.ic_default
    private var isIncomeSelected: Boolean = true

    private val iconList = listOf(
        R.drawable.ic_food, R.drawable.ic_transport,
        R.drawable.ic_salary, R.drawable.ic_gift, R.drawable.ic_health, R.drawable.ic_airport,
        R.drawable.ic_bar, R.drawable.ic_cafe, R.drawable.ic_car_wash, R.drawable.ic_clothes,
        R.drawable.ic_dining, R.drawable.ic_education, R.drawable.ic_entertainment,
        R.drawable.ic_flower, R.drawable.ic_game, R.drawable.ic_gas_station, R.drawable.ic_hospital,
        R.drawable.ic_investment, R.drawable.ic_phone, R.drawable.ic_store, R.drawable.ic_attractions,
        R.drawable.ic_cake, R.drawable.ic_child, R.drawable.ic_church, R.drawable.ic_roller,
        R.drawable.ic_selebration, R.drawable.ic_sports
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        nameEditText = findViewById(R.id.categoryNameEditText)
        iconGridView = findViewById(R.id.iconGridView)
        saveButton = findViewById(R.id.saveCategoryButton)
        incomeToggle = findViewById(R.id.incomeToggle)
        expenseToggle = findViewById(R.id.expenseToggle)
        typeGroup = findViewById(R.id.typeGroup)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        isIncomeSelected = intent.getBooleanExtra("isIncome", true)
        updateToggleButtons(if (isIncomeSelected) "income" else "expense")

        iconGridView.adapter = IconAdapter(this, iconList)

        iconGridView.setOnItemClickListener { _, _, position, _ ->
            selectedIconResId = iconList[position]
            Toast.makeText(this, "Icon selected", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            finish()
        }

        incomeToggle.setOnClickListener {
            isIncomeSelected = true
            updateToggleButtons("income")
        }

        expenseToggle.setOnClickListener {
            isIncomeSelected = false
            updateToggleButtons("expense")
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()

            if (name.isEmpty()) {
                nameEditText.error = "Please enter category name"
                return@setOnClickListener
            }

            // Возвращаем результат в AddTransactionActivity
            val resultIntent = Intent()
            resultIntent.putExtra("newCategory", name)
            resultIntent.putExtra("isIncome", isIncomeSelected)
            resultIntent.putExtra("iconResId", selectedIconResId)

            setResult(RESULT_OK, resultIntent)
            finish()
        }
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