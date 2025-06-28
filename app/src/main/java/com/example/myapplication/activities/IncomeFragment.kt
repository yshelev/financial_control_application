package com.example.myapplication

import com.example.myapplication.database.MainDatabase

class IncomeFragment : BaseChartFragment() {
    override val chartTitle = "Доходы"
    override suspend fun loadData(start: Long, end: Long) =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getSumIncomeForChart(start, end)
}
