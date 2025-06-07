package com.example.myapplication
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2

class DashboardActivity : AppCompatActivity() {

    private lateinit var cardsViewPager: ViewPager2
    private lateinit var transactionsRecycler: androidx.recyclerview.widget.RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        cardsViewPager = findViewById(R.id.cardsViewPager)
        transactionsRecycler = findViewById(R.id.transactionsRecycler)

        val sampleCards = listOf(
            Card("Visa Classic", "**** 1234", "01/33","₽", 120000.0),
            Card("Mastercard Gold", "**** 5678", "11/25","₽", 45000.0)
        )

        val sampleTransactions = listOf(
            Transaction("Products", "June 5", 3200, false, R.drawable.ic_eye),
            Transaction("Salary", "June 1", 45000, true, R.drawable.ic_eye),
            Transaction("Education", "May 31", 599, false, R.drawable.ic_eye)
        )

        cardsViewPager.adapter = CardsAdapter(sampleCards)

        val pageMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(pageMarginPx))
        cardsViewPager.setPageTransformer(compositePageTransformer)

        cardsViewPager.clipToPadding = false
        cardsViewPager.clipChildren = false
        cardsViewPager.offscreenPageLimit = 3


        transactionsRecycler.layoutManager = LinearLayoutManager(this)
        transactionsRecycler.adapter = TransactionsAdapter(sampleTransactions)
    }
}
