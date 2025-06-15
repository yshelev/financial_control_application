package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.database.entities.UserTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AuthBaseActivity() {

    private lateinit var cardsViewPager: ViewPager2
    private lateinit var transactionsRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var timeFilterSpinner: Spinner
    private lateinit var addTransactionButton: FloatingActionButton
    private lateinit var transactionsAdapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val db = App.database

        cardsViewPager = findViewById(R.id.cardsViewPager)
        transactionsRecycler = findViewById(R.id.transactionsRecycler)
        timeFilterSpinner = findViewById(R.id.timeFilterSpinner)
        addTransactionButton = findViewById(R.id.addTransactionButton)

        val timeOptions = listOf("All", "Today", "Week", "Month", "Year", "Custom period")

        val timeAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#FFFFFF"))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#FFFFFF"))
                return view
            }
        }

        timeFilterSpinner.adapter = timeAdapter

        // Инициализация адаптера с пустым списком
        transactionsAdapter = TransactionsAdapter(
            this,
            App.database.transactionDao().getAllTransactions(),
            onDeleteClicked = { transaction ->
                lifecycleScope.launch {
                    db.transactionDao().delete(transaction)
                }
            }
        )
        transactionsRecycler.layoutManager = LinearLayoutManager(this)
        transactionsRecycler.adapter = transactionsAdapter

        // Обработчик выбора периода
        timeFilterSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateTransactionsByPeriod(timeOptions[position])
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        val sampleCards = listOf(
            Card("Visa Classic", "**** 1234", "01/33", "₽", 120000.0),
            Card("Mastercard Gold", "**** 5678", "11/25", "₽", 45000.0)
        )
        cardsViewPager.adapter = CardsAdapter(sampleCards)
        cardsViewPager.setPageTransformer(CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(dpToPx(16f)))
        })
        cardsViewPager.clipToPadding = false
        cardsViewPager.clipChildren = false
        cardsViewPager.offscreenPageLimit = 3

        addTransactionButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_cards -> true
                else -> false
            }
        }

        // Загружаем все транзакции при старте
        updateTransactionsByPeriod("All")
    }

    private fun updateTransactionsByPeriod(period: String) {
        when (period) {
            "Custom period" -> showDateRangeDialog()
            else -> {
                lifecycleScope.launch {
                    val now = System.currentTimeMillis()
                    val calendar = Calendar.getInstance()

                    val transactions = when (period) {
                        "Today" -> {
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            val startOfDay = calendar.timeInMillis
                            App.database.transactionDao().getTransactionsByDateRange(startOfDay, now)
                        }
                        "Week" -> {
                            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            val startOfWeek = calendar.timeInMillis
                            App.database.transactionDao().getTransactionsByDateRange(startOfWeek, now)
                        }
                        "Month" -> {
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            val startOfMonth = calendar.timeInMillis
                            App.database.transactionDao().getTransactionsByDateRange(startOfMonth, now)
                        }
                        "Year" -> {
                            calendar.set(Calendar.DAY_OF_YEAR, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            val startOfYear = calendar.timeInMillis
                            App.database.transactionDao().getTransactionsByDateRange(startOfYear, now)
                        }
                        else -> { // "All"
                            App.database.transactionDao().getAllTransactions().collect { transactions ->
                                transactionsAdapter.updateTransactions(transactions)
                            }
                            return@launch
                        }
                    }

                    transactionsAdapter.updateTransactions(transactions)
                }
            }
        }
    }

    private fun showDateRangeDialog() {
        val calendar = Calendar.getInstance()

        // Диалог для выбора начальной даты
        val startDatePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val startCalendar = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Диалог для выбора конечной даты
                val endDatePicker = DatePickerDialog(
                    this,
                    { _, year, month, day ->
                        val endCalendar = Calendar.getInstance().apply {
                            set(year, month, day, 23, 59, 59)
                            set(Calendar.MILLISECOND, 999)
                        }

                        // Проверка что конечная дата не раньше начальной
                        if (endCalendar.before(startCalendar)) {
                            Toast.makeText(
                                this,
                                "End date cannot be before start date",
                                Toast.LENGTH_LONG
                            ).show()
                            showDateRangeDialog() // Показываем диалог снова
                        } else {
                            loadCustomPeriodTransactions(
                                startCalendar.timeInMillis,
                                endCalendar.timeInMillis
                            )
                        }
                    },
                    // Устанавливаем начальную дату как минимальную для конечной даты
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH))

                // Устанавливаем минимальную дату для конечного выбора
                endDatePicker.datePicker.minDate = startCalendar.timeInMillis
                endDatePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))

        startDatePicker.setTitle("Select start date")
        startDatePicker.show()
    }

    // Добавляем extension-функцию для форматирования даты
    fun Date.toShortDateString(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)
    }
    private fun loadCustomPeriodTransactions(startDate: Long, endDate: Long) {
        lifecycleScope.launch {
            val transactions = App.database.transactionDao()
                .getTransactionsByDateRange(startDate, endDate)
            transactionsAdapter.updateTransactions(transactions)
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        ).toInt()
    }

    fun logout() {
        authController.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}