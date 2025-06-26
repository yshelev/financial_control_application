package com.example.myapplication

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var iconGridView: GridView
    private lateinit var saveButton: MaterialButton
    private lateinit var incomeToggle: MaterialButton
    private lateinit var expenseToggle: MaterialButton
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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

        // Анимации появления
        listOf(iconGridView, nameEditText, saveButton, incomeToggle, expenseToggle).forEach {
            it.alpha = 0f
            it.animate().alpha(1f).setDuration(500).start()
        }

        iconGridView.setOnItemClickListener { _, view, position, _ ->
            selectedIconResId = iconList[position]

            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
            }.start()

            Toast.makeText(this, getString(R.string.icon_selected), Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        incomeToggle.setOnClickListener {
            animateButton(it)
            isIncomeSelected = true
            updateToggleButtons("income")
        }

        expenseToggle.setOnClickListener {
            animateButton(it)
            isIncomeSelected = false
            updateToggleButtons("expense")
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()

            if (name.isEmpty()) {
                nameEditText.error = getString(R.string.enter_category_name)
                shakeView(nameEditText)
                return@setOnClickListener
            }

            val resultIntent = Intent().apply {
                putExtra("newCategory", name)
                putExtra("isIncome", isIncomeSelected)
                putExtra("iconResId", selectedIconResId)
            }

            setResult(RESULT_OK, resultIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun updateToggleButtons(selected: String) {
        if (selected == "income") {
            incomeToggle.backgroundTintList = ColorStateList.valueOf(activeIncomeBg)
            incomeToggle.setTextColor(activeIncomeText)

            expenseToggle.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            expenseToggle.setTextColor(inactiveExpenseText)
        } else {
            expenseToggle.backgroundTintList = ColorStateList.valueOf(activeExpenseBg)
            expenseToggle.setTextColor(activeExpenseText)

            incomeToggle.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            incomeToggle.setTextColor(inactiveIncomeText)
        }
    }

    private fun animateButton(button: android.view.View) {
        button.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
            button.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
    }

    private fun shakeView(view: android.view.View) {
        view.animate().translationX(10f).setDuration(50).withEndAction {
            view.animate().translationX(-10f).setDuration(50).withEndAction {
                view.animate().translationX(0f).setDuration(50).start()
            }.start()
        }.start()
    }
}
