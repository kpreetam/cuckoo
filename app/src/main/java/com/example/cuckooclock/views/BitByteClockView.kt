package com.example.cuckooclock.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.min

/**
 * Bit/Byte Clock — displays time as a binary grid.
 * Columns (left→right): Hours tens, Hours units, Minutes tens, Minutes units, Seconds tens, Seconds units
 * Rows (top→bottom): bit 8, bit 4, bit 2, bit 1
 * Lit dot = 1, dark dot = 0
 */
class BitByteClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val paintOn = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintOff = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E")
        style = Paint.Style.FILL
    }
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val paintLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
    }
    private val paintColLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AAAAAA")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
    }
    private val paintTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00FF88")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
        isFakeBoldText = true
    }
    private val paintDecimal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCCCCC")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
    }

    // Colors for H, M, S columns
    private val colorHours = Color.parseColor("#FF6B6B")
    private val colorMinutes = Color.parseColor("#4ECDC4")
    private val colorSeconds = Color.parseColor("#FFE66D")

    private val cols = 6
    private val rows = 4
    private val bitValues = intArrayOf(8, 4, 2, 1)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cal = Calendar.getInstance()
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        val s = cal.get(Calendar.SECOND)

        // Each column digit
        val digits = intArrayOf(h / 10, h % 10, m / 10, m % 10, s / 10, s % 10)
        val colColors = intArrayOf(colorHours, colorHours, colorMinutes, colorMinutes, colorSeconds, colorSeconds)
        val colLabels = arrayOf("H", "H", "M", "M", "S", "S")
        val decimalVals = arrayOf("${h / 10}", "${h % 10}", "${m / 10}", "${m % 10}", "${s / 10}", "${s % 10}")

        val w = width.toFloat()
        val h2 = height.toFloat()

        val titleH = h2 * 0.08f
        val labelH = h2 * 0.07f
        val bottomH = h2 * 0.10f
        val gridH = h2 - titleH - labelH - bottomH
        val dotAreaH = gridH / rows
        val dotAreaW = w / cols

        val dotR = min(dotAreaW, dotAreaH) * 0.35f
        val borderR = dotR + 4f

        // Title
        paintTitle.textSize = titleH * 0.65f
        canvas.drawText("BIT / BYTE CLOCK", w / 2f, titleH * 0.8f, paintTitle)

        // Column labels (H H M M S S)
        paintColLabel.textSize = labelH * 0.6f
        for (c in 0..cols - 1) {
            val cx = dotAreaW * c + dotAreaW / 2f
            paintColLabel.color = colColors[c]
            canvas.drawText(colLabels[c], cx, titleH + labelH * 0.7f, paintColLabel)
        }

        // Grid dots
        for (c in 0 until cols) {
            val cx = dotAreaW * c + dotAreaW / 2f
            for (r in 0 until rows) {
                val cy = titleH + labelH + dotAreaH * r + dotAreaH / 2f
                val bitOn = (digits[c] and bitValues[r]) != 0

                if (bitOn) {
                    // Glowing dot with gradient
                    val glow = RadialGradient(
                        cx, cy, dotR,
                        intArrayOf(colColors[c], adjustAlpha(colColors[c], 0.4f), Color.TRANSPARENT),
                        floatArrayOf(0f, 0.6f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    paintOn.shader = glow
                    canvas.drawCircle(cx, cy, dotR * 1.3f, paintOn)
                    paintOn.shader = null
                    paintOn.color = colColors[c]
                    canvas.drawCircle(cx, cy, dotR, paintOn)
                } else {
                    canvas.drawCircle(cx, cy, dotR, paintOff)
                    paintBorder.color = adjustAlpha(colColors[c], 0.25f)
                    canvas.drawCircle(cx, cy, borderR, paintBorder)
                }
            }
        }

        // Bit value labels on left side
        paintLabel.textSize = dotAreaH * 0.28f
        for (r in 0 until rows) {
            val cy = titleH + labelH + dotAreaH * r + dotAreaH / 2f + paintLabel.textSize * 0.37f
            canvas.drawText("${bitValues[r]}", dotAreaW * 0.18f, cy, paintLabel)
        }

        // Decimal values below grid
        paintDecimal.textSize = bottomH * 0.45f
        val decY = titleH + labelH + gridH + bottomH * 0.55f
        // Draw H : M : S
        for (c in 0 until cols) {
            val cx = dotAreaW * c + dotAreaW / 2f
            paintDecimal.color = colColors[c]
            canvas.drawText(decimalVals[c], cx, decY, paintDecimal)
        }
        // Colons
        paintDecimal.color = Color.parseColor("#666666")
        paintDecimal.textSize = bottomH * 0.4f
        canvas.drawText(":", dotAreaW * 2, decY, paintDecimal)
        canvas.drawText(":", dotAreaW * 4, decY, paintDecimal)
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val a = (Color.alpha(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color))
    }
}
