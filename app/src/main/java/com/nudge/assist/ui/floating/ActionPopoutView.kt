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
 * 3 separate individual circular popout buttons expanding vertically downwards.
 * Features a minimalist textless design with large bold centered icons, dark mode backgrounds,
 * and low-opacity accents of white for utility shortcuts and coral red for the Power button.
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

    private val handler = Handler(Looper.getMainLooper())
    private val autoDismissRunnable = Runnable { listener?.onDismissRequested() }

    // Dark Mode Base Fill
    private val darkBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(235, 22, 22, 26) // Deep dark obsidian charcoal
    }

    // Shadow Paint
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(55, 0, 0, 0)
    }

    // Low-opacity White Accent for Shot & Vol
    private val whiteAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(32, 255, 255, 255) // Low-opacity white wash
    }

    private val whiteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = Color.argb(85, 255, 255, 255) // Refined subtle white/grey border ring
    }

    // Large Bold White Icons for Shot & Vol
    private val boldWhiteIconStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        color = Color.WHITE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val boldWhiteIconFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
        strokeWidth = 2.8f
        color = Color.argb(165, 239, 68, 68) // Vibrant coral-red border ring
    }

    // Large Bold Red Icon for Power
    private val boldRedIconStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.0f
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

    fun resetAutoDismissTimer() {
        handler.removeCallbacks(autoDismissRunnable)
        handler.postDelayed(autoDismissRunnable, 4000)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(autoDismissRunnable)
    }

    private fun getCircleRadius(): Float {
        val w = width.toFloat()
        return (w / 2f) - 6f
    }

    private fun getCenterPoints(): Array<Pair<Float, Float>> {
        val cx = width / 2f
        val radius = getCircleRadius()
        val diameter = radius * 2f
        val h = height.toFloat()

        // Distribute 3 circles evenly vertically with appropriate margins
        val remainingSpace = h - (3f * diameter)
        val spacing = (remainingSpace / 4f).coerceAtLeast(8f)

        val cy0 = spacing + radius
        val cy1 = cy0 + diameter + spacing
        val cy2 = cy1 + diameter + spacing

        return arrayOf(Pair(cx, cy0), Pair(cx, cy1), Pair(cx, cy2))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        resetAutoDismissTimer()
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            val touchX = event.x
            val touchY = event.y
            val points = getCenterPoints()
            val touchRadius = getCircleRadius() + 14f

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
            listener?.onDismissRequested()
            return true
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val radius = getCircleRadius()
        if (radius <= 0) return

        val points = getCenterPoints()

        // 1. Circle 0: Screenshot [ 📸 ]
        drawCircularButton(
            canvas = canvas,
            cx = points[0].first,
            cy = points[0].second,
            radius = radius,
            animOffset = 0f,
            isPower = false
        ) { c, cx, cy ->
            drawLargeCameraIcon(c, cx, cy)
        }

        // 2. Circle 1: Volume [ 🔊 ]
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

        // 3. Circle 2: Power [ ⏻ ] (Accented Coral Red)
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
        canvas.drawCircle(cx, cy + 3.5f, radius, shadowPaint)

        // 1. Dark Mode Base Circle
        darkBasePaint.alpha = (itemProgress * 235).toInt().coerceIn(0, 235)
        canvas.drawCircle(cx, cy, radius, darkBasePaint)

        // 2. Specific Low-Opacity Accents
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

        // 3. Large Bold Centered Icon
        drawIcon(canvas, cx, cy)

        canvas.restore()
    }

    private fun drawLargeCameraIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bw = 18f
        val bh = 13.5f
        val rect = RectF(cx - bw, cy - bh + 2f, cx + bw, cy + bh + 2f)
        canvas.drawRoundRect(rect, 4.5f, 4.5f, boldWhiteIconStroke)

        // Center Lens
        canvas.drawCircle(cx, cy + 2f, 5.5f, boldWhiteIconStroke)

        // Flash dot
        canvas.drawCircle(cx - 10f, cy - bh + 6f, 1.8f, boldWhiteIconFill)
    }

    private fun drawLargeVolumeIcon(canvas: Canvas, cx: Float, cy: Float) {
        val path = Path().apply {
            moveTo(cx - 12.5f, cy - 4.5f)
            lineTo(cx - 7f, cy - 4.5f)
            lineTo(cx, cy - 11f)
            lineTo(cx, cy + 11f)
            lineTo(cx - 7f, cy + 4.5f)
            lineTo(cx - 12.5f, cy + 4.5f)
            close()
        }
        canvas.drawPath(path, boldWhiteIconFill)

        // Sound wave arcs
        val arcRectSmall = RectF(cx - 2f, cy - 6.5f, cx + 8.5f, cy + 6.5f)
        canvas.drawArc(arcRectSmall, -45f, 90f, false, boldWhiteIconStroke)

        val arcRectLarge = RectF(cx + 2.5f, cy - 11.5f, cx + 15.5f, cy + 11.5f)
        canvas.drawArc(arcRectLarge, -45f, 90f, false, boldWhiteIconStroke)
    }

    private fun drawLargePowerIcon(canvas: Canvas, cx: Float, cy: Float) {
        val radius = 12.5f
        val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // 270° bold open arc
        canvas.drawArc(arcRect, -45f, 270f, false, boldRedIconStroke)

        // Bold vertical center line through open gap
        canvas.drawLine(cx, cy - radius - 2.5f, cx, cy - 1f, boldRedIconStroke)
    }
}
