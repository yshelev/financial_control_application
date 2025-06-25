package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.AddCardActivity
import com.example.myapplication.AddTransactionActivity
import com.example.myapplication.App
import com.example.myapplication.CardsAdapter
import com.example.myapplication.R
import com.example.myapplication.TransactionsAdapter
import com.example.myapplication.database.entities.UserTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var currentFilter = "All"
    private lateinit var cardsViewPager: ViewPager2
    private lateinit var transactionsRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var timeFilterSpinner: Spinner
    private lateinit var addTransactionButton: FloatingActionButton
    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var totalBalanceTextView: TextView
    private lateinit var incomeTextView: TextView
    private lateinit var expensesTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentFilter = savedInstanceState?.getString("CURRENT_FILTER", "All") ?: "All"

        val db = App.database

        totalBalanceTextView = view.findViewById(R.id.totalBalance)
        incomeTextView = view.findViewById(R.id.income)
        expensesTextView = view.findViewById(R.id.expenses)
        cardsViewPager = view.findViewById(R.id.cardsViewPager)
        transactionsRecycler = view.findViewById(R.id.transactionsRecycler)
        timeFilterSpinner = view.findViewById(R.id.timeFilterSpinner)
        addTransactionButton = view.findViewById(R.id.addTransactionButton)

        val timeOptions = listOf("All", "Today", "Week", "Month", "Year", "Custom period")
        val selectedPosition = timeOptions.indexOf(currentFilter)
        if (selectedPosition >= 0) {
            timeFilterSpinner.setSelection(selectedPosition)
        }

        val timeAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, timeOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, R.color.buttonTextColor))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, R.color.buttonTextColor))
                return view
            }
        }

        timeFilterSpinner.adapter = timeAdapter

        transactionsAdapter = TransactionsAdapter(
            viewLifecycleOwner,
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
                }
            }
        )

        transactionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        transactionsRecycler.adapter = transactionsAdapter

        timeFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateTransactionsByPeriod(timeOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        cardsViewPager.adapter = CardsAdapter(
            db.cardDao().getAllCards(),
            db.transactionDao().getAllTransactions(),
            lifecycleScope,
            onAddCardClicked = {
                startActivity(Intent(requireContext(), AddCardActivity::class.java))
            },
            onDeleteCardClicked = { card ->
                lifecycleScope.launch {
                    db.cardDao().delete(card)
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
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivity(intent)
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
                        else -> {
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

        val startDatePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val startCalendar = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val endDatePicker = DatePickerDialog(
                    requireContext(),
                    { _, year, month, day ->
                        val endCalendar = Calendar.getInstance().apply {
                            set(year, month, day, 23, 59, 59)
                            set(Calendar.MILLISECOND, 999)
                        }

                        if (endCalendar.before(startCalendar)) {
                            Toast.makeText(requireContext(), "End date cannot be before start date", Toast.LENGTH_LONG).show()
                            showDateRangeDialog()
                        } else {
                            loadCustomPeriodTransactions(startCalendar.timeInMillis, endCalendar.timeInMillis)
                        }
                    },
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH)
                )

                endDatePicker.datePicker.minDate = startCalendar.timeInMillis
                endDatePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        startDatePicker.setTitle("Select start date")
        startDatePicker.show()
    }

    private fun loadCustomPeriodTransactions(startDate: Long, endDate: Long) {
        lifecycleScope.launch {
            val transactions = App.database.transactionDao()
                .getTransactionsByDateRange(startDate, endDate)
            transactionsAdapter.updateTransactions(transactions)
            updateBalanceStats(transactions)
        }
    }

    private suspend fun updateBalanceStats(transactions: List<UserTransaction>) {
        var totalIncome = 0.0
        var totalExpenses = 0.0
        var totalBalance = 0.0

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

        activity?.runOnUiThread {
            totalBalanceTextView.text = "Balance: ${numberFormat.format(totalBalance)}₽"
            incomeTextView.text = "Income:\n+${numberFormat.format(totalIncome)}₽"
            expensesTextView.text = "Expenses:\n-${numberFormat.format(totalExpenses)}₽"
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        ).toInt()
    }

    private fun Date.toShortDateString(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val db = App.database
            cardsViewPager.adapter = CardsAdapter(
                db.cardDao().getAllCards(),
                db.transactionDao().getAllTransactions(),
                lifecycleScope,
                onAddCardClicked = {
                    startActivity(Intent(requireContext(), AddCardActivity::class.java))
                },
                onDeleteCardClicked = { card ->
                    lifecycleScope.launch {
                        db.cardDao().delete(card)
                        updateBalanceStats(emptyList())
                    }
                }
            )
            updateBalanceStats(emptyList())
        }

        updateTransactionsByPeriod(currentFilter)
    }
}

