package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.database.entities.UserTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class DashboardActivity : AuthBaseActivity() {

    private lateinit var cardsViewPager: ViewPager2
    private lateinit var transactionsRecycler: androidx.recyclerview.widget.RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val db = App.database
        val transactions = db.transactionDao().getAllTransactions().asLiveData()

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val trans = UserTransaction(isIncome =  true, amount =  3200.0, currency =  "Рубли", category =  "food", description = "1", iconResId =  R.drawable.ic_eye)
            lifecycleScope.launch {
                db.transactionDao().insert(trans)
            }
        }

        cardsViewPager = findViewById(R.id.cardsViewPager)
        transactionsRecycler = findViewById(R.id.transactionsRecycler)

        val sampleCards = listOf(
            Card("Visa Classic", "**** 1234", "01/33", "₽", 120000.0),
            Card("Mastercard Gold", "**** 5678", "11/25", "₽", 45000.0)
        )

        val sampleTransactions = listOf(
            UserTransaction(isIncome =  true, amount =  3200.0, currency =  "Рубли", category =  "food", description = "1", iconResId =  R.drawable.ic_eye),
            UserTransaction(isIncome =  true, amount =  3200.0, currency =  "Рубли", category =  "food", description = "1", iconResId =  R.drawable.ic_eye),
            UserTransaction(isIncome =  true, amount =  3200.0, currency =  "Рубли", category =  "food", description = "1", iconResId =  R.drawable.ic_eye),
        )

        cardsViewPager.adapter = CardsAdapter(sampleCards)

        val pageMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics
        ).toInt()

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(pageMarginPx))
        cardsViewPager.setPageTransformer(compositePageTransformer)

        cardsViewPager.clipToPadding = false
        cardsViewPager.clipChildren = false
        cardsViewPager.offscreenPageLimit = 3


        transactionsRecycler.layoutManager = LinearLayoutManager(this)
        transactionsRecycler.adapter = TransactionsAdapter(this, db.transactionDao().getAllTransactions())

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> { /* TODO: open home */ true
                }

                R.id.nav_cards -> {
                    true
                }

                R.id.nav_settings -> { /* TODO: open settings */ true
                }

                else -> false
            }
        }

    }

    fun logout() {
        authController.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
