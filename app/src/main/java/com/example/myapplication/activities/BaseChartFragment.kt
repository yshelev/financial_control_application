package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.database.MainDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import java.util.Calendar


abstract class BaseChartFragment : Fragment() {
    protected abstract val chartTitle: String
    protected abstract suspend fun loadData(start: Long, end: Long): List<CategorySum>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_chart, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pieChart = view.findViewById<PieChart>(R.id.pieChart)

        val db = MainDatabase.getDatabase(requireContext().applicationContext)
        val dao = db.transactionDao()
        val start = getStartOfMonth()
        val end = getEndOfMonth()

        lifecycleScope.launch {
            val data = loadData(start, end)
            setupChart(pieChart, data, chartTitle)
        }
    }

    private fun setupChart(
        pieChart: PieChart,
        data: List<CategorySum>,
        title: String
    ) {
        val entries = data.map { PieEntry(it.category_sum.toFloat(), it.category) }

        // Тема
        val isDark = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        val palette = getCurrentThemePalette(isDark)
        val colors = List(data.size) { i -> palette.spots[i % palette.spots.size].color }

        val dataSet = PieDataSet(entries, title)
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.buttonTextColor)

        pieChart.data = PieData(dataSet)
        pieChart.isRotationEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawHoleEnabled(false)
        pieChart.animateY(500)
        pieChart.invalidate()
    }


    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun getCurrentThemePalette(isDarkTheme: Boolean): ThemePalette {
        return if (isDarkTheme) {
            // Тёмная тема с расширенной палитрой ~24 цвета
            ThemePalette(
                background = Color.parseColor("#170035"),
                spots = listOf(
                    SpotConfig(Color.parseColor("#FF7043")),
                    SpotConfig(Color.parseColor("#4DD0E1")),
                    SpotConfig(Color.parseColor("#81C784")),
                    SpotConfig(Color.parseColor("#FFB74D")),
                    SpotConfig(Color.parseColor("#9575CD")),
                    SpotConfig(Color.parseColor("#F06292")),
                    SpotConfig(Color.parseColor("#BA68C8")),
                    SpotConfig(Color.parseColor("#4FC3F7")),
                    SpotConfig(Color.parseColor("#AED581")),
                    SpotConfig(Color.parseColor("#FF8A65")),
                    SpotConfig(Color.parseColor("#90CAF9")),
                    SpotConfig(Color.parseColor("#F48FB1")),
                    SpotConfig(Color.parseColor("#CE93D8")),
                    SpotConfig(Color.parseColor("#64B5F6")),
                    SpotConfig(Color.parseColor("#A5D6A7")),
                    SpotConfig(Color.parseColor("#FFF176")),
                    SpotConfig(Color.parseColor("#81D4FA")),
                    SpotConfig(Color.parseColor("#E57373")),
                    SpotConfig(Color.parseColor("#BADE7A")),
                    SpotConfig(Color.parseColor("#FFCC80")),
                    SpotConfig(Color.parseColor("#B39DDB")),
                    SpotConfig(Color.parseColor("#4DD0E1")),
                    SpotConfig(Color.parseColor("#F48FB1")),
                    SpotConfig(Color.parseColor("#7986CB"))
                )
            )
        } else {
            // Светлая тема с расширенной палитрой ~24 цвета
            ThemePalette(
                background = Color.parseColor("#FAFAFA"),
                spots = listOf(
                    SpotConfig(Color.parseColor("#FF9E80")),
                    SpotConfig(Color.parseColor("#47D9ED")),
                    SpotConfig(Color.parseColor("#C5E1A5")),
                    SpotConfig(Color.parseColor("#FFD54F")),
                    SpotConfig(Color.parseColor("#B39DDB")),
                    SpotConfig(Color.parseColor("#F48FB1")),
                    SpotConfig(Color.parseColor("#90CAF9")),
                    SpotConfig(Color.parseColor("#81C784")),
                    SpotConfig(Color.parseColor("#FFB74D")),
                    SpotConfig(Color.parseColor("#64B5F6")),
                    SpotConfig(Color.parseColor("#A1887F")),
                    SpotConfig(Color.parseColor("#E6EE9C")),
                    SpotConfig(Color.parseColor("#FFE082")),
                    SpotConfig(Color.parseColor("#4DD0E1")),
                    SpotConfig(Color.parseColor("#AED581")),
                    SpotConfig(Color.parseColor("#7986CB")),
                    SpotConfig(Color.parseColor("#FF8A65")),
                    SpotConfig(Color.parseColor("#F06292")),
                    SpotConfig(Color.parseColor("#A5D6A7")),
                    SpotConfig(Color.parseColor("#FFF176")),
                    SpotConfig(Color.parseColor("#64B5F6")),
                    SpotConfig(Color.parseColor("#DCE775")),
                    SpotConfig(Color.parseColor("#BA68C8")),
                    SpotConfig(Color.parseColor("#81D4FA"))
                )
            )
        }
    }

    data class SpotConfig(val color: Int)
    data class ThemePalette(val background: Int, val spots: List<SpotConfig>)
}
