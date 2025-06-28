package com.example.myapplication

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class GradientBackgroundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class ThemePalette(
        val background: Int,
        val spots: List<SpotConfig>
    )

    private data class SpotConfig(
        val color: Int,
        val alpha: Int = 200
    )

    private data class SpotPosition(
        val xRatio: Float,
        val yRatio: Float,
        val sizeRatio: Float
    )

    private val lightTheme = ThemePalette(
        background = Color.parseColor("#FAFAFA"),
        spots = listOf(
            SpotConfig(Color.parseColor("#FF9E80")),
            SpotConfig(Color.parseColor("#47d9ed")),
            SpotConfig(Color.parseColor("#C5E1A5")),
            SpotConfig(Color.parseColor("#FFD54F")),
            SpotConfig(Color.parseColor("#B39DDB")),
            SpotConfig(Color.parseColor("#F48FB1"))
        )
    )

    private val darkTheme = ThemePalette(
        background = Color.parseColor("#170035"),
        spots = listOf(
            SpotConfig(Color.parseColor("#FF7043")),
            SpotConfig(Color.parseColor("#4DD0E1")),
            SpotConfig(Color.parseColor("#81C784")),
            SpotConfig(Color.parseColor("#FFB74D")),
            SpotConfig(Color.parseColor("#9575CD")),
            SpotConfig(Color.parseColor("#F06292"))
        )
    )

    private val spotPositions = listOf(
        SpotPosition(0.25f, 0.60f, 0.50f),
        SpotPosition(0.22f, 0.28f, 0.42f),
        SpotPosition(0.75f, 0.65f, 0.33f),
        SpotPosition(0.85f, 0.32f, 0.30f),
        SpotPosition(0.75f, 0.10f, 0.20f),
        SpotPosition(0.40f, 0.85f, 0.28f)
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var startTime = 0L

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTime = System.currentTimeMillis()
        invalidate()
    }

    private fun isDarkTheme(): Boolean {
        val uiMode = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onDraw(canvas: Canvas) {
        val now = System.currentTimeMillis()
        val elapsed = (now - startTime) / 1000f

        val speed = 0.2f
        val progress = elapsed * 2 * Math.PI.toFloat() * speed

        val theme = if (isDarkTheme()) darkTheme else lightTheme
        canvas.drawColor(theme.background)

        for (i in spotPositions.indices) {
            val pos = spotPositions[i]
            val config = theme.spots[i]

            val cx = width * pos.xRatio
            val cy = height * pos.yRatio
            val baseRadius = width * pos.sizeRatio * 1.8f

            val offsetX = 18f * sin(progress + i)
            val offsetY = 18f * cos(progress * 1.2f + i)
            val dynamicRadius = baseRadius * (1f + 0.06f * sin(progress * 1.5f + i))

            val centerColor = Color.argb(
                config.alpha,
                Color.red(config.color),
                Color.green(config.color),
                Color.blue(config.color)
            )

            paint.shader = RadialGradient(
                cx + offsetX, cy + offsetY, dynamicRadius,
                centerColor, Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx + offsetX, cy + offsetY, dynamicRadius, paint)
        }

        paint.shader = null
        invalidate()
    }
}
