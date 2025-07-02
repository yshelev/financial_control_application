package com.example.myapplication

import android.util.Log
import com.example.myapplication.dataClasses.PeriodCurrencyTransaction
import com.example.myapplication.dataClasses.PeriodTransaction
import com.example.myapplication.database.MainDatabase

class IncomeFragment : BaseChartFragment() {
    override val chartTitle: String
        get() = getString(R.string.income)
    override suspend fun loadData(start: Long, end: Long): List<CategoryCurrencySum> =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getSumIncomeCurForChart(start, end)

    override suspend fun loadBarData(start: Long, end: Long): List<PeriodCurrencyTransaction> =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getCurIncomeForMonth(start, end)

    override suspend fun loadBarDataDays(start: Long, end: Long): List<PeriodCurrencyTransaction> =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getCurIncomeForDays(start, end)

    override suspend fun loadBarDataYears(start: Long, end: Long): List<PeriodCurrencyTransaction> =
        MainDatabase.getDatabase(requireContext()).transactionDao()
            .getCurIncomeForYears(start, end)

}
