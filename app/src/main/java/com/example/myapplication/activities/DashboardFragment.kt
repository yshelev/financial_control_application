package com.example.myapplication

import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.currency.ExchangeRateClient
import com.example.myapplication.database.MainDatabase
import com.example.myapplication.database.entities.ExchangeRateEntity
import com.example.myapplication.database.entities.UserTransaction
import com.example.myapplication.mappers.toEntity
import com.example.myapplication.mappers.toEntityList
import com.example.myapplication.schemas.BalanceCardUpdateSchema
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class DashboardFragment : Fragment() {

    private var currentFilter = "All"
    private lateinit var cardsViewPager: ViewPager2
    private lateinit var transactionsRecycler: RecyclerView
    private lateinit var timeFilterSpinner: Spinner
    private lateinit var addTransactionButton: FloatingActionButton
    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var totalBalanceTextView: TextView
    private lateinit var db: MainDatabase
    private lateinit var incomeTextView: TextView
    private lateinit var expensesTextView: TextView
    private lateinit var addCardLauncher: ActivityResultLauncher<Intent>

    protected lateinit var authController: AuthController

    val transactionRepository by lazy {
        (requireActivity().applicationContext as App).transactionRepository
    }

    val cardRepository by lazy {
        (requireActivity().applicationContext as App).cardRepository
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentFilter = savedInstanceState?.getString("CURRENT_FILTER", "All") ?: "All"
        authController = AuthController(requireActivity(), App.database)
        db = App.database

        totalBalanceTextView = view.findViewById(R.id.totalBalance)
        incomeTextView = view.findViewById(R.id.income)
        expensesTextView = view.findViewById(R.id.expenses)
        cardsViewPager = view.findViewById(R.id.cardsViewPager)
        transactionsRecycler = view.findViewById(R.id.transactionsRecycler)
        timeFilterSpinner = view.findViewById(R.id.timeFilterSpinner)
        addTransactionButton = view.findViewById(R.id.addTransactionButton)

        val timeOptions = listOf(
            getString(R.string.filter_all),
            getString(R.string.filter_today),
            getString(R.string.filter_week),
            getString(R.string.filter_month),
            getString(R.string.filter_year),
            getString(R.string.filter_custom_period)
        )
        timeFilterSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        transactionsAdapter = TransactionsAdapter(
            viewLifecycleOwner,
            db.transactionDao().getAllTransactions(),
            db.categoryDao().getAllCategories(),
            onDeleteClicked = { transaction ->
                authController.getCurrentUser {
                    user ->
                    if (user?.email == null) {
                        lifecycleScope.launch {
                            val card = db.cardDao().getCardById(transaction.cardId)
                            if (card != null) {
                                val newBalance = if (transaction.isIncome) {
                                    card.balance.subtract(transaction.amount)
                                } else {
                                    card.balance.add(transaction.amount)
                                }

                                db.cardDao().update(card.copy(balance = newBalance))
                                db.transactionDao().delete(transaction)
                            }
                        }
                        return@getCurrentUser
                    }
                    lifecycleScope.launch {
                        val card = db.cardDao().getCardById(transaction.cardId)

                        if (card != null) {
                            val newBalance = if (transaction.isIncome) {
                                card.balance.subtract(transaction.amount)
                            } else {
                                card.balance.add(transaction.amount)
                            }

                            val ubs = BalanceCardUpdateSchema(
                                card.id,
                                new_balance = newBalance
                            )
                            try {
                                cardRepository.updateBalanceCard(ubs)
                            }
                            catch (e: Exception) {
                                Log.d("update card balance", "cant update card on server")
                            }
                            db.cardDao().update(card.copy(balance = newBalance))
                            try {
                                transactionRepository.deleteTransaction(transaction.id)
                            }
                            catch (e: Exception) {
                                Log.d("delete transaction", "cannot delete transaction on server")
                            }
                            db.transactionDao().delete(transaction)
                        }


                    }
                }
            }
        )

        transactionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        transactionsRecycler.adapter = transactionsAdapter

        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                private val backgroundPaint = Paint().apply { color = Color.RED }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val transaction = transactionsAdapter.transactions[position]
                    authController.getCurrentUser {
                        user ->
                        if (user?.email == null) {
                            lifecycleScope.launch {
                                val card = db.cardDao().getCardById(transaction.cardId)
                                if (card != null) {
                                    val newBalance = if (transaction.isIncome) {
                                        card.balance - transaction.amount
                                    } else {
                                        card.balance + transaction.amount
                                    }

                                    db.cardDao().update(card.copy(balance = newBalance))
                                    db.transactionDao().delete(transaction)
                                }
                            }
                            return@getCurrentUser
                        }
                        lifecycleScope.launch {
                            val card = db.cardDao().getCardById(transaction.cardId)

                            if (card != null) {
                                val newBalance = if (transaction.isIncome) {
                                    card.balance - transaction.amount
                                } else {
                                    card.balance + transaction.amount
                                }

                                val ubs = BalanceCardUpdateSchema(
                                    card.id,
                                    new_balance = newBalance
                                )
                                try {
                                    cardRepository.updateBalanceCard(ubs)
                                }
                                catch (e: Exception) {
                                    Log.d("update card balance", "cant update card on server")
                                }
                                db.cardDao().update(card.copy(balance = newBalance))
                                try {
                                    transactionRepository.deleteTransaction(transaction.id)
                                }
                                catch (e: Exception) {
                                    Log.d("delete transaction", "cannot delete transaction on server")
                                }
                                db.transactionDao().delete(transaction)
                            }


                        }
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float, dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val itemView = viewHolder.itemView
                    val cornerRadius = 30f  // радиус скругления

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val backgroundLeft = itemView.right + dX
                        val backgroundRight = itemView.right.toFloat()
                        val backgroundTop = itemView.top.toFloat()
                        val backgroundBottom = itemView.bottom.toFloat()

                        val alpha = (255 * (abs(dX) / itemView.width)).toInt().coerceIn(0, 255)
                        val smoothPaint = Paint().apply {
                            color = Color.argb(alpha, 255, 69, 58)
                            isAntiAlias = true
                        }

                        val rectF = android.graphics.RectF(
                            backgroundLeft,
                            backgroundTop,
                            backgroundRight,
                            backgroundBottom
                        )

                        c.drawRoundRect(rectF, cornerRadius, cornerRadius, smoothPaint)
                    }

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(transactionsRecycler)

        timeFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateTransactionsByPeriod(timeOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setupCardsViewPager()

        addCardLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                setupCardsViewPager()
                updateTransactionsByPeriod(currentFilter)
            }
        }

        addTransactionButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
        }

        updateTransactionsByPeriod(getString(R.string.filter_all))
    }

    private fun setupCardsViewPager() {
        val db = App.database

        cardsViewPager.adapter = CardsAdapter(
            db.cardDao().getAllCards(),
            db.transactionDao().getAllTransactions(),
            lifecycleScope,
            onAddCardClicked = {
                addCardLauncher.launch(Intent(requireContext(), AddCardActivity::class.java))
            },
            onDeleteCardClicked = { card ->
                authController.getCurrentUser {
                user ->
                    if (user?.email == null) {
                        lifecycleScope.launch {
                            db.cardDao().delete(card)
                            updateBalanceStats(emptyList())
                        }
                        return@getCurrentUser
                    }
                    else {
                        lifecycleScope.launch {
                            db.cardDao().delete(card)
                            updateBalanceStats(emptyList())
                        }
                        lifecycleScope.launch {
                            try {
                                cardRepository.deleteCard(card.id)
                            }
                            catch (e: Exception) {
                                Log.d("delete card", "cannot delete card on backend, \n$e")
                            }
                        }
                    }
                }
            }
        )

        val transformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(dpToPx(16f)))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f
                page.alpha = 0.5f + r * 0.5f
            }
        }

        cardsViewPager.setPageTransformer(transformer)
        cardsViewPager.clipToPadding = false
        cardsViewPager.clipChildren = false
        cardsViewPager.offscreenPageLimit = 3
    }

    private fun updateTransactionsByPeriod(period: String) {
        currentFilter = period
        when (period) {
            getString(R.string.filter_custom_period) -> showDateRangeDialog()
            else -> {
                lifecycleScope.launch {
                    val now = System.currentTimeMillis()
                    val calendar = Calendar.getInstance()

                    val transactions = when (period) {
                        getString(R.string.filter_today) -> {
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            App.database.transactionDao()
                                .getTransactionsByDateRange(calendar.timeInMillis, now)
                        }

                        getString(R.string.filter_week) -> {
                            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            App.database.transactionDao()
                                .getTransactionsByDateRange(calendar.timeInMillis, now)
                        }

                        getString(R.string.filter_month) -> {
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            App.database.transactionDao()
                                .getTransactionsByDateRange(calendar.timeInMillis, now)
                        }

                        getString(R.string.filter_year) -> {
                            calendar.set(Calendar.DAY_OF_YEAR, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            App.database.transactionDao()
                                .getTransactionsByDateRange(calendar.timeInMillis, now)
                        }

                        else -> {
                            App.database.transactionDao().getAllTransactions().collect {
                                transactionsAdapter.updateTransactions(it)
                                updateBalanceStats(it)
                            }
                            return@launch
                        }
                    }

                    transactionsAdapter.updateTransactions(transactions)
                    updateBalanceStats(transactions)
                }
            }
        }
    }

    private fun showDateRangeDialog() {
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(getString(R.string.select_date_range))
                .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first ?: return@addOnPositiveButtonClickListener
            val endDate = selection.second ?: return@addOnPositiveButtonClickListener

            lifecycleScope.launch {
                val transactions = App.database.transactionDao()
                    .getTransactionsByDateRange(startDate, endDate)
                transactionsAdapter.updateTransactions(transactions)
                updateBalanceStats(transactions)
            }
        }

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

//    private fun showDateRangeDialog() {
//        val calendar = Calendar.getInstance()
//
//        DatePickerDialog(
//            requireContext(),
//            { _, year, month, day ->
//                val startCalendar = Calendar.getInstance()
//                startCalendar.set(year, month, day, 0, 0, 0)
//                startCalendar.set(Calendar.MILLISECOND, 0)
//
//                DatePickerDialog(
//                    requireContext(),
//                    { _, endYear, endMonth, endDay ->
//                        val endCalendar = Calendar.getInstance()
//                        endCalendar.set(endYear, endMonth, endDay, 23, 59, 59)
//                        endCalendar.set(Calendar.MILLISECOND, 999)
//
//                        if (endCalendar.timeInMillis < startCalendar.timeInMillis) {
//                            Toast.makeText(
//                                requireContext(),
//                                getString(R.string.error_end_date_before_start),
//                                Toast.LENGTH_LONG
//                            ).show()
//                            return@DatePickerDialog
//                        }
//
//                        lifecycleScope.launch {
//                            val transactions = App.database.transactionDao()
//                                .getTransactionsByDateRange(
//                                    startCalendar.timeInMillis,
//                                    endCalendar.timeInMillis
//                                )
//                            transactionsAdapter.updateTransactions(transactions)
//                            updateBalanceStats(transactions)
//                        }
//                    },
//                    calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH),
//                    calendar.get(Calendar.DAY_OF_MONTH)
//                ).show()
//            },
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH)
//        ).show()
//    }

    private suspend fun convertCurrency(
        amount: BigDecimal,
        fromCurrency: String,
        toCurrency: String
    ): BigDecimal {
        val db = App.database

        try {
            val response = ExchangeRateClient.api.getRates(fromCurrency)
            Log.d("convert currency", "${response.result} ${response.rates}")

            if (response.result == "success" && response.rates != null) {
                val now = System.currentTimeMillis()
                val rates = response.rates.mapNotNull { (currency, rateAny) ->
                    val rate = rateAny as? Double ?: return@mapNotNull null
                    ExchangeRateEntity(
                        currencyCode = currency,
                        rate = rate,
                        baseCurrency = fromCurrency,
                        lastUpdated = now
                    )
                }
                db.exchangeRateDao().insertAll(rates)

                val rate = when (val rateAny = response.rates[toCurrency]) {
                    is Double -> BigDecimal.valueOf(rateAny)
                    is Int -> BigDecimal.valueOf(rateAny.toDouble())
                    else -> throw IllegalArgumentException("Currency not found or invalid rate: $toCurrency")
                }

                return amount.multiply(rate)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cachedRate = db.exchangeRateDao().getRate(fromCurrency, toCurrency)
            ?: throw RuntimeException("Нет доступа к курсу $fromCurrency → $toCurrency")

        return amount.multiply(BigDecimal.valueOf(cachedRate.rate))
    }

    private fun updateBalanceStats(transactions: List<UserTransaction>) {
        lifecycleScope.launch {
            val prefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            val userPrefCurrency = prefs.getString("PreferredCurrency", "RUB") ?: "RUB"
            val db = App.database
            var totalBalance = BigDecimal.ZERO
            var totalIncome = BigDecimal.ZERO
            var totalExpenses = BigDecimal.ZERO

            val cards = db.cardDao().getAllCardsOnce()
            cards.forEach { card ->
                val convertedAmount = if (card.currency != userPrefCurrency) {
                    convertCurrency(card.balance, card.currency, userPrefCurrency)
                } else {
                    card.balance
                }
                totalBalance = totalBalance.add(convertedAmount)
            }
            transactions.forEach { transaction ->
                val convertedAmount = if (transaction.currency != userPrefCurrency) {
                    convertCurrency(transaction.amount, transaction.currency, userPrefCurrency)
                } else {
                    transaction.amount
                }

                if (transaction.isIncome) {
                    totalIncome = totalIncome.add(convertedAmount)
                } else {
                    totalExpenses = totalExpenses.add(convertedAmount)
                }
            }

            val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                groupingSeparator = ' '
                decimalSeparator = '.'
            }

            val decimalFormat = DecimalFormat("#,###.##", symbols).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }

            val currencySymbol = getCurrencySymbol(userPrefCurrency)

            totalBalanceTextView.text = getString(
                R.string.label_balance,
                decimalFormat.format(totalBalance), currencySymbol
            )
            incomeTextView.text = getString(
                R.string.label_income,
                decimalFormat.format(totalIncome), currencySymbol
            )
            expensesTextView.text = getString(
                R.string.label_expenses,
                decimalFormat.format(totalExpenses), currencySymbol
            )
        }
    }

    private fun getCurrencySymbol(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            currencyCode
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("CURRENT_FILTER", currentFilter)
        super.onSaveInstanceState(outState)
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }
}

