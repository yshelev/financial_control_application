package com.example.myapplication

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.database.MainDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.launch
import java.util.Calendar
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContentProviderCompat.requireContext
import com.github.mikephil.charting.utils.Utils
import java.util.TimeZone


abstract class BaseChartFragment : Fragment() {
    protected abstract val chartTitle: String
    protected abstract suspend fun loadData(start: Long, end: Long): List<CategorySum>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_chart, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = MainDatabase.getDatabase(requireContext().applicationContext)
        val dao = db.transactionDao()
        val loader = view.findViewById<View>(R.id.chartLoader)
        val pieChart = view.findViewById<PieChart>(R.id.pieChart)
        val dateCard = view.findViewById<View>(R.id.dateCard)
        val dateText = view.findViewById<TextView>(R.id.selectDateText)

        val start = getStartOfMonth()
        val end = getEndOfMonth()
        loadAndDisplayChart(start, end, pieChart, loader)

        dateCard.setOnClickListener {
            val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText(getString(R.string.select_date_range))
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val startDateUtc = selection.first ?: return@addOnPositiveButtonClickListener
                val endDateUtc = selection.second ?: return@addOnPositiveButtonClickListener

                // Преобразуем из UTC в локальное время и выставляем границы дней
                val localStartDate = getStartOfDayInLocal(startDateUtc)
                val localEndDate = getEndOfDayInLocal(endDateUtc - 1)

                val formatted = formatDate(localStartDate) + " – " + formatDate(localEndDate)
                dateText.text = formatted

                loadAndDisplayChart(localStartDate, localEndDate, pieChart, loader)
                loadAndDisplayChart(localStartDate, localEndDate, pieChart, loader)

            }
            picker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun getStartOfDayInLocal(utcMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = utcMillis
        // Смещаем на текущий часовой пояс
        val tzOffset = cal.timeZone.getOffset(cal.timeInMillis)
        cal.timeInMillis += tzOffset

        // Устанавливаем 00:00:00.000
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Возвращаем обратно в UTC
        return cal.timeInMillis
    }

    // Функция для конца дня в локальном времени
    private fun getEndOfDayInLocal(utcMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = utcMillis
        val tzOffset = cal.timeZone.getOffset(cal.timeInMillis)
        cal.timeInMillis += tzOffset

        // Устанавливаем 23:59:59.999
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)

