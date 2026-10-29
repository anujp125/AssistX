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
 * Sleek 2-action popout view displaying Screenshot and Volume shortcuts adjacent to the floating button.
 */
class ActionPopoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface PopoutListener {
        fun onScreenshotClicked()
        fun onVolumeClicked()
        fun onDismissRequested()
    }

    var listener: PopoutListener? = null

    private val handler = Handler(Looper.getMainLooper())
    private val autoDismissRunnable = Runnable { listener?.onDismissRequested() }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(245, 30, 30, 30) // Deep dark charcoal
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = Color.argb(120, 255, 255, 255)
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        color = Color.argb(70, 255, 255, 255)
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
        color = Color.argb(220, 255, 255, 255)
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }

    private val bgRect = RectF()
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
            val midX = width / 2f
            android.util.Log.i("AssistX", "ActionPopoutView touch UP at x=$touchX, midX=$midX, width=$width")
            if (touchX < midX) {
                android.util.Log.i("AssistX", "ActionPopoutView: Screenshot clicked")
                listener?.onScreenshotClicked()
            } else {
                android.util.Log.i("AssistX", "ActionPopoutView: Volume clicked")
                listener?.onVolumeClicked()
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
        val alphaInt = (animAlpha * 255).toInt().coerceIn(0, 255)
        bgPaint.alpha = (animAlpha * 245).toInt().coerceIn(0, 255)

        // 1. Draw rounded pill container
        bgRect.set(2f, 2f, w - 2f, h - 2f)
        val cornerRadius = h / 2f
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, borderPaint)

        // 2. Draw vertical divider between buttons
        val midX = w / 2f
        canvas.drawLine(midX, h * 0.22f, midX, h * 0.78f, dividerPaint)

        // 3. Draw Left Item: Camera / Screenshot Icon
        val leftCenterX = w * 0.25f
        val centerY = h * 0.42f
        drawCameraIcon(canvas, leftCenterX, centerY)
        canvas.drawText("Shot", leftCenterX, h * 0.84f, textPaint)

        // 4. Draw Right Item: Speaker / Volume Icon
        val rightCenterX = w * 0.75f
        drawVolumeIcon(canvas, rightCenterX, centerY)
        canvas.drawText("Vol", rightCenterX, h * 0.84f, textPaint)

        canvas.restore()
    }

    private fun drawCameraIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bw = 18f
        val bh = 13f
        val rect = RectF(cx - bw, cy - bh + 2f, cx + bw, cy + bh + 2f)
        canvas.drawRoundRect(rect, 4f, 4f, iconPaint)

        // Lens circle
        canvas.drawCircle(cx, cy + 2f, 5.5f, iconPaint)

        // Flash dot
        canvas.drawCircle(cx - 10f, cy - bh + 6f, 1.5f, iconFillPaint)
    }

    private fun drawVolumeIcon(canvas: Canvas, cx: Float, cy: Float) {
        val path = Path().apply {
            moveTo(cx - 12f, cy - 4f)
            lineTo(cx - 7f, cy - 4f)
            lineTo(cx, cy - 10f)
            lineTo(cx, cy + 10f)
            lineTo(cx - 7f, cy + 4f)
            lineTo(cx - 12f, cy + 4f)
            close()
        }
        canvas.drawPath(path, iconFillPaint)

        // Sound wave arcs
        val arcRectSmall = RectF(cx - 2f, cy - 6f, cx + 8f, cy + 6f)
        canvas.drawArc(arcRectSmall, -45f, 90f, false, iconPaint)

        val arcRectLarge = RectF(cx + 2f, cy - 11f, cx + 15f, cy + 11f)
        canvas.drawArc(arcRectLarge, -45f, 90f, false, iconPaint)
    }
}
