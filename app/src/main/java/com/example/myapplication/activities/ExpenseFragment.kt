package com.example.myapplication

import com.example.myapplication.dataClasses.PeriodCurrencyTransaction
import com.example.myapplication.dataClasses.PeriodTransaction
import com.example.myapplication.database.MainDatabase

class ExpenseFragment : BaseChartFragment() {
    override val chartTitle: String
        get() = getString(R.string.expense)
    override suspend fun loadData(start: Long, end: Long): List<CategoryCurrencySum> =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getSumExpenseCurForChart(start, end)

    override suspend fun loadBarData(start: Long, end: Long): List<PeriodCurrencyTransaction> =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getCurExpenseForMonth(start, end)

    override suspend fun loadBarDataDays(start: Long, end: Long): List<PeriodCurrencyTransaction> =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getCurExpenseForDays(start, end)

    override suspend fun loadBarDataYears(start: Long, end: Long): List<PeriodCurrencyTransaction> =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getCurExpenseForYears(start, end)

}
