package com.example.myapplication

import android.util.Log
import com.example.myapplication.database.MainDatabase

class IncomeFragment : BaseChartFragment() {
    override val chartTitle: String
        get() = getString(R.string.income)
    override suspend fun loadData(start: Long, end: Long) =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getSumIncomeForChart(start, end)
}
