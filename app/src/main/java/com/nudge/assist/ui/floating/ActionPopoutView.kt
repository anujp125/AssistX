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
 * 3 separate individual circular popout buttons expanding vertically downwards with appropriate margins.
 * Each popout button features an accent white/grey background circle with crisp icons, and a specially
 * differentiated Power button.
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

    // Accent White/Grey background circle styling
    private val circleBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.rgb(238, 241, 245) // Premium light silver-grey accent
    }

    private val circleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = Color.argb(160, 190, 198, 210) // Subtle refined border
    }

    // Shadow/drop glow paint
    private val circleShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(40, 0, 0, 0)
    }

    // Standard Icon & Text paints (Slate Dark on White/Grey)
    private val iconStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.8f
        color = Color.rgb(30, 41, 59) // Deep slate dark (#1E293B)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val iconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.rgb(30, 41, 59)
    }

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(30, 41, 59)
        textSize = 17f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // Special Differentiator for Power Button
    private val powerCircleBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.rgb(255, 242, 242) // Accent white/grey with soft coral warmth
    }

    private val powerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.rgb(239, 68, 68) // Vivid coral red accent ring (#EF4444)
    }

    private val powerIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.2f
        color = Color.rgb(220, 38, 38) // Vivid red (#DC2626)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val powerLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(220, 38, 38)
        textSize = 17f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var animProgress = 0f

    init {
        // Vertical entrance cascade animation
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

        // Distribute 3 circles evenly vertically
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
            val touchRadius = getCircleRadius() + 14f // Comfortable touch hit target

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
            // Tapped in transparent spacing between circles -> dismiss
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

        // 1. Draw Circle 0: Screenshot [ 📸 Shot ]
        drawCircularButton(
            canvas = canvas,
            cx = points[0].first,
            cy = points[0].second,
            radius = radius,
            animOffset = 0f,
            isPower = false
        ) { c, cx, cy ->
            val iconCy = cy - 4f
            drawCameraIcon(c, cx, iconCy)
            c.drawText("Shot", cx, cy + radius * 0.65f, labelTextPaint)
        }

        // 2. Draw Circle 1: Volume [ 🔊 Vol ]
        drawCircularButton(
            canvas = canvas,
            cx = points[1].first,
            cy = points[1].second,
            radius = radius,
            animOffset = 0.1f,
            isPower = false
        ) { c, cx, cy ->
            val iconCy = cy - 4f
            drawVolumeIcon(c, cx, iconCy)
            c.drawText("Vol", cx, cy + radius * 0.65f, labelTextPaint)
        }

        // 3. Draw Circle 2: Power [ ⏻ Power ] (Special Differentiator)
        drawCircularButton(
            canvas = canvas,
            cx = points[2].first,
            cy = points[2].second,
            radius = radius,
            animOffset = 0.2f,
            isPower = true
        ) { c, cx, cy ->
            val iconCy = cy - 4f
            drawPowerIcon(c, cx, iconCy)
            c.drawText("Power", cx, cy + radius * 0.65f, powerLabelPaint)
        }
    }

    private fun drawCircularButton(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        animOffset: Float,
        isPower: Boolean,
        drawContent: (Canvas, Float, Float) -> Unit
    ) {
        // Individual scale and alpha based on cascading animProgress
        val itemProgress = ((animProgress - animOffset) / (1f - animOffset)).coerceIn(0f, 1f)
        if (itemProgress <= 0f) return

        canvas.save()
        canvas.scale(itemProgress, itemProgress, cx, cy)

        val alphaInt = (itemProgress * 255).toInt().coerceIn(0, 255)
        circleShadowPaint.alpha = (itemProgress * 50).toInt().coerceIn(0, 50)

        // Drop shadow circle
        canvas.drawCircle(cx, cy + 3.5f, radius, circleShadowPaint)

        // Background Accent White/Grey circle
        if (isPower) {
            powerCircleBgPaint.alpha = alphaInt
            powerBorderPaint.alpha = alphaInt
            canvas.drawCircle(cx, cy, radius, powerCircleBgPaint)
            canvas.drawCircle(cx, cy, radius, powerBorderPaint)
        } else {
            circleBgPaint.alpha = alphaInt
            circleBorderPaint.alpha = alphaInt
            canvas.drawCircle(cx, cy, radius, circleBgPaint)
            canvas.drawCircle(cx, cy, radius, circleBorderPaint)
        }

        // Content
        drawContent(canvas, cx, cy)

        canvas.restore()
    }

    private fun drawCameraIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bw = 15f
        val bh = 11f
        val rect = RectF(cx - bw, cy - bh + 2f, cx + bw, cy + bh + 2f)
        canvas.drawRoundRect(rect, 3.5f, 3.5f, iconStrokePaint)

        // Lens
        canvas.drawCircle(cx, cy + 2f, 4.5f, iconStrokePaint)

        // Flash
        canvas.drawCircle(cx - 8.5f, cy - bh + 5f, 1.4f, iconFillPaint)
    }

    private fun drawVolumeIcon(canvas: Canvas, cx: Float, cy: Float) {
        val path = Path().apply {
            moveTo(cx - 10f, cy - 3.5f)
            lineTo(cx - 6f, cy - 3.5f)
            lineTo(cx, cy - 8.5f)
            lineTo(cx, cy + 8.5f)
            lineTo(cx - 6f, cy + 3.5f)
            lineTo(cx - 10f, cy + 3.5f)
            close()
        }
        canvas.drawPath(path, iconFillPaint)

        // Sound waves
        val arcRectSmall = RectF(cx - 2f, cy - 5f, cx + 6.5f, cy + 5f)
        canvas.drawArc(arcRectSmall, -45f, 90f, false, iconStrokePaint)

        val arcRectLarge = RectF(cx + 1.5f, cy - 9f, cx + 12f, cy + 9f)
        canvas.drawArc(arcRectLarge, -45f, 90f, false, iconStrokePaint)
    }

    private fun drawPowerIcon(canvas: Canvas, cx: Float, cy: Float) {
        val radius = 9f
        val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // 270° open arc
        canvas.drawArc(arcRect, -45f, 270f, false, powerIconPaint)

        // Vertical center line through open gap
        canvas.drawLine(cx, cy - radius - 2f, cx, cy - 1f, powerIconPaint)
    }
}