        return cal.timeInMillis
    }


    private fun setupChart(
        pieChart: PieChart,
        data: List<CategorySum>,
        title: String
    ) {
        pieChart.setNoDataText("")

        val entries = data.map { PieEntry(it.category_sum.toFloat(), it.category) }

        // Тема
        val isDark = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        val palette = getCurrentThemePalette(isDark)
        val shuffledColors = palette.spots.map { it.color }.shuffled()
        val colors = List(data.size) { i -> shuffledColors[i % shuffledColors.size] }

        val dataSet = PieDataSet(entries, title)
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.buttonTextColor)

        pieChart.data = PieData(dataSet)
        pieChart.setUsePercentValues(true)
        pieChart.isRotationEnabled = false
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.buttonTextColor))
        pieChart.setDrawHoleEnabled(false)
        pieChart.animateY(500)

        pieChart.renderer = FramedPieChartRenderer(
            pieChart,
            pieChart.animator,
            pieChart.viewPortHandler
        )

        pieChart.invalidate()

        renderCustomLegend(data, colors)
    }

    private fun loadAndDisplayChart(start: Long, end: Long, pieChart: PieChart, loader: View) {
        lifecycleScope.launch {
            loader.visibility = View.VISIBLE
            pieChart.visibility = View.INVISIBLE

            val data = loadData(start, end)

            loader.visibility = View.GONE
            pieChart.visibility = View.VISIBLE

            setupChart(pieChart, data, chartTitle)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        return "$day.$month"
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
                    SpotConfig(Color.parseColor("#FF6F61")), // красно-оранжевый
                    SpotConfig(Color.parseColor("#00BCD4")), // яркий голубой
                    SpotConfig(Color.parseColor("#66BB6A")), // ярко-зелёный
                    SpotConfig(Color.parseColor("#FFA726")), // яркий оранжевый
                    SpotConfig(Color.parseColor("#7E57C2")), // яркий фиолетовый
                    SpotConfig(Color.parseColor("#EC407A")), // малиновый
                    SpotConfig(Color.parseColor("#AB47BC")), // сиреневый
                    SpotConfig(Color.parseColor("#29B6F6")), // небесный
                    SpotConfig(Color.parseColor("#9CCC65")), // оливковый
                    SpotConfig(Color.parseColor("#FF7043")), // коралловый
                    SpotConfig(Color.parseColor("#42A5F5")), // насыщенный синий
                    SpotConfig(Color.parseColor("#F06292")), // розовый
                    SpotConfig(Color.parseColor("#BA68C8")), // пастельно-фиолетовый
                    SpotConfig(Color.parseColor("#5C6BC0")), // сине-фиолетовый
                    SpotConfig(Color.parseColor("#26C6DA")), // бирюзовый
                    SpotConfig(Color.parseColor("#D4E157")), // жёлто-зелёный
                    SpotConfig(Color.parseColor("#26A69A")), // морская волна
                    SpotConfig(Color.parseColor("#FFCA28")), // насыщенно-жёлтый
                    SpotConfig(Color.parseColor("#EF5350")), // яркий красный
                    SpotConfig(Color.parseColor("#00E5FF")), // лазурный
                    SpotConfig(Color.parseColor("#81D4FA")), // светло-голубой
                    SpotConfig(Color.parseColor("#9575CD")), // мягкий фиолетовый
                    SpotConfig(Color.parseColor("#4DB6AC")), // тёмная мята
                    SpotConfig(Color.parseColor("#F48FB1"))  // розово-серый
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
                    SpotConfig(Color.parseColor("#26C6DA")), // 💧 заменённый цвет
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

    private fun renderCustomLegend(data: List<CategorySum>, colors: List<Int>) {
        val container = view?.findViewById<FlexboxLayout>(R.id.legendContainer) ?: return
        container.removeAllViews()

        for (i in data.indices) {
            val category = data[i].category
            val baseColor = colors[i % colors.size]
            val color = (baseColor and 0x00FFFFFF) or (0xAA shl 24) // прозрачность 170/255 (~67%)

            val chip = layoutInflater.inflate(R.layout.item_legend_chip, container, false) as TextView
            chip.text = category

            val drawable = chip.background.mutate()
            if (drawable is GradientDrawable) {
                drawable.setColor(color)
            } else {
                chip.setBackgroundColor(color)
            }

            container.addView(chip)
        }
    }

    data class SpotConfig(val color: Int)
    data class ThemePalette(val background: Int, val spots: List<SpotConfig>)

    inner class BorderedValueFormatter : ValueFormatter() {
        override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
            return "${value.toInt()}"
        }
    }

    inner class FramedPieChartRenderer(
        chart: PieChart,
        animator: com.github.mikephil.charting.animation.ChartAnimator,
        viewPortHandler: com.github.mikephil.charting.utils.ViewPortHandler
    ) : PieChartRenderer(chart, animator, viewPortHandler) {

        private val boxPaint = Paint().apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(requireContext(), R.color.transparent) // Полупрозрачный чёрный фон
            isAntiAlias = true
        }

        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(requireContext(), R.color.buttonTextColor)
            textSize = Utils.convertDpToPixel(12f)
        }

        override fun drawValues(c: Canvas) {
            val center = mChart.centerCircleBox
            val radius = mChart.radius

            val rotationAngle = mChart.rotationAngle
            val drawAngles = mChart.drawAngles
            val absoluteAngles = mChart.absoluteAngles

            val data = mChart.data
            val dataSets = data.dataSets

            var angle = 0f

            for (i in dataSets.indices) {
                val dataSet = dataSets[i]

                for (j in 0 until dataSet.entryCount) {
                    val entry = dataSet.getEntryForIndex(j) as PieEntry
                    val value = dataSet.getEntryForIndex(j).y

                    val offset = drawAngles[j] / 2f
                    val sliceAngle = drawAngles[j]
                    val transformedAngle = rotationAngle + angle + offset

                    val sliceXBase = Math.cos(Math.toRadians(transformedAngle.toDouble())).toFloat()
                    val sliceYBase = Math.sin(Math.toRadians(transformedAngle.toDouble())).toFloat()

                    val label = "${entry.label}\n${"%.1f".format(value)}%"

                    val x = center.x + radius / 1.8f * sliceXBase
                    val y = center.y + radius / 1.8f * sliceYBase

                    // Рамка
                    val padding = Utils.convertDpToPixel(6f)
                    val textBounds = RectF()
                    val lines = label.split("\n")
                    val width = lines.maxOf { textPaint.measureText(it) }
                    val height = textPaint.textSize * lines.size + padding

                    textBounds.set(
                        x - width / 2 - padding,
                        y - height / 2 - padding / 2,
                        x + width / 2 + padding,
                        y + height / 2 + padding / 2
                    )

                    c.drawRoundRect(textBounds, 16f, 16f, boxPaint)

                    // Текст
                    lines.forEachIndexed { idx, line ->
                        c.drawText(
                            line,
                            x,
                            y + textPaint.textSize * (idx - 0.5f),
                            textPaint.apply { textAlign = Paint.Align.CENTER }
                        )
                    }

                    angle += sliceAngle
                }
            }
        }
    }

}
