package com.example.cuckooclock.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AnalogClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val paintFace = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E")
        style = Paint.Style.FILL
    }
    private val paintRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6B800")
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }
    private val paintInnerRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C8960C")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val paintHourHand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F0E6D3")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 14f
    }
    private val paintMinuteHand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F0E6D3")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f
    }
    private val paintSecondHand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF4444")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 4f
    }
    private val paintTick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6B800")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val paintNumber = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6B800")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    private val paintCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF4444")
        style = Paint.Style.FILL
    }
    private val paintCenterRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6B800")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val shadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.88f

        // Gradient background for clock face
        val radialShader = RadialGradient(
            cx, cy, radius,
            intArrayOf(Color.parseColor("#252545"), Color.parseColor("#0D0D1A")),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        shadePaint.shader = radialShader
        canvas.drawCircle(cx, cy, radius, shadePaint)
        canvas.drawCircle(cx, cy, radius, paintFace.apply { style = Paint.Style.STROKE; strokeWidth = 1f })
        canvas.drawCircle(cx, cy, radius, paintRim)
        canvas.drawCircle(cx, cy, radius - 16f, paintInnerRim)

        // Hour numbers
        paintNumber.textSize = radius * 0.14f
        for (hour in 1..12) {
            val angle = Math.toRadians((hour * 30 - 90).toDouble())
            val numRadius = radius * 0.78f
            val x = cx + (numRadius * cos(angle)).toFloat()
            val y = cy + (numRadius * sin(angle)).toFloat() + paintNumber.textSize * 0.37f
            canvas.drawText(hour.toString(), x, y, paintNumber)
        }

        // Tick marks
        for (i in 0..59) {
            val angle = Math.toRadians((i * 6 - 90).toDouble())
            val isHour = i % 5 == 0
            paintTick.strokeWidth = if (isHour) 5f else 2f
            val outerR = radius - 22f
            val innerR = if (isHour) radius - 42f else radius - 32f
            canvas.drawLine(
                cx + (innerR * cos(angle)).toFloat(),
                cy + (innerR * sin(angle)).toFloat(),
                cx + (outerR * cos(angle)).toFloat(),
                cy + (outerR * sin(angle)).toFloat(),
                paintTick
            )
        }

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        // Hour hand
        val hourAngle = Math.toRadians(((hour * 30 + minute * 0.5) - 90).toDouble())
        val hourLen = radius * 0.5f
        canvas.drawLine(
            cx - (hourLen * 0.15f * cos(hourAngle)).toFloat(),
            cy - (hourLen * 0.15f * sin(hourAngle)).toFloat(),
            cx + (hourLen * cos(hourAngle)).toFloat(),
            cy + (hourLen * sin(hourAngle)).toFloat(),
            paintHourHand
        )

        // Minute hand
        val minAngle = Math.toRadians(((minute * 6 + second * 0.1) - 90).toDouble())
        val minLen = radius * 0.72f
        canvas.drawLine(
            cx - (minLen * 0.12f * cos(minAngle)).toFloat(),
            cy - (minLen * 0.12f * sin(minAngle)).toFloat(),
            cx + (minLen * cos(minAngle)).toFloat(),
            cy + (minLen * sin(minAngle)).toFloat(),
            paintMinuteHand
        )

        // Second hand
        val secAngle = Math.toRadians((second * 6 - 90).toDouble())
        val secLen = radius * 0.82f
        canvas.drawLine(
            cx - (secLen * 0.22f * cos(secAngle)).toFloat(),
            cy - (secLen * 0.22f * sin(secAngle)).toFloat(),
            cx + (secLen * cos(secAngle)).toFloat(),
            cy + (secLen * sin(secAngle)).toFloat(),
            paintSecondHand
        )

        // Center dot
        canvas.drawCircle(cx, cy, 10f, paintCenter)
        canvas.drawCircle(cx, cy, 10f, paintCenterRing)
    }
}
