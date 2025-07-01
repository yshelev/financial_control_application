package com.example.myapplication

import com.example.myapplication.dataClasses.PeriodTransaction
import com.example.myapplication.database.MainDatabase

class ExpenseFragment : BaseChartFragment() {
    override val chartTitle: String
        get() = getString(R.string.expense)
    override suspend fun loadData(start: Long, end: Long) =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getSumExpenseForChart(start, end)
    override suspend fun loadBarData(start: Long, end: Long): List<PeriodTransaction> {
        return MainDatabase.getDatabase(requireContext()).transactionDao()
            .getExpenseForMonth(start, end)
    }
    override suspend fun loadBarDataDays(start: Long, end: Long): List<PeriodTransaction> {
        return MainDatabase.getDatabase(requireContext()).transactionDao()
            .getExpenseForDays(start, end)
    }
    override suspend fun loadBarDataYears(start: Long, end: Long): List<PeriodTransaction> {
        return MainDatabase.getDatabase(requireContext()).transactionDao()
            .getExpenseForYears(start, end)
    }
}
