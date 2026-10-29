package com.nudge.assist.ui.floating

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import kotlin.math.hypot

/**
 * 3 separate circular popout buttons expanding vertically downwards from the floating button.
 * - Minimalist textless design with smooth, organic icons (~20-21dp span, 10% smaller).
 * - Screenshot icon: smooth rounded viewfinder brackets framing a soft-cornered smartphone silhouette.
 * - Volume icon: smooth flowing curves and Bezier flared horn with rounded sound wave arcs.
 * - Power icon: smooth rounded 280° arc with rounded stem dome caps.
 * - Dark mode base with refined low-opacity white accents (Shot/Vol) and soft coral-red accents (Power).
 * - Subtle, elegant accent styling without overly bold/garish color saturations.
 * - Full-screen dismiss scrim: outside touches safely dismiss the menu without activating background apps.
 */
class ActionPopoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface PopoutListener {
        fun onScreenshotClicked()
        fun onVolumeClicked()
        fun onPowerClicked()
        fun onDismissRequested()
    }

    var listener: PopoutListener? = null

    private val density = context.resources.displayMetrics.density
    private val handler = Handler(Looper.getMainLooper())
    private val autoDismissRunnable = Runnable { listener?.onDismissRequested() }

    // Anchor positioning parameters
    private var anchorX = 0f
    private var anchorY = 0f
    private var anchorSize = 56f * density

    // Full-screen subtle scrim
    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    // Dark Mode Base Fill
    private val darkBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(240, 22, 22, 26) // Deep obsidian dark mode charcoal
    }

    // Shadow Paint
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(60, 0, 0, 0)
    }

    // Low-opacity White Accent for Shot & Vol (subtle and not too bold)
    private val whiteAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(18, 255, 255, 255) // Subtle soft white accent glow
    }

    private val whiteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.3f * density
        color = Color.argb(45, 255, 255, 255) // Refined subtle white/grey accent rim
    }

    // Bold White Icons for Shot & Vol (smooth curves, calibrated bold weight)
    private val boldWhiteStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.3f * density
        color = Color.WHITE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val boldWhiteFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    // Low-opacity Red Accent for Power Button (subtle accent, not too bold)
    private val redAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(25, 239, 68, 68) // Soft coral-red accent wash
    }

    private val redBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.3f * density
        color = Color.argb(80, 239, 68, 68) // Tasteful coral-red accent rim
    }

    // Bold Red Icon for Power (smooth curves, calibrated bold weight)
    private val boldRedStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.4f * density
        color = Color.rgb(248, 113, 113) // Soft coral red (#F87171), refined accent
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var animProgress = 0f

    init {
        // Vertical cascading entrance animation
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 220
            interpolator = OvershootInterpolator(1.25f)
            addUpdateListener { animator ->
                animProgress = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
        resetAutoDismissTimer()
    }

    fun configureAnchor(btnX: Float, btnY: Float, btnSize: Float) {
        this.anchorX = btnX
        this.anchorY = btnY
        this.anchorSize = btnSize
        invalidate()
    }

    fun resetAutoDismissTimer() {
        handler.removeCallbacks(autoDismissRunnable)
        handler.postDelayed(autoDismissRunnable, 4000)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(autoDismissRunnable)
    }

    private fun getCircleRadius(): Float = 27f * density

    private fun getCenterPoints(): Array<Pair<Float, Float>> {
        val radius = getCircleRadius()
        val margin = 12f * density
        val spacing = 10f * density
        val diameter = radius * 2f

        val btnCenterX = anchorX + (anchorSize / 2f)
        val cx = btnCenterX.coerceIn(radius + 8f, width - radius - 8f)

        val totalHeight = (3f * diameter) + (2f * spacing)
        val navBuffer = 80f * density
        val isExpandingDownwards = (anchorY + anchorSize + margin + totalHeight <= height - navBuffer)

        return if (isExpandingDownwards) {
            val cy0 = anchorY + anchorSize + margin + radius
            val cy1 = cy0 + diameter + spacing
            val cy2 = cy1 + diameter + spacing
            arrayOf(Pair(cx, cy0), Pair(cx, cy1), Pair(cx, cy2))
        } else {
            // Near bottom threshold: expand upwards above the button
            val cy2 = anchorY - margin - radius
            val cy1 = cy2 - diameter - spacing
            val cy0 = cy1 - diameter - spacing
            arrayOf(Pair(cx, cy0), Pair(cx, cy1), Pair(cx, cy2))
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        resetAutoDismissTimer()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true // Consume touch to shield background apps
            MotionEvent.ACTION_UP -> {
                val touchX = event.x
                val touchY = event.y
                val points = getCenterPoints()
                val touchRadius = getCircleRadius() + (14f * density)

                for (i in points.indices) {
                    val (cx, cy) = points[i]
                    if (hypot(touchX - cx, touchY - cy) <= touchRadius) {
                        when (i) {
                            0 -> {
                                android.util.Log.i("AssistX", "ActionPopoutView: Screenshot clicked")
                                listener?.onScreenshotClicked()
                            }
                            1 -> {
                                android.util.Log.i("AssistX", "ActionPopoutView: Volume clicked")
                                listener?.onVolumeClicked()
                            }
                            2 -> {
                                android.util.Log.i("AssistX", "ActionPopoutView: Power clicked")
                                listener?.onPowerClicked()
                            }
                        }
                        return true
                    }
                }
                // Tapped anywhere outside the 3 circles -> safely dismiss popout
                android.util.Log.i("AssistX", "ActionPopoutView: Tap outside captured, dismissing popout")
                listener?.onDismissRequested()
                return true
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // 1. Draw subtle full-screen dismiss scrim (18% black)
        scrimPaint.alpha = (animProgress * 45).toInt().coerceIn(0, 45)
        canvas.drawRect(0f, 0f, w, h, scrimPaint)

        val radius = getCircleRadius()
        val points = getCenterPoints()

        // 2. Circle 0: Screenshot [ Smooth Viewfinder Frame ]
        drawCircularButton(
            canvas = canvas,
            cx = points[0].first,
            cy = points[0].second,
            radius = radius,
            animOffset = 0f,
            isPower = false
        ) { c, cx, cy ->
            drawSmoothScreenshotViewfinder(c, cx, cy)
        }

        // 3. Circle 1: Volume [ Smooth Speaker & Sound Waves ]
        drawCircularButton(
            canvas = canvas,
            cx = points[1].first,
            cy = points[1].second,
            radius = radius,
            animOffset = 0.1f,
            isPower = false
        ) { c, cx, cy ->
            drawSmoothVolumeIcon(c, cx, cy)
        }

        // 4. Circle 2: Power [ Smooth Bold Power Symbol ]
        drawCircularButton(
            canvas = canvas,
            cx = points[2].first,
            cy = points[2].second,
            radius = radius,
            animOffset = 0.2f,
            isPower = true
        ) { c, cx, cy ->
            drawSmoothPowerIcon(c, cx, cy)
        }
    }

    private fun drawCircularButton(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        animOffset: Float,
        isPower: Boolean,
        drawIcon: (Canvas, Float, Float) -> Unit
    ) {
        val itemProgress = ((animProgress - animOffset) / (1f - animOffset)).coerceIn(0f, 1f)
        if (itemProgress <= 0f) return

        canvas.save()
        canvas.scale(itemProgress, itemProgress, cx, cy)

        val alphaInt = (itemProgress * 255).toInt().coerceIn(0, 255)
        shadowPaint.alpha = (itemProgress * 65).toInt().coerceIn(0, 65)

        // Drop shadow
        canvas.drawCircle(cx, cy + (3.5f * density), radius, shadowPaint)

        // Dark Mode Base Circle
        darkBasePaint.alpha = (itemProgress * 235).toInt().coerceIn(0, 235)
        canvas.drawCircle(cx, cy, radius, darkBasePaint)

        // Low-Opacity Specific Accents
        if (isPower) {
            redAccentPaint.alpha = (itemProgress * 45).toInt().coerceIn(0, 45)
            redBorderPaint.alpha = (itemProgress * 165).toInt().coerceIn(0, 165)
            canvas.drawCircle(cx, cy, radius, redAccentPaint)
            canvas.drawCircle(cx, cy, radius, redBorderPaint)
        } else {
            whiteAccentPaint.alpha = (itemProgress * 32).toInt().coerceIn(0, 32)
            whiteBorderPaint.alpha = (itemProgress * 85).toInt().coerceIn(0, 85)
            canvas.drawCircle(cx, cy, radius, whiteAccentPaint)
            canvas.drawCircle(cx, cy, radius, whiteBorderPaint)
        }

        // Large Bold Centered Icon (>60% of button space)
        drawIcon(canvas, cx, cy)

        canvas.restore()
    }

    /**
     * Smooth Screen Capture Viewfinder icon.
     * 10% smaller than previous size (21dp span inside 54dp circle),
     * featuring smooth quadratic Bezier rounded corner brackets framing a smooth rounded phone screen.
     */
    private fun drawSmoothScreenshotViewfinder(canvas: Canvas, cx: Float, cy: Float) {
        val halfW = 10.5f * density // 21dp total width span (10% smaller than 24dp)
        val halfH = 10.5f * density // 21dp total height span
        val armLen = 5.2f * density
        val cornerR = 2.8f * density // Smooth rounded fillet radius (zero sharp corners)

        // 4 Smooth Viewfinder Corner Brackets with Rounded Bezier Fillets
        // Top-Left ┌
        val tlPath = Path().apply {
            moveTo(cx - halfW, cy - halfH + armLen)
            lineTo(cx - halfW, cy - halfH + cornerR)
            quadTo(cx - halfW, cy - halfH, cx - halfW + cornerR, cy - halfH)
            lineTo(cx - halfW + armLen, cy - halfH)
        }
        canvas.drawPath(tlPath, boldWhiteStroke)

        // Top-Right ┐
        val trPath = Path().apply {
            moveTo(cx + halfW - armLen, cy - halfH)
            lineTo(cx + halfW - cornerR, cy - halfH)
            quadTo(cx + halfW, cy - halfH, cx + halfW, cy - halfH + cornerR)
            lineTo(cx + halfW, cy - halfH + armLen)
        }
        canvas.drawPath(trPath, boldWhiteStroke)

        // Bottom-Left └
        val blPath = Path().apply {
            moveTo(cx - halfW, cy + halfH - armLen)
            lineTo(cx - halfW, cy + halfH - cornerR)
            quadTo(cx - halfW, cy + halfH, cx - halfW + cornerR, cy + halfH)
            lineTo(cx - halfW + armLen, cy + halfH)
        }
        canvas.drawPath(blPath, boldWhiteStroke)

        // Bottom-Right ┘
        val brPath = Path().apply {
            moveTo(cx + halfW - armLen, cy + halfH)
            lineTo(cx + halfW - cornerR, cy + halfH)
            quadTo(cx + halfW, cy + halfH, cx + halfW, cy + halfH - cornerR)
            lineTo(cx + halfW, cy + halfH - armLen)
        }
        canvas.drawPath(brPath, boldWhiteStroke)

        // Center Smartphone Screen Silhouette with Smooth Rounded Corners
        val phoneW = 4.8f * density
        val phoneH = 7.2f * density
        val phoneRect = RectF(cx - phoneW, cy - phoneH, cx + phoneW, cy + phoneH)
        val strokeThin = Paint(boldWhiteStroke).apply {
            strokeWidth = 1.5f * density
        }
        canvas.drawRoundRect(phoneRect, 2.6f * density, 2.6f * density, strokeThin)

        // Smooth pill home-indicator bar
        canvas.drawLine(
            cx - (1.8f * density),
            cy + phoneH - (1.8f * density),
            cx + (1.8f * density),
            cy + phoneH - (1.8f * density),
            strokeThin
        )
    }

    /**
     * Smooth Volume icon.
     * 10% smaller than previous size (~21dp span), with organic flowing curves,
     * smooth Bezier horn flares, and rounded soundwave arcs.
     */
    private fun drawSmoothVolumeIcon(canvas: Canvas, cx: Float, cy: Float) {
        val shiftLeft = 1.2f * density
        val bodyLeft = cx - (9.5f * density) + shiftLeft
        val neckX = cx - (5.0f * density) + shiftLeft
        val mouthX = cx - (0.5f * density) + shiftLeft
        val neckHalfH = 3.2f * density
        val mouthHalfH = 8.2f * density
        val r = 1.6f * density

        val path = Path().apply {
            moveTo(bodyLeft + r, cy - neckHalfH)
            lineTo(neckX, cy - neckHalfH)
            // Smooth organic flare towards mouth
            quadTo(neckX + (1.6f * density), cy - neckHalfH, mouthX, cy - mouthHalfH)
            // Smooth rounded front rim of horn
            quadTo(mouthX + (1.0f * density), cy, mouthX, cy + mouthHalfH)
            // Smooth organic flare back down to neck
            quadTo(neckX + (1.6f * density), cy + neckHalfH, neckX, cy + neckHalfH)
            lineTo(bodyLeft + r, cy + neckHalfH)
            // Bottom-left rounded corner
            quadTo(bodyLeft, cy + neckHalfH, bodyLeft, cy + neckHalfH - r)
            lineTo(bodyLeft, cy - neckHalfH + r)
            // Top-left rounded corner
            quadTo(bodyLeft, cy - neckHalfH, bodyLeft + r, cy - neckHalfH)
            close()
        }
        canvas.drawPath(path, boldWhiteFill)

        // Inner sound wave arc (smooth rounded cap)
        val arcRectSmall = RectF(
            mouthX + (1.8f * density),
            cy - (4.2f * density),
            mouthX + (6.0f * density),
            cy + (4.2f * density)
        )
        canvas.drawArc(arcRectSmall, -38f, 76f, false, boldWhiteStroke)

        // Outer sound wave arc (smooth rounded cap)
        val arcRectLarge = RectF(
            mouthX + (4.6f * density),
            cy - (8.0f * density),
            mouthX + (11.2f * density),
            cy + (8.0f * density)
        )
        canvas.drawArc(arcRectLarge, -42f, 84f, false, boldWhiteStroke)
    }

    /**
     * Smooth Power icon.
     * 10% smaller than previous size (19.6dp diameter),
     * smooth rounded arc ends and rounded vertical center stem.
     */
    private fun drawSmoothPowerIcon(canvas: Canvas, cx: Float, cy: Float) {
        val radius = 9.8f * density // ~19.6dp diameter (10% smaller than 22dp)
        val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // 280° smooth open arc with rounded caps
        canvas.drawArc(arcRect, -50f, 280f, false, boldRedStroke)

        // Smooth vertical center stem with rounded dome caps
        canvas.drawLine(
            cx,
            cy - radius - (2.2f * density),
            cx,
            cy - (0.6f * density),
            boldRedStroke
        )
    }
}
