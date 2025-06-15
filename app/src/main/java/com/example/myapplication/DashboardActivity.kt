package com.example.myapplication

import Card
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
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

class DashboardActivity : AuthBaseActivity() {

    private lateinit var cardsViewPager: ViewPager2
    private lateinit var transactionsRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var timeFilterSpinner: Spinner
    private lateinit var addTransactionButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val db = App.database

        cardsViewPager = findViewById(R.id.cardsViewPager)
        transactionsRecycler = findViewById(R.id.transactionsRecycler)
        timeFilterSpinner = findViewById(R.id.timeFilterSpinner)
        addTransactionButton = findViewById(R.id.addTransactionButton)

        val timeOptions = listOf("All", "Today", "Week", "Month", "Year")

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
        val sampleCards = listOf(
            Card("Visa Classic", "**** 1234", "01/33", "₽", 120000.0),
            Card("Mastercard Gold", "**** 5678", "11/25", "₽", 45000.0)
        )
        cardsViewPager.adapter = CardsAdapter(sampleCards) {
            val intent = Intent(this, AddCardActivity::class.java)
            startActivity(intent)
        }

        cardsViewPager.setPageTransformer(CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(dpToPx(16f)))
        })
        cardsViewPager.clipToPadding = false
        cardsViewPager.clipChildren = false
        cardsViewPager.offscreenPageLimit = 3

        transactionsRecycler.layoutManager = LinearLayoutManager(this)
        val transactionDao = db.transactionDao()
        transactionsRecycler.adapter = TransactionsAdapter(
            this,
            transactionDao.getAllTransactions(),
            onDeleteClicked = { transaction ->
                lifecycleScope.launch {
                    transactionDao.delete(transaction)
                }
            }
        )

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
