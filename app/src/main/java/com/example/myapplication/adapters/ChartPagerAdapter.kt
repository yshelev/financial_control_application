package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChartPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    // 0 — доходы, 1 — расходы
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> IncomeFragment()
            else -> ExpenseFragment()
        }
    }
}
