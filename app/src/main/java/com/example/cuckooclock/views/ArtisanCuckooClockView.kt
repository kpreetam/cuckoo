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
import com.example.cuckooclock.MoonPhase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class ArtisanCuckooClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // --- Animation state ---
    private var doorOpenAmount = 0f
    private var birdOutAmount = 0f
    private var birdBobOffset = 0f
    private var beakOpenAmount = 0f
    private var isAnimating = false
    private var pendulumAngle = 0f  // synced to seconds
    private var pendulumSwing = 1f  // +1 or -1 direction

    // Moon info cached
    private var moonInfo = MoonPhase.calculate()
    private var lastMoonCalc = 0L

    // --- Paints ---
    private fun woodPaint(color: String) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.parseColor(color); style = Paint.Style.FILL
    }
    private fun strokePaint(color: String, w: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.parseColor(color); style = Paint.Style.STROKE; strokeWidth = w
    }
    private fun fillPaint(color: String) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.parseColor(color); style = Paint.Style.FILL
    }

    private val pWoodDark    = woodPaint("#3B1F0A")
    private val pWoodMed     = woodPaint("#5C3010")
    private val pWoodLight   = woodPaint("#7A4520")
    private val pWoodHighlight = woodPaint("#9B6030")
    private val pRoof        = woodPaint("#271108")
    private val pRoofLight   = woodPaint("#3B1F0A")
    private val pGoldFill    = fillPaint("#E6B800")
    private val pGoldStroke  = strokePaint("#E6B800", 2f)
    private val pGoldDim     = strokePaint("#886E00", 1.5f)
    private val pLeafGreen   = fillPaint("#2A5E2A")
    private val pLeafDark    = fillPaint("#183818")
    private val pBerry       = fillPaint("#CC2200")
    private val pBarkStroke  = strokePaint("#1A0A00", 1.5f).apply { alpha = 100 }
    private val pFaceBg      = fillPaint("#F2E4C0")
    private val pFaceRing    = strokePaint("#3B1F0A", 3f)
    private val pFaceInner   = strokePaint("#886E00", 1.5f)
    private val pTickMajor   = strokePaint("#3B1F0A", 3f).apply { strokeCap = Paint.Cap.ROUND }
    private val pTickMinor   = strokePaint("#6B4020", 1.5f).apply { strokeCap = Paint.Cap.ROUND }
    private val pHourHand    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A0A00"); style = Paint.Style.FILL
    }
    private val pMinuteHand  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2A1400"); style = Paint.Style.FILL
    }
    private val pHandStroke  = strokePaint("#E6B800", 1f)
    private val pCenter      = fillPaint("#E6B800")
    private val pCenterRing  = strokePaint("#3B1F0A", 2f)
    private val pDoorDark    = fillPaint("#1A0800")
    private val pDoorWood    = fillPaint("#4A2810")
    private val pDoorBorder  = strokePaint("#C8960C", 2f)
    private val pChain       = strokePaint("#C8960C", 3f).apply { strokeCap = Paint.Cap.ROUND }
    private val pPendRod     = strokePaint("#8B6914", 4f).apply { strokeCap = Paint.Cap.ROUND }
    private val pPendBob     = fillPaint("#E6B800")
    private val pPendBobRim  = strokePaint("#3B1F0A", 2f)
    private val pPendShine   = fillPaint("#FFF0A0")
    private val pWeight      = fillPaint("#7A6040")
    private val pWeightDark  = fillPaint("#3B2810")
    private val pWeightShine = fillPaint("#B09060")
    private val pShadow      = fillPaint("#1A0A00").apply { alpha = 80 }
    private val pBirdBody    = fillPaint("#6A6A6A")
    private val pBirdWing    = fillPaint("#444444")
    private val pBirdBelly   = fillPaint("#C8A878")
    private val pBirdBeak    = fillPaint("#FFB300")
    private val pBirdEye     = fillPaint("#0A0A0A")
    private val pBirdEyeHL   = fillPaint("#FFFFFF")
    private val pBirdStripe  = fillPaint("#888888")
    private val pDateText    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F2E4C0")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    }
    private val pMoonText    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AAAACC")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
    private val pMoonEmoji   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val pNumberText  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3B1F0A")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    // Pendulum animator (driven externally by tick())
    private var pendulumAnimator: ValueAnimator? = null
    private var lastSecond = -1

    fun tick() {
        val now = Calendar.getInstance()
        val second = now.get(Calendar.SECOND)
        if (second != lastSecond) {
            lastSecond = second
            // Each second: swing to opposite side
            animatePendulumTick()
        }
        // Refresh moon every hour
        val nowMs = System.currentTimeMillis()
        if (nowMs - lastMoonCalc > 3_600_000L) {
            moonInfo = MoonPhase.calculate()
            lastMoonCalc = nowMs
        }
        invalidate()
    }

    private fun animatePendulumTick() {
        pendulumAnimator?.cancel()
        val target = if (pendulumSwing > 0) 22f else -22f
        pendulumAnimator = ValueAnimator.ofFloat(pendulumAngle, target).apply {
            duration = 950
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                pendulumAngle = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    pendulumSwing = -pendulumSwing
                }
            })
            start()
        }
    }

