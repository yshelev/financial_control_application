package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.repositories.TransactionRepository

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var iconGridView: GridView
    private lateinit var saveButton: Button

    private var selectedIconResId: Int = R.drawable.ic_default // по умолчанию

    private val iconList = listOf(
        R.drawable.ic_food, R.drawable.ic_transport,
        R.drawable.ic_salary,
        R.drawable.ic_gift, R.drawable.ic_health
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        nameEditText = findViewById(R.id.categoryNameEditText)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
        iconGridView = findViewById(R.id.iconGridView)
        saveButton = findViewById(R.id.saveCategoryButton)

        iconGridView.adapter = IconAdapter(this, iconList)

        iconGridView.setOnItemClickListener { _, _, position, _ ->
            selectedIconResId = iconList[position]
            Toast.makeText(this, "Icon selected", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val type = when (typeRadioGroup.checkedRadioButtonId) {
                R.id.expenseRadioButton -> "expense"
                R.id.incomeRadioButton -> "income"
                else -> null
            }

            if (name.isBlank() || type == null) {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // TODO: Сохранить категорию в БД
            Toast.makeText(this, "Category \"$name\" saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
