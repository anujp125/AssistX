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
 * - Minimalist textless design with bold icons occupying >60% of button space.
 * - Screenshot icon redesigned as a viewfinder capture frame (no camera/Instagram resemblance).
 * - Dark mode base with low-opacity white accents (Shot/Vol) and low-opacity red accents (Power).
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
        color = Color.argb(235, 20, 20, 24) // Deep obsidian charcoal
    }

    // Shadow Paint
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(60, 0, 0, 0)
    }

    // Low-opacity White Accent for Shot & Vol
    private val whiteAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(32, 255, 255, 255) // Low-opacity white wash
    }

    private val whiteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        color = Color.argb(85, 255, 255, 255) // Subtle white/grey border ring
    }

    // Large Bold White Icons for Shot & Vol (>60% of button space)
    private val boldWhiteStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.6f * density
        color = Color.WHITE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val boldWhiteFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    // Low-opacity Red Accent for Power Button
    private val redAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(45, 239, 68, 68) // Low-opacity coral-red wash (#2DEF4444)
    }

    private val redBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.8f * density
        color = Color.argb(165, 239, 68, 68) // Vibrant coral-red border ring
    }

    // Large Bold Red Icon for Power (>60% of button space)
    private val boldRedStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.0f * density
        color = Color.rgb(255, 75, 75) // Vivid coral red (#FF4B4B)
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

        // 2. Circle 0: Screenshot [ Viewfinder Capture Frame ]
        drawCircularButton(
            canvas = canvas,
            cx = points[0].first,
            cy = points[0].second,
            radius = radius,
            animOffset = 0f,
            isPower = false
        ) { c, cx, cy ->
            drawLargeScreenshotViewfinder(c, cx, cy)
        }

        // 3. Circle 1: Volume [ Large Speaker & Sound Waves ]
        drawCircularButton(
            canvas = canvas,
            cx = points[1].first,
            cy = points[1].second,
            radius = radius,
            animOffset = 0.1f,
            isPower = false
        ) { c, cx, cy ->
            drawLargeVolumeIcon(c, cx, cy)
        }

        // 4. Circle 2: Power [ Large Bold IEC 5009 Power Symbol ]
        drawCircularButton(
            canvas = canvas,
            cx = points[2].first,
            cy = points[2].second,
            radius = radius,
            animOffset = 0.2f,
            isPower = true
        ) { c, cx, cy ->
            drawLargePowerIcon(c, cx, cy)
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
     * Unmistakable Screen Capture Viewfinder icon (>60% of button space).
     * Features 4 prominent viewfinder corner crop brackets framing a smartphone display silhouette.
     */
    private fun drawLargeScreenshotViewfinder(canvas: Canvas, cx: Float, cy: Float) {
        val halfW = 16f * density // 32dp width span (~60% of 54dp circle)
        val halfH = 16f * density // 32dp height span
        val armLen = 7.5f * density

        // 4 Viewfinder Corner Brackets
        // Top-Left ┌
        canvas.drawLine(cx - halfW, cy - halfH + armLen, cx - halfW, cy - halfH, boldWhiteStroke)
        canvas.drawLine(cx - halfW, cy - halfH, cx - halfW + armLen, cy - halfH, boldWhiteStroke)

        // Top-Right ┐
        canvas.drawLine(cx + halfW - armLen, cy - halfH, cx + halfW, cy - halfH, boldWhiteStroke)
        canvas.drawLine(cx + halfW, cy - halfH, cx + halfW, cy - halfH + armLen, boldWhiteStroke)

        // Bottom-Left └
        canvas.drawLine(cx - halfW, cy + halfH - armLen, cx - halfW, cy + halfH, boldWhiteStroke)
        canvas.drawLine(cx - halfW, cy + halfH, cx - halfW + armLen, cy + halfH, boldWhiteStroke)

        // Bottom-Right ┘
        canvas.drawLine(cx + halfW - armLen, cy + halfH, cx + halfW, cy + halfH, boldWhiteStroke)
        canvas.drawLine(cx + halfW, cy + halfH, cx + halfW, cy + halfH - armLen, boldWhiteStroke)

        // Center Smartphone Screen Silhouette
        val phoneW = 7.5f * density
        val phoneH = 11.5f * density
        val phoneRect = RectF(cx - phoneW, cy - phoneH, cx + phoneW, cy + phoneH)
        val strokeThin = Paint(boldWhiteStroke).apply {
            strokeWidth = 2.2f * density
        }
        canvas.drawRoundRect(phoneRect, 2.5f * density, 2.5f * density, strokeThin)

        // Small home-indicator notch bar
        canvas.drawLine(
            cx - (3f * density),
            cy + phoneH - (2.5f * density),
            cx + (3f * density),
            cy + phoneH - (2.5f * density),
            strokeThin
        )
    }

    /**
     * Large Bold Volume icon (>60% of button space).
     */
    private fun drawLargeVolumeIcon(canvas: Canvas, cx: Float, cy: Float) {
        val shiftLeft = 2f * density
        val path = Path().apply {
            moveTo(cx - (15f * density) + shiftLeft, cy - (5f * density))
            lineTo(cx - (8f * density) + shiftLeft, cy - (5f * density))
            lineTo(cx - (1.5f * density) + shiftLeft, cy - (13.5f * density))
            lineTo(cx - (1.5f * density) + shiftLeft, cy + (13.5f * density))
            lineTo(cx - (8f * density) + shiftLeft, cy + (5f * density))
            lineTo(cx - (15f * density) + shiftLeft, cy + (5f * density))
            close()
        }
        canvas.drawPath(path, boldWhiteFill)

        // Inner sound wave arc
        val arcRectSmall = RectF(
            cx + (1f * density),
            cy - (7.5f * density),
            cx + (9.5f * density),
            cy + (7.5f * density)
        )
        canvas.drawArc(arcRectSmall, -45f, 90f, false, boldWhiteStroke)

        // Outer sound wave arc
        val arcRectLarge = RectF(
            cx + (5f * density),
            cy - (14f * density),
            cx + (17.5f * density),
            cy + (14f * density)
        )
        canvas.drawArc(arcRectLarge, -45f, 90f, false, boldWhiteStroke)
    }

    /**
     * Large Bold Power icon (>60% of button space).
     */
    private fun drawLargePowerIcon(canvas: Canvas, cx: Float, cy: Float) {
        val radius = 15f * density // 30dp diameter (~60% of 54dp circle)
        val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // 270° bold open arc
        canvas.drawArc(arcRect, -45f, 270f, false, boldRedStroke)

        // Bold vertical center line through open gap
        canvas.drawLine(
            cx,
            cy - radius - (3.5f * density),
            cx,
            cy - (1.5f * density),
            boldRedStroke
        )
    }
}
