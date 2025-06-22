package com.example.myapplication

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.View

class GradientBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
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

    private data class GradientData(
        val shader: RadialGradient,
        val cx: Float,
        val cy: Float,
        val radius: Float
    )

    private val lightTheme = ThemePalette(
        background = Color.parseColor("#FAFAFA"),
        spots = listOf(
            SpotConfig(Color.parseColor("#FF9E80")),
            SpotConfig(Color.parseColor("#80DEEA")),
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

    private var lightGradients: List<GradientData> = emptyList()
    private var darkGradients: List<GradientData> = emptyList()

    private var currentDrawable: BitmapDrawable? = null
    private var lastDarkTheme: Boolean? = null

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private fun isDarkTheme(): Boolean {
        val uiMode = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        lightGradients = buildGradients(w, h, lightTheme, smooth = false)
        darkGradients = buildGradients(w, h, darkTheme, smooth = true)
        regenerateCurrentDrawable(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        val isDark = isDarkTheme()

        if (lastDarkTheme == null) {
            regenerateCurrentDrawable(width, height)
            currentDrawable?.draw(canvas)
            lastDarkTheme = isDark
            return
        }

        if (lastDarkTheme != isDark) {
            val oldDrawable = currentDrawable
            val newBitmap = generateBitmap(width, height, if (isDark) darkTheme else lightTheme)
            val newDrawable = BitmapDrawable(resources, newBitmap).apply {
                setBounds(0, 0, width, height)
            }

            val transition = TransitionDrawable(arrayOf(oldDrawable, newDrawable)).apply {
                isCrossFadeEnabled = true
                setBounds(0, 0, width, height)
            }

            currentDrawable = newDrawable
            transition.startTransition(600)
            transition.draw(canvas)
            lastDarkTheme = isDark
        } else {
            currentDrawable?.draw(canvas)
        }
    }

    private fun regenerateCurrentDrawable(w: Int, h: Int) {
        val theme = if (isDarkTheme()) darkTheme else lightTheme
        val bitmap = generateBitmap(w, h, theme)
        currentDrawable = BitmapDrawable(resources, bitmap).apply {
            setBounds(0, 0, w, h)
        }
        lastDarkTheme = isDarkTheme()
    }

    private fun generateBitmap(w: Int, h: Int, theme: ThemePalette): Bitmap {
        val gradients = if (theme == darkTheme) darkGradients else lightGradients
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(theme.background)

        gradients.forEach { data ->
            paint.shader = data.shader
            canvas.drawCircle(data.cx, data.cy, data.radius, paint)
        }
        paint.shader = null

        return bitmap
    }

    private fun buildGradients(
        width: Int,
        height: Int,
        palette: ThemePalette,
        smooth: Boolean
    ): List<GradientData> {
        return spotPositions.mapIndexed { index, pos ->
            val config = palette.spots[index]
            val baseAlpha = (config.alpha + if (palette == darkTheme) 3 else 5).coerceIn(0, 255)

            val cx = width * pos.xRatio
            val cy = height * pos.yRatio
            val radius = width * pos.sizeRatio * if (smooth) 1.5f else 1.2f

            val centerColor = Color.argb(
                baseAlpha,
                Color.red(config.color),
                Color.green(config.color),
                Color.blue(config.color)
            )

            val shader = RadialGradient(
                cx, cy, radius,
                centerColor, Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )

            GradientData(shader, cx, cy, radius)
        }
    }
}
