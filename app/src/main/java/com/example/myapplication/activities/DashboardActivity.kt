package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.database.MainDatabase
import com.example.myapplication.database.entities.UserTransaction
import com.example.myapplication.mappers.toEntityList
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AuthBaseActivity() {

    private var currentFilter = "All"
    private lateinit var cardsViewPager: ViewPager2
    private lateinit var transactionsRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var timeFilterSpinner: Spinner
    private lateinit var addTransactionButton: FloatingActionButton
    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var totalBalanceTextView: TextView
    private lateinit var incomeTextView: TextView
    private lateinit var expensesTextView: TextView
    private lateinit var db: MainDatabase

    private val transactionRepository by lazy {
        (applicationContext as App).transactionRepository
    }
    private val cardRepository by lazy {
        (applicationContext as App).cardRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        currentFilter = savedInstanceState?.getString("CURRENT_FILTER", "All") ?: "All"

        db = App.database

        totalBalanceTextView = findViewById(R.id.totalBalance)
        incomeTextView = findViewById(R.id.income)
        expensesTextView = findViewById(R.id.expenses)
        cardsViewPager = findViewById(R.id.cardsViewPager)
        transactionsRecycler = findViewById(R.id.transactionsRecycler)
        timeFilterSpinner = findViewById(R.id.timeFilterSpinner)
        addTransactionButton = findViewById(R.id.addTransactionButton)

        val timeOptions = listOf("All", "Today", "Week", "Month", "Year", "Custom period")
        val selectedPosition = timeOptions.indexOf(currentFilter)
        if (selectedPosition >= 0) {
            timeFilterSpinner.setSelection(selectedPosition)
        }

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
            db.transactionDao().getAllTransactions(),
            onDeleteClicked = { transaction ->
                lifecycleScope.launch {
                    val card = db.cardDao().getCardById(transaction.cardId)
                    if (card != null) {
                        val newBalance = if (transaction.isIncome) {
                            card.balance - transaction.amount
                        } else {
                            card.balance + transaction.amount
                        }
                        db.cardDao().update(card.copy(balance = newBalance))
                    }
                    db.transactionDao().delete(transaction)
                    transactionRepository.deleteTransaction(transaction.id)
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

        cardsViewPager.adapter = CardsAdapter(
            db.cardDao().getAllCards(),
            db.transactionDao().getAllTransactions(),
            lifecycleScope,
            onAddCardClicked = {
                val intent = Intent(this@DashboardActivity, AddCardActivity::class.java)
                startActivity(intent)
            },
            onDeleteCardClicked = { card ->
                lifecycleScope.launch {
                    db.cardDao().delete(card)
                    cardRepository.deleteCard(card.id)

                    updateBalanceStats(emptyList())
                }
            }
        )

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
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }
                else -> false
            }
        }

        updateTransactionsByPeriod("All")
    }

    private fun updateTransactionsByPeriod(period: String) {
        currentFilter = period
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
                                updateBalanceStats(transactions)
                            }
                            return@launch
                        }
                    }

                    updateBalanceStats(transactions)
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


    private suspend fun updateBalanceStats(transactions: List<UserTransaction>) {
        var totalIncome = 0.0
        var totalExpenses = 0.0
        var totalBalance = 0.0

        // Считаем общий начальный баланс всех карт
        val cards = App.database.cardDao().getAllCardsOnce()
        cards.forEach { card ->
            totalBalance += card.balance
        }

        transactions.forEach { transaction ->
            if (transaction.isIncome) {
                totalIncome += transaction.amount
            } else {
                totalExpenses += transaction.amount
            }
        }

        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

        runOnUiThread {
            totalBalanceTextView.text = "Balance: ${numberFormat.format(totalBalance)}₽"
            incomeTextView.text = "Income:\n+${numberFormat.format(totalIncome)}₽"
            expensesTextView.text = "Expenses:\n-${numberFormat.format(totalExpenses)}₽"
        }
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
            updateBalanceStats(transactions)
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        ).toInt()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("CURRENT_FILTER", currentFilter)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            authController.getCurrentUser {
                user ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if (user?.email == null) {
                        Log.e("CardRefresh", "User or email not found from callback")
                        return@launch
                    }
                    val email = user.email
                    val transactionDtos = transactionRepository.getTransactions(email)
                    val transactionEntities = transactionDtos.toEntityList()
                    Log.d("dashboardAct", transactionEntities.toString())
                    db.transactionDao().refreshTransactions(transactionEntities)
                    Log.d("DashboardActivity", "Transactions successfully refreshed from API.")
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            authController.getCurrentUser {
                user ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if (user?.email == null) {
                        Log.e("CardRefresh", "User or email not found from callback")
                        return@launch
                    }
                    Log.d("DashboardActivity", "User email: ${user.email}")
                    val email = user.email

                    val cardDtos = cardRepository.getCards(email)

                    Log.d("DashboardActivity", "CardDtos count: ${cardDtos.size}")
                    val cardEntities = cardDtos.toEntityList()
                    db.cardDao().refreshCards(cardEntities)
                    Log.d("CardRefresh", "Cards updated. Count: ${cardEntities.size}")
                }
            }
        }
        lifecycleScope.launch {
            val db = App.database
            cardsViewPager.adapter = CardsAdapter(
                db.cardDao().getAllCards(),
                db.transactionDao().getAllTransactions(), // Add transactions flow
                lifecycleScope,
                onAddCardClicked = {
                    val intent = Intent(this@DashboardActivity, AddCardActivity::class.java)
                    startActivity(intent)
                },
                onDeleteCardClicked = { card ->
                    lifecycleScope.launch {
                        db.cardDao().delete(card)
                        cardRepository.deleteCard(card.id)

                        updateBalanceStats(emptyList())
                    }
                }
            )

            updateBalanceStats(emptyList())
        }

        updateTransactionsByPeriod(currentFilter)
    }

    fun logout() {
        authController.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
