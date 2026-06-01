package com.example.cuckooclock.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.min

class CuckooAnimationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var doorOpenAmount = 0f
    private var birdOutAmount = 0f
    private var birdBobOffset = 0f
    private var beakOpenAmount = 0f
    private var isAnimating = false

    private val woodDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A2800")
        style = Paint.Style.FILL
    }
    private val woodMedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B3A00")
        style = Paint.Style.FILL
    }
    private val woodLightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8B5A00")
        style = Paint.Style.FILL
    }
    private val woodGrainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3A2000")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        alpha = 80
    }
    private val roofPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2D1600")
        style = Paint.Style.FILL
    }
    private val roofEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A0D00")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val doorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3D2000")
        style = Paint.Style.FILL
    }
    private val doorOpenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A0A00")
        style = Paint.Style.FILL
    }
    private val doorBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C8960C")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2D6A2D")
        style = Paint.Style.FILL
    }
    private val leafDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A4A1A")
        style = Paint.Style.FILL
    }
    private val berryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC2200")
        style = Paint.Style.FILL
    }
    private val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6B800")
        style = Paint.Style.FILL
    }
    private val goldStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6B800")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val birdBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        style = Paint.Style.FILL
    }
    private val birdWingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        style = Paint.Style.FILL
    }
    private val birdBellyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCAA88")
        style = Paint.Style.FILL
    }
    private val birdBeakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFB300")
        style = Paint.Style.FILL
    }
    private val birdEyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val birdEyeHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val chainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C8960C")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val weightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8B7355")
        style = Paint.Style.FILL
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A0D00")
        style = Paint.Style.FILL
        alpha = 60
    }
    private val pendulumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C8960C")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val pendulumBobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6B800")
        style = Paint.Style.FILL
    }

    private var pendulumAngle = 0f
    private var pendulumAnimator: ValueAnimator? = null

    init {
        startPendulum()
    }

    private fun startPendulum() {
        pendulumAnimator = ValueAnimator.ofFloat(-18f, 18f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                pendulumAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun animateCuckoo(count: Int) {
        if (isAnimating) return
        isAnimating = true
        doChimeSequence(count, 0)
    }

    private fun doChimeSequence(total: Int, current: Int) {
        if (current >= total) {
            animateDoor(false) {
                isAnimating = false
                invalidate()
            }
            return
        }
        if (current == 0) {
            animateDoor(true) {
                animateBirdOut {
                    animateBob {
                        animateBirdIn {
                            doChimeSequence(total, current + 1)
                        }
                    }
                }
            }
        } else {
            animateBirdOut {
                animateBob {
                    animateBirdIn {
                        doChimeSequence(total, current + 1)
                    }
                }
            }
        }
    }

    private fun animateDoor(open: Boolean, onDone: () -> Unit) {
        ValueAnimator.ofFloat(if (open) 0f else 1f, if (open) 1f else 0f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                doorOpenAmount = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { onDone() }
            })
            start()
        }
    }

    private fun animateBirdOut(onDone: () -> Unit) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200
            interpolator = OvershootInterpolator(1.5f)
            addUpdateListener {
                birdOutAmount = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { onDone() }
            })
            start()
        }
    }

    private fun animateBob(onDone: () -> Unit) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 500
            addUpdateListener { anim ->
                val t = anim.animatedFraction
                birdBobOffset = -8f * Math.sin(Math.PI * t * 2).toFloat()
                beakOpenAmount = if (t < 0.5f) t * 2f else (1f - t) * 2f
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    birdBobOffset = 0f
                    beakOpenAmount = 0f
                    onDone()
                }
            })
            start()
        }
    }

    private fun animateBirdIn(onDone: () -> Unit) {
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 200
            addUpdateListener {
                birdOutAmount = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { onDone() }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val scale = min(w, h) / 320f

        canvas.save()
        canvas.translate(cx, h * 0.08f)
        canvas.scale(scale, scale)
        drawClock(canvas)
        canvas.restore()
    }

    private fun drawClock(canvas: Canvas) {
        val cw = 110f
        val ch = 160f

        canvas.drawLine(-20f, ch * 0.6f, -20f, ch * 1.1f, chainPaint)
        canvas.drawLine(20f, ch * 0.6f, 20f, ch * 1.1f, chainPaint)
        drawPinecone(canvas, -20f, ch * 1.1f, 10f)
        drawPinecone(canvas, 20f, ch * 1.1f, 10f)

        canvas.save()
        canvas.translate(0f, ch * 0.75f)
        canvas.rotate(pendulumAngle)
        canvas.drawLine(0f, 0f, 0f, 70f, pendulumPaint)
        canvas.drawCircle(0f, 75f, 12f, pendulumBobPaint)
        canvas.drawCircle(0f, 75f, 12f, goldStrokePaint)
        canvas.restore()

        val bodyRect = RectF(-cw, 0f, cw, ch)
        canvas.drawRoundRect(bodyRect, 8f, 8f, woodMedPaint)

        for (i in 0..8) {
            val y = i * ch / 8f
            canvas.drawLine(-cw + 4f, y, cw - 4f, y, woodGrainPaint)
        }

        canvas.drawRect(-cw, 0f, -cw + 8f, ch, woodDarkPaint)
        canvas.drawRect(cw - 8f, 0f, cw, ch, woodDarkPaint)

        drawRoof(canvas, cw, ch)

        drawLeafDecoration(canvas, -cw + 20f, ch * 0.55f)
        drawLeafDecoration(canvas, cw - 20f, ch * 0.55f)

        val faceR = 38f
        val faceCy = ch * 0.38f
        canvas.drawCircle(0f, faceCy, faceR + 4f, woodDarkPaint)
        canvas.drawCircle(0f, faceCy, faceR, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F5E6C8")
            style = Paint.Style.FILL
        })
        for (i in 0..11) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val outerR = faceR - 4f
            val innerR = faceR - 10f
            canvas.drawLine(
                (innerR * Math.cos(angle)).toFloat(),
                faceCy + (innerR * Math.sin(angle)).toFloat(),
                (outerR * Math.cos(angle)).toFloat(),
                faceCy + (outerR * Math.sin(angle)).toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#4A2800")
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                }
            )
        }
        canvas.drawCircle(0f, faceCy, 3f, woodDarkPaint)

        drawDoor(canvas, ch)

        canvas.drawRoundRect(bodyRect, 8f, 8f, goldStrokePaint.apply { strokeWidth = 2f })

        if (birdOutAmount > 0f) {
            drawBird(canvas, ch)
        }
    }

    private fun drawRoof(canvas: Canvas, cw: Float, ch: Float) {
        val roofPath = Path().apply {
            moveTo(-cw - 18f, 0f)
            lineTo(0f, -70f)
            lineTo(cw + 18f, 0f)
            close()
        }
        canvas.drawPath(roofPath, roofPaint)
        canvas.drawPath(roofPath, roofEdgePaint)

        for (layer in 0..2) {
            val t = layer / 3f
            val lx = (cw + 18f) * (1f - t * 0.6f)
            val ly = -70f * t
            val roofLayer = Path().apply {
                moveTo(-lx, ly)
                lineTo(0f, ly - 70f * (1f - t))
                lineTo(lx, ly)
            }
            canvas.drawPath(roofLayer, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1A0D00")
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                alpha = 120
            })
        }

        canvas.drawCircle(0f, -72f, 6f, woodLightPaint)
        canvas.drawCircle(0f, -72f, 6f, goldStrokePaint)

        drawLeafCluster(canvas, -cw - 10f, -10f)
        drawLeafCluster(canvas, cw + 10f, -10f)
    }

    private fun drawLeafCluster(canvas: Canvas, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        canvas.drawOval(RectF(-8f, -12f, 8f, 0f), leafPaint)
        canvas.drawOval(RectF(-12f, -8f, 0f, 4f), leafDarkPaint)
        canvas.drawOval(RectF(0f, -8f, 12f, 4f), leafDarkPaint)
        canvas.drawCircle(-4f, -2f, 3f, berryPaint)
        canvas.drawCircle(4f, -2f, 3f, berryPaint)
        canvas.restore()
    }

    private fun drawLeafDecoration(canvas: Canvas, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        canvas.drawOval(RectF(-6f, -20f, 6f, 0f), leafPaint)
        canvas.drawOval(RectF(-10f, -14f, 2f, 4f), leafDarkPaint)
        canvas.drawOval(RectF(-2f, -14f, 10f, 4f), leafDarkPaint)
        canvas.drawCircle(0f, 0f, 3f, berryPaint)
        canvas.restore()
    }

    private fun drawPinecone(canvas: Canvas, x: Float, y: Float, r: Float) {
        canvas.save()
        canvas.translate(x, y)
        for (i in 0..3) {
            val s = 1f - i * 0.15f
            canvas.drawOval(RectF(-r * s, i * r * 0.4f - r * 0.2f,
                r * s, i * r * 0.4f + r * 0.5f), weightPaint)
        }
        canvas.drawOval(RectF(-r * 0.3f, -r * 0.5f, r * 0.3f, 0f), woodDarkPaint)
        canvas.restore()
    }

    private fun drawDoor(canvas: Canvas, ch: Float) {
        val doorW = 28f
        val doorH = 32f
        val doorX = -doorW / 2f
        val doorY = ch * 0.58f

        canvas.drawRoundRect(RectF(doorX, doorY, doorX + doorW, doorY + doorH),
            4f, 4f, doorOpenPaint)

        if (doorOpenAmount < 1f) {
            val closedW = doorW * (1f - doorOpenAmount)
            val leftDoor = Path().apply {
                moveTo(doorX, doorY)
                lineTo(doorX + closedW * 0.5f, doorY + doorOpenAmount * 4f)
                lineTo(doorX + closedW * 0.5f, doorY + doorH - doorOpenAmount * 4f)
                lineTo(doorX, doorY + doorH)
                close()
            }
            canvas.drawPath(leftDoor, doorPaint)
            val rightDoor = Path().apply {
                moveTo(doorX + doorW, doorY)
                lineTo(doorX + doorW - closedW * 0.5f, doorY + doorOpenAmount * 4f)
                lineTo(doorX + doorW - closedW * 0.5f, doorY + doorH - doorOpenAmount * 4f)
                lineTo(doorX + doorW, doorY + doorH)
                close()
            }
            canvas.drawPath(rightDoor, doorPaint)
        }

        canvas.drawRoundRect(RectF(doorX, doorY, doorX + doorW, doorY + doorH),
            4f, 4f, doorBorderPaint)
    }

    private fun drawBird(canvas: Canvas, ch: Float) {
        val doorCenterY = ch * 0.58f + 16f
        val birdX = 0f
        val birdY = doorCenterY - birdOutAmount * 28f + birdBobOffset

        canvas.save()
        canvas.translate(birdX, birdY)

        canvas.drawOval(RectF(-10f, 8f, 10f, 14f), shadowPaint)

        val tailPath = Path().apply {
            moveTo(-6f, 4f)
            lineTo(-14f, 10f)
            lineTo(-10f, 6f)
            lineTo(-16f, 14f)
            lineTo(-8f, 8f)
            lineTo(-12f, 16f)
            lineTo(-4f, 8f)
            close()
        }
        canvas.drawPath(tailPath, birdWingPaint)

        canvas.drawOval(RectF(-10f, -6f, 10f, 10f), birdBodyPaint)
        canvas.drawOval(RectF(-6f, -2f, 8f, 8f), birdBellyPaint)

        val wingPath = Path().apply {
            moveTo(-8f, 0f)
            quadTo(-14f, -8f, -4f, -10f)
            quadTo(2f, -8f, 4f, -2f)
            close()
        }
        canvas.drawPath(wingPath, birdWingPaint)

        canvas.drawCircle(6f, -8f, 9f, birdBodyPaint)
        canvas.drawCircle(9f, -10f, 2.5f, birdEyePaint)
        canvas.drawCircle(9.8f, -10.8f, 0.8f, birdEyeHighlightPaint)

        val beakGap = beakOpenAmount * 4f
        val upperBeak = Path().apply {
            moveTo(14f, -9f)
            lineTo(22f, -8f - beakGap * 0.3f)
            lineTo(14f, -7f)
            close()
        }
        canvas.drawPath(upperBeak, birdBeakPaint)
        val lowerBeak = Path().apply {
            moveTo(14f, -7f)
            lineTo(20f, -6f + beakGap)
            lineTo(14f, -6f)
            close()
        }
        canvas.drawPath(lowerBeak, birdBeakPaint)

        canvas.drawOval(RectF(3f, -18f, 7f, -12f), birdBodyPaint)
        canvas.drawOval(RectF(6f, -20f, 9f, -14f), birdBodyPaint)
        canvas.drawOval(RectF(9f, -18f, 12f, -13f), birdBodyPaint)

        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pendulumAnimator?.cancel()
        pendulumAnimator = null
    }
}