fun animateSingleCuckoo(index: Int, total: Int) {
    if (index == 0) {
        // First cuckoo: open door then bird out+bob
        if (!isAnimating) {
            isAnimating = true
            animateDoor(true) {
                animateBirdOut { animateBob { animateBirdIn {
                    if (index >= total - 1) finishAnimation()
                } } }
            }
        } else {
            animateBirdOut { animateBob { animateBirdIn {
                if (index >= total - 1) finishAnimation()
            } } }
        }
    } else if (index >= total - 1) {
        // Last cuckoo: bob then close door
        animateBirdOut { animateBob { animateBirdIn { finishAnimation() } } }
    } else {
        // Middle cuckoos: just bob
        animateBirdOut { animateBob { animateBirdIn {} } }
    }
}

private fun finishAnimation() {
    animateDoor(false) { isAnimating = false; invalidate() }
}

    private fun animateDoor(open: Boolean, onDone: () -> Unit) {
        ValueAnimator.ofFloat(if (open) 0f else 1f, if (open) 1f else 0f).apply {
            duration = 350
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { doorOpenAmount = it.animatedValue as Float; invalidate() }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { onDone() }
            })
            start()
        }
    }

    private fun animateBirdOut(onDone: () -> Unit) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 220
            interpolator = OvershootInterpolator(1.2f)
            addUpdateListener { birdOutAmount = it.animatedValue as Float; invalidate() }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { onDone() }
            })
            start()
        }
    }

    private fun animateBob(onDone: () -> Unit) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 550
            addUpdateListener { anim ->
                val t = anim.animatedFraction
                birdBobOffset = -10f * sin(Math.PI * t * 2).toFloat()
                beakOpenAmount = if (t < 0.5f) t * 2f else (1f - t) * 2f
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    birdBobOffset = 0f; beakOpenAmount = 0f; onDone()
                }
            })
            start()
        }
    }

    private fun animateBirdIn(onDone: () -> Unit) {
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 220
            addUpdateListener { birdOutAmount = it.animatedValue as Float; invalidate() }
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

        // Scale so clock fits width, with room for pendulum below
        val clockW = w * 0.82f
        val scale = clockW / 220f
        val clockBodyH = 160f * scale
        val roofH = 70f * scale
        val topPad = h * 0.03f

        canvas.save()
        canvas.translate(cx, topPad + roofH)
        canvas.scale(scale, scale)
        drawClockBody(canvas, h / scale)
        canvas.restore()

        // Date & Moon drawn at bottom in real coords
        drawDateMoon(canvas, w, h, topPad + roofH + clockBodyH * 1.05f)
    }

    private fun drawClockBody(canvas: Canvas, availableH: Float) {
        val cw = 110f
        val ch = 160f

        // How much vertical space available for pendulum (in scaled units)
        val pendulumSpace = availableH - ch - 30f

        drawChainsAndWeights(canvas, cw, ch)
        drawPendulum(canvas, ch, pendulumSpace.coerceAtLeast(120f))
        drawBody(canvas, cw, ch)
        drawRoof(canvas, cw)
        drawClockFace(canvas, ch)
        drawDoor(canvas, ch)
        drawBodyBorder(canvas, cw, ch)
        if (birdOutAmount > 0f) drawBird(canvas, ch)
    }

    private fun drawChainsAndWeights(canvas: Canvas, cw: Float, ch: Float) {
        // Two chains
        listOf(-30f, 30f).forEach { x ->
            // Dashed chain effect
            var y = ch * 0.65f
            while (y < ch * 1.15f) {
                canvas.drawLine(x, y, x, y + 6f, pChain)
                y += 10f
            }
        }
        // Pine cone weights — more detailed
        drawPineconeWeight(canvas, -30f, ch * 1.15f, 14f)
        drawPineconeWeight(canvas, 30f, ch * 1.15f, 14f)
    }

    private fun drawPineconeWeight(canvas: Canvas, x: Float, y: Float, r: Float) {
        canvas.save()
        canvas.translate(x, y)
        // Stalk
        canvas.drawLine(0f, -r * 0.5f, 0f, -r, strokePaint("#8B6914", 3f))
        // Scales layered
        for (i in 0..4) {
            val s = (1f - i * 0.12f)
            val oy = i * r * 0.38f
            canvas.drawOval(RectF(-r * s, oy - r * 0.3f, r * s, oy + r * 0.55f), pWeight)
            canvas.drawOval(RectF(-r * s * 0.7f, oy - r * 0.15f, r * s * 0.7f, oy + r * 0.3f), pWeightDark)
        }
        // Shine
        canvas.drawOval(RectF(-r * 0.3f, -r * 0.1f, r * 0.1f, r * 0.2f), pWeightShine)
        canvas.restore()
    }

    private fun drawPendulum(canvas: Canvas, ch: Float, length: Float) {
        canvas.save()
        canvas.translate(0f, ch * 0.78f)
        canvas.rotate(pendulumAngle)

        // Rod with taper
        pPendRod.strokeWidth = 4f
        canvas.drawLine(0f, 0f, 0f, length * 0.72f, pPendRod)

        // Decorative bob — ornate brass disc
        val bobY = length * 0.72f
        val bobR = 20f

        // Shadow
        canvas.drawOval(RectF(-bobR + 3f, bobY - bobR + 3f, bobR + 3f, bobY + bobR + 3f), pShadow)

        // Outer ring
        canvas.drawCircle(0f, bobY, bobR, pPendBob)

        // Engraved rings
        canvas.drawCircle(0f, bobY, bobR, pPendBobRim)
        canvas.drawCircle(0f, bobY, bobR * 0.75f, strokePaint("#C8960C", 1.5f))
        canvas.drawCircle(0f, bobY, bobR * 0.5f, pPendBob)
        canvas.drawCircle(0f, bobY, bobR * 0.5f, strokePaint("#3B1F0A", 1.5f))

        // Radial engravings
        for (i in 0..7) {
            val a = Math.toRadians(i * 45.0)
            val r1 = bobR * 0.55f
            val r2 = bobR * 0.72f
            canvas.drawLine(
                (r1 * cos(a)).toFloat(), bobY + (r1 * sin(a)).toFloat(),
                (r2 * cos(a)).toFloat(), bobY + (r2 * sin(a)).toFloat(),
                strokePaint("#C8960C", 1f)
            )
        }

        // Shine
        canvas.drawOval(RectF(-bobR * 0.4f, bobY - bobR * 0.6f, bobR * 0.1f, bobY - bobR * 0.2f), pPendShine)

        // Rod connector at top
        canvas.drawRect(-4f, -6f, 4f, 6f, pGoldFill)
        canvas.drawRect(-4f, -6f, 4f, 6f, pPendBobRim)

        canvas.restore()
    }

    private fun drawBody(canvas: Canvas, cw: Float, ch: Float) {
        // Main body with rounded bottom
        val bodyPath = Path().apply {
            moveTo(-cw, 0f)
            lineTo(-cw, ch - 12f)
            quadTo(-cw, ch, -cw + 12f, ch)
            lineTo(cw - 12f, ch)
            quadTo(cw, ch, cw, ch - 12f)
            lineTo(cw, 0f)
            close()
        }
        canvas.drawPath(bodyPath, pWoodMed)

        // Wood grain
        val grainPaint = strokePaint("#2A1000", 1.2f).apply { alpha = 60 }
        for (i in 0..12) {
            val y = i * ch / 12f
            // Slightly wavy grain
            val path = Path()
            path.moveTo(-cw + 6f, y)
            for (xi in 0..10) {
                val px = -cw + 6f + xi * (cw * 2 - 12f) / 10f
                val wave = 1.5f * sin(xi * 0.8f + i.toFloat()).toFloat()
                path.lineTo(px, y + wave)
            }
            canvas.drawPath(path, grainPaint)
        }

        // Side panels — carved look
        canvas.drawRect(-cw, 0f, -cw + 10f, ch, pWoodDark)
        canvas.drawRect(cw - 10f, 0f, cw, ch, pWoodDark)

        // Highlight edge
        canvas.drawLine(-cw + 10f, 4f, -cw + 10f, ch - 4f, strokePaint("#9B6030", 1f))
        canvas.drawLine(cw - 10f, 4f, cw - 10f, ch - 4f, strokePaint("#9B6030", 1f))

        // Bottom decorative panel
        val bottomPanel = RectF(-cw + 12f, ch * 0.82f, cw - 12f, ch - 8f)
        canvas.drawRoundRect(bottomPanel, 6f, 6f, pWoodDark)
        canvas.drawRoundRect(bottomPanel, 6f, 6f, strokePaint("#C8960C", 1.5f))

        // Carved floral in bottom panel
        drawFloralCarving(canvas, 0f, ch * 0.89f, 18f)

        // Side leaf carvings
        drawLeafSprig(canvas, -cw + 22f, ch * 0.62f, -1f)
        drawLeafSprig(canvas, cw - 22f, ch * 0.62f, 1f)
    }

    private fun drawFloralCarving(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = strokePaint("#C8960C", 1.2f).apply { alpha = 180 }
        for (i in 0..5) {
            val a = Math.toRadians(i * 60.0)
            val x = cx + (r * 0.55f * cos(a)).toFloat()
            val y = cy + (r * 0.55f * sin(a)).toFloat()
            canvas.drawOval(RectF(x - 5f, y - 8f, x + 5f, y + 8f), p)
        }
        canvas.drawCircle(cx, cy, r * 0.25f, p)
    }

    private fun drawLeafSprig(canvas: Canvas, x: Float, y: Float, dir: Float) {
        canvas.save()
        canvas.translate(x, y)
        // Stem
        val stemP = strokePaint("#1A5A1A", 1.5f)
        canvas.drawLine(0f, 0f, dir * 8f, -20f, stemP)
        canvas.drawLine(0f, 0f, dir * 14f, -10f, stemP)
        // Leaves
        canvas.drawOval(RectF(dir * 2f, -22f, dir * 12f, -12f), pLeafGreen)
        canvas.drawOval(RectF(dir * 8f, -14f, dir * 18f, -4f), pLeafDark)
        canvas.drawCircle(dir * 6f, -3f, 3.5f, pBerry)
        canvas.drawCircle(dir * 10f, -6f, 2.5f, pBerry)
        canvas.restore()
    }

    private fun drawRoof(canvas: Canvas, cw: Float) {
        // Layered shingle roof
        val roofH = 70f
        // Shadow under roof
        canvas.drawOval(RectF(-cw - 5f, -3f, cw + 5f, 8f), pShadow)

        // Main roof
        val roofPath = Path().apply {
            moveTo(-cw - 22f, 0f)
            lineTo(0f, -roofH)
            lineTo(cw + 22f, 0f)
            close()
        }
        canvas.drawPath(roofPath, pRoof)

        // Shingle layers
        for (layer in 1..4) {
            val t = layer / 4f
            val lx = (cw + 22f) * (1f - t * 0.7f) + 4f
            val ly = -roofH * t
            val shinglePath = Path().apply {
                moveTo(-lx, ly)
                lineTo(-lx + 8f, ly + 4f)
                lineTo(lx - 8f, ly + 4f)
                lineTo(lx, ly)
            }
            canvas.drawPath(shinglePath, pRoofLight)
            // Shingle shadow line
            canvas.drawLine(-lx, ly, lx, ly, strokePaint("#100500", 1f))
        }

        canvas.drawPath(roofPath, strokePaint("#100500", 2f))

        // Decorative barge boards
        val leftBoard = Path().apply {
            moveTo(-cw - 22f, 0f)
            lineTo(-cw - 28f, 0f)
            lineTo(-4f, -roofH - 2f)
            lineTo(0f, -roofH)
            close()
        }
        canvas.drawPath(leftBoard, pWoodDark)
        canvas.drawPath(leftBoard, strokePaint("#C8960C", 1f))
        val rightBoard = Path().apply {
            moveTo(cw + 22f, 0f)
            lineTo(cw + 28f, 0f)
            lineTo(4f, -roofH - 2f)
            lineTo(0f, -roofH)
            close()
        }
        canvas.drawPath(rightBoard, pWoodDark)
        canvas.drawPath(rightBoard, strokePaint("#C8960C", 1f))

        // Finial
        canvas.drawCircle(0f, -roofH - 2f, 5f, pGoldFill)
        canvas.drawCircle(0f, -roofH - 2f, 5f, pCenterRing)
        canvas.drawCircle(0f, -roofH - 8f, 3f, pGoldFill)
        canvas.drawLine(0f, -roofH - 11f, 0f, -roofH - 18f, strokePaint("#E6B800", 2f))

        // Roof corner decorations
        drawLeafCluster(canvas, -cw - 16f, -2f)
        drawLeafCluster(canvas, cw + 16f, -2f)

        // Decorative fretwork on front
        drawFretwork(canvas, cw)
    }

    private fun drawFretwork(canvas: Canvas, cw: Float) {
        val p = strokePaint("#C8960C", 1.2f).apply { alpha = 160 }
        val fp = fillPaint("#C8960C").apply { alpha = 120 }
        // Scalloped edge
        var x = -cw + 5f
        while (x < cw - 5f) {
            canvas.drawArc(RectF(x, -5f, x + 14f, 9f), 0f, -180f, false, p)
            x += 14f
        }
        // Small diamonds
        x = -cw + 12f
        while (x < cw - 12f) {
            val dp = Path().apply {
                moveTo(x, -8f); lineTo(x + 4f, -4f); lineTo(x, 0f); lineTo(x - 4f, -4f); close()
            }
            canvas.drawPath(dp, fp)
            x += 22f
        }
    }

    private fun drawLeafCluster(canvas: Canvas, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        canvas.drawOval(RectF(-8f, -14f, 8f, 0f), pLeafGreen)
        canvas.drawOval(RectF(-13f, -9f, 1f, 5f), pLeafDark)
        canvas.drawOval(RectF(-1f, -9f, 13f, 5f), pLeafDark)
        canvas.drawCircle(-4f, -1f, 3.5f, pBerry)
        canvas.drawCircle(4f, -1f, 3.5f, pBerry)
        canvas.drawCircle(0f, -13f, 2f, pBerry)
        canvas.restore()
    }

    private fun drawClockFace(canvas: Canvas, ch: Float) {
        val faceR = 40f
        val fcy = ch * 0.36f

        // Outer wooden ring
        canvas.drawCircle(0f, fcy, faceR + 6f, pWoodDark)
        // Carved ring detail
        canvas.drawCircle(0f, fcy, faceR + 6f, strokePaint("#C8960C", 1.5f))
        canvas.drawCircle(0f, fcy, faceR + 3f, strokePaint("#886E00", 1f))

        // Face
        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                0f, fcy - faceR * 0.3f, faceR * 1.5f,
                intArrayOf(Color.parseColor("#FFF5DC"), Color.parseColor("#E8D4A0")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(0f, fcy, faceR, facePaint)
        canvas.drawCircle(0f, fcy, faceR, pFaceRing)
        canvas.drawCircle(0f, fcy, faceR - 3f, pFaceInner)

        // Hour markers — Roman numerals feel with varied ticks
        for (i in 0..59) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val isHour = i % 5 == 0
            val isQuarter = i % 15 == 0
            val outerR = faceR - 4f
            val innerR = when {
                isQuarter -> faceR - 14f
                isHour -> faceR - 11f
                else -> faceR - 8f
            }
            val p = if (isHour) pTickMajor else pTickMinor
            p.strokeWidth = when {
                isQuarter -> 3.5f
                isHour -> 2.5f
                else -> 1f
            }
            canvas.drawLine(
                (innerR * cos(a)).toFloat(), fcy + (innerR * sin(a)).toFloat(),
                (outerR * cos(a)).toFloat(), fcy + (outerR * sin(a)).toFloat(), p
            )
        }

        // Hour numbers (Roman-style positioning with Arabic)
        pNumberText.textSize = faceR * 0.22f
        val numR = faceR * 0.7f
        val romanNums = arrayOf("XII","I","II","III","IV","V","VI","VII","VIII","IX","X","XI")
        for (i in 0..11) {
            val a = Math.toRadians((i * 30 - 90).toDouble())
            val nx = (numR * cos(a)).toFloat()
            val ny = fcy + (numR * sin(a)).toFloat() + pNumberText.textSize * 0.37f
            canvas.drawText(romanNums[i], nx, ny, pNumberText)
        }

        // Clock hands
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        // Hour hand — tapered
        val hourAngle = Math.toRadians(((hour * 30 + minute * 0.5) - 90).toDouble())
        drawTaperedHand(canvas, fcy, hourAngle, faceR * 0.52f, faceR * 0.12f, 5f, pHourHand, pHandStroke)

        // Minute hand — longer tapered
        val minAngle = Math.toRadians(((minute * 6 + second * 0.1) - 90).toDouble())
        drawTaperedHand(canvas, fcy, minAngle, faceR * 0.78f, faceR * 0.08f, 3.5f, pMinuteHand, pHandStroke)

        // Center cap
        canvas.drawCircle(0f, fcy, 5f, pGoldFill)
        canvas.drawCircle(0f, fcy, 5f, pCenterRing)
        canvas.drawCircle(0f, fcy, 2.5f, fillPaint("#3B1F0A"))
    }

    private fun drawTaperedHand(
        canvas: Canvas, fcy: Float, angle: Double,
        length: Float, baseW: Float, tailLen: Float,
        fill: Paint, stroke: Paint
    ) {
        val path = Path().apply {
            val tipX = (length * cos(angle)).toFloat()
            val tipY = fcy + (length * sin(angle)).toFloat()
            val perpA = angle + Math.PI / 2
            val bx1 = (baseW * cos(perpA)).toFloat()
            val by1 = (baseW * sin(perpA)).toFloat()
            val tailX = (-tailLen * cos(angle)).toFloat()
            val tailY = fcy + (-tailLen * sin(angle)).toFloat()
            moveTo(tipX, tipY)
            lineTo(bx1, fcy + by1)
            lineTo(tailX, tailY)
            lineTo(-bx1, fcy - by1)
            close()
        }
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun drawDoor(canvas: Canvas, ch: Float) {
        val dw = 30f
        val dh = 35f
        val dx = -dw / 2f
        val dy = ch * 0.57f

        // Door arch
        val archPath = Path().apply {
            moveTo(dx, dy + dh)
            lineTo(dx, dy + dh * 0.4f)
            quadTo(dx, dy, dx + dw / 2f, dy)
            quadTo(dx + dw, dy, dx + dw, dy + dh * 0.4f)
            lineTo(dx + dw, dy + dh)
            close()
        }
        canvas.drawPath(archPath, pDoorDark)

        // Door panels (open with perspective)
        if (doorOpenAmount < 1f) {
            val cw2 = dw * (1f - doorOpenAmount)
            // Left panel
            val leftDoor = Path().apply {
                moveTo(dx, dy)
                lineTo(dx + cw2 * 0.5f, dy + doorOpenAmount * 5f)
                lineTo(dx + cw2 * 0.5f, dy + dh - doorOpenAmount * 3f)
                lineTo(dx, dy + dh)
                close()
            }
            canvas.drawPath(leftDoor, pDoorWood)
            // Panel detail
            canvas.drawRoundRect(
                RectF(dx + 2f, dy + 4f, dx + cw2 * 0.45f, dy + dh - 4f),
                3f, 3f, strokePaint("#C8960C", 1f).apply { alpha = 150 }
            )
            // Right panel
            val rightDoor = Path().apply {
                moveTo(dx + dw, dy)
                lineTo(dx + dw - cw2 * 0.5f, dy + doorOpenAmount * 5f)
                lineTo(dx + dw - cw2 * 0.5f, dy + dh - doorOpenAmount * 3f)
                lineTo(dx + dw, dy + dh)
                close()
            }
            canvas.drawPath(rightDoor, pDoorWood)
            canvas.drawRoundRect(
                RectF(dx + dw - cw2 * 0.45f, dy + 4f, dx + dw - 2f, dy + dh - 4f),
                3f, 3f, strokePaint("#C8960C", 1f).apply { alpha = 150 }
            )
        }
        canvas.drawPath(archPath, pDoorBorder)

        // Door frame carving
        canvas.drawPath(archPath, strokePaint("#886E00", 3f).apply {
            style = Paint.Style.STROKE
            pathEffect = null
        })
    }

    private fun drawBodyBorder(canvas: Canvas, cw: Float, ch: Float) {
        val bodyPath = Path().apply {
            moveTo(-cw, 0f)
            lineTo(-cw, ch - 12f)
            quadTo(-cw, ch, -cw + 12f, ch)
            lineTo(cw - 12f, ch)
            quadTo(cw, ch, cw, ch - 12f)
            lineTo(cw, 0f)
            close()
        }
        canvas.drawPath(bodyPath, pGoldStroke)
    }

    private fun drawBird(canvas: Canvas, ch: Float) {
        val doorCenterY = ch * 0.57f + 17f
        val birdY = doorCenterY - birdOutAmount * 34f + birdBobOffset

        canvas.save()
        canvas.translate(0f, birdY)

        // Shadow
        canvas.drawOval(RectF(-12f, 10f, 12f, 16f), pShadow)

        // Tail feathers — multiple
        for (i in -1..1) {
            val tailPath = Path().apply {
                moveTo(-4f + i * 2f, 6f)
                lineTo(-18f + i * 4f, 16f + i * 2f)
                lineTo(-12f + i * 3f, 8f)
                close()
            }
            canvas.drawPath(tailPath, if (i == 0) pBirdWing else pBirdBody)
        }

        // Body
        canvas.drawOval(RectF(-11f, -5f, 11f, 11f), pBirdBody)

        // Belly patch
        canvas.drawOval(RectF(-6f, -1f, 9f, 9f), pBirdBelly)

        // Wing with feather detail
        val wingPath = Path().apply {
            moveTo(-9f, 1f)
            quadTo(-16f, -7f, -5f, -11f)
            quadTo(2f, -9f, 4f, -1f)
            close()
        }
        canvas.drawPath(wingPath, pBirdWing)
        // Wing feather lines
        val fp = strokePaint("#333333", 1f)
        for (i in 0..2) {
            canvas.drawLine(-9f + i * 3f, 1f - i * 2f, -14f + i * 3f, -6f + i, fp)
        }

        // Head
        canvas.drawCircle(6f, -8f, 10f, pBirdBody)

        // Crest
        for (i in 0..2) {
            canvas.drawOval(RectF(3f + i * 2.5f, -20f - i, 7f + i * 2.5f, -12f - i), pBirdWing)
        }

        // Eye
        canvas.drawCircle(9.5f, -9.5f, 3f, pBirdEye)
        canvas.drawCircle(10.5f, -10.5f, 1f, pBirdEyeHL)
        canvas.drawCircle(9f, -9f, 1f, pBirdEyeHL)

        // Beak
        val beakGap = beakOpenAmount * 5f
        val upperBeak = Path().apply {
            moveTo(15f, -10f)
            lineTo(25f, -8f - beakGap * 0.3f)
            lineTo(15f, -7.5f)
            close()
        }
        canvas.drawPath(upperBeak, pBirdBeak)
        val lowerBeak = Path().apply {
            moveTo(15f, -7.5f)
            lineTo(23f, -5f + beakGap)
            lineTo(15f, -6f)
            close()
        }
        canvas.drawPath(lowerBeak, pBirdBeak)

        // Throat stripe
        canvas.drawOval(RectF(4f, -6f, 10f, -1f), pBirdStripe)

        canvas.restore()
    }

    private fun drawDateMoon(canvas: Canvas, w: Float, h: Float, startY: Float) {
        val cal = Calendar.getInstance()
        val dateFmt = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

        // Date
        pDateText.textSize = w * 0.038f
        canvas.drawText(dateFmt.format(cal.time), w / 2f, startY + w * 0.06f, pDateText)

        // Moon
        val moon = moonInfo
        pMoonEmoji.textSize = w * 0.07f
        canvas.drawText(moon.emoji, w * 0.18f, startY + w * 0.13f, pMoonEmoji)

        pMoonText.textSize = w * 0.030f
        canvas.drawText(moon.phaseName, w / 2f + w * 0.05f, startY + w * 0.10f, pMoonText)
        pMoonText.textSize = w * 0.025f
        canvas.drawText(moon.trending, w / 2f + w * 0.05f, startY + w * 0.135f, pMoonText)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pendulumAnimator?.cancel()
    }
}

