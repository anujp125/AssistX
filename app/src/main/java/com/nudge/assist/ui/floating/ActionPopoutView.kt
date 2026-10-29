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

/**
 * Sleek 3-action popout view displaying Screenshot, Volume, and Power shortcuts adjacent to the floating button.
 * Features an enlarged thumb-friendly footprint and an accented, distinct Power button.
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

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(245, 26, 26, 26) // Deep dark charcoal
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = Color.argb(120, 255, 255, 255)
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        color = Color.argb(60, 255, 255, 255)
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val iconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(230, 255, 255, 255)
        textSize = 21f
        textAlign = Paint.Align.CENTER
    }

    // Special distinct styling for Power Button
    private val powerBackdropPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(45, 255, 75, 75) // Subtle soft red glow/badge
    }

    private val powerIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        color = Color.rgb(255, 90, 95) // Vivid coral red
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val powerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 125, 125)
        textSize = 21f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val bgRect = RectF()
    private val powerBadgeRect = RectF()
    private var animScale = 1.0f
    private var animAlpha = 1.0f

    init {
        // Entrance animation
        ValueAnimator.ofFloat(0.85f, 1.0f).apply {
            duration = 180
            interpolator = OvershootInterpolator(1.2f)
            addUpdateListener { animator ->
                animScale = animator.animatedValue as Float
                animAlpha = (animScale - 0.85f) / 0.15f
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        resetAutoDismissTimer()
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            val touchX = event.x
            val colWidth = width / 3f
            android.util.Log.i("AssistX", "ActionPopoutView touch UP at x=$touchX, colWidth=$colWidth, width=$width")
            when {
                touchX < colWidth -> {
                    android.util.Log.i("AssistX", "ActionPopoutView: Screenshot clicked")
                    listener?.onScreenshotClicked()
                }
                touchX < 2 * colWidth -> {
                    android.util.Log.i("AssistX", "ActionPopoutView: Volume clicked")
                    listener?.onVolumeClicked()
                }
                else -> {
                    android.util.Log.i("AssistX", "ActionPopoutView: Power clicked")
                    listener?.onPowerClicked()
                }
            }
            return true
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        canvas.save()
        canvas.scale(animScale, animScale, w / 2f, h / 2f)
        val alphaInt = (animAlpha * 245).toInt().coerceIn(0, 255)
        bgPaint.alpha = alphaInt

        // 1. Draw rounded pill container
        bgRect.set(2f, 2f, w - 2f, h - 2f)
        val cornerRadius = h / 2f
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, borderPaint)

        val colWidth = w / 3f
        val centerY = h * 0.40f

        // 2. Draw vertical dividers between buttons
        canvas.drawLine(colWidth, h * 0.20f, colWidth, h * 0.80f, dividerPaint)
        canvas.drawLine(colWidth * 2f, h * 0.20f, colWidth * 2f, h * 0.80f, dividerPaint)

        // 3. Draw Left Item: Camera / Screenshot
        val leftCenterX = colWidth * 0.5f
        drawCameraIcon(canvas, leftCenterX, centerY)
        canvas.drawText("Shot", leftCenterX, h * 0.84f, textPaint)

        // 4. Draw Center Item: Speaker / Volume
        val midCenterX = colWidth * 1.5f
        drawVolumeIcon(canvas, midCenterX, centerY)
        canvas.drawText("Vol", midCenterX, h * 0.84f, textPaint)

        // 5. Draw Right Item: Special Accented Power Button
        val rightCenterX = colWidth * 2.5f

        // Soft tinted backdrop circle for Power differentiator
        val badgeRadius = h * 0.40f
        powerBadgeRect.set(
            rightCenterX - badgeRadius,
            h * 0.10f,
            rightCenterX + badgeRadius,
            h * 0.90f
        )
        canvas.drawRoundRect(powerBadgeRect, badgeRadius, badgeRadius, powerBackdropPaint)

        drawPowerIcon(canvas, rightCenterX, centerY)
        canvas.drawText("Power", rightCenterX, h * 0.84f, powerTextPaint)

        canvas.restore()
    }

    private fun drawCameraIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bw = 19f
        val bh = 14f
        val rect = RectF(cx - bw, cy - bh + 2f, cx + bw, cy + bh + 2f)
        canvas.drawRoundRect(rect, 4.5f, 4.5f, iconPaint)

        // Lens circle
        canvas.drawCircle(cx, cy + 2f, 6f, iconPaint)

        // Flash dot
        canvas.drawCircle(cx - 11f, cy - bh + 6f, 1.8f, iconFillPaint)
    }

    private fun drawVolumeIcon(canvas: Canvas, cx: Float, cy: Float) {
        val path = Path().apply {
            moveTo(cx - 13f, cy - 4.5f)
            lineTo(cx - 7.5f, cy - 4.5f)
            lineTo(cx, cy - 11f)
            lineTo(cx, cy + 11f)
            lineTo(cx - 7.5f, cy + 4.5f)
            lineTo(cx - 13f, cy + 4.5f)
            close()
        }
        canvas.drawPath(path, iconFillPaint)

        // Sound wave arcs
        val arcRectSmall = RectF(cx - 2f, cy - 6.5f, cx + 9f, cy + 6.5f)
        canvas.drawArc(arcRectSmall, -45f, 90f, false, iconPaint)

        val arcRectLarge = RectF(cx + 2.5f, cy - 12f, cx + 16.5f, cy + 12f)
        canvas.drawArc(arcRectLarge, -45f, 90f, false, iconPaint)
    }

    private fun drawPowerIcon(canvas: Canvas, cx: Float, cy: Float) {
        val radius = 11.5f
        val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // Draw 280° open circle (leaves a 80° gap at top from -130° to -50°)
        canvas.drawArc(arcRect, -40f, 260f, false, powerIconPaint)

        // Draw vertical center notch through top gap
        canvas.drawLine(cx, cy - radius - 2.5f, cx, cy - 1f, powerIconPaint)
    }
}
