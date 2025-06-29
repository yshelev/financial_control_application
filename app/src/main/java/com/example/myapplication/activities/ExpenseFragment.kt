package com.example.myapplication

import com.example.myapplication.database.MainDatabase

class ExpenseFragment : BaseChartFragment() {
    override val chartTitle: String
        get() = getString(R.string.expense)
    override suspend fun loadData(start: Long, end: Long) =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getSumExpenseForChart(start, end)
}
