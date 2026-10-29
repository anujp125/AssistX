package com.nudge.assist.ui.floating

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.hypot

/**
 * Custom View rendering the 56dp assistive touch button with snap-to-edge dragging,
 * idle opacity fading, double-tap, and long-press gesture detection.
 */
class FloatingButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface InteractionListener {
        fun onSingleTap()
        fun onDoubleTap()
        fun onLongPress()
        fun onDragMoved(deltaX: Float, deltaY: Float)
        fun onDragReleased()
    }

    var listener: InteractionListener? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val handler = Handler(Looper.getMainLooper())
    private val idleFadeRunnable = Runnable { fadeToIdleAlpha() }

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    private var idleAlpha = 0.75f
    private val activeAlpha = 1.0f
    private var currentAlpha = idleAlpha
    private var idleFadeDelayMs = 3000L
    private var hapticFeedbackEnabled = true
    var snapToEdgeEnabled = true
        private set

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.argb(220, 240, 240, 240)
    }

    private val innerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private fun triggerHaptic() {
        if (hapticFeedbackEnabled) {
            performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    fun updateSettings(
        idleOpacityPercent: Int,
        idleFadeSeconds: Int,
        hapticFeedback: Boolean,
        snapToEdge: Boolean
    ) {
        this.idleAlpha = (idleOpacityPercent / 100f).coerceIn(0.2f, 1.0f)
        this.idleFadeDelayMs = (idleFadeSeconds * 1000L).coerceIn(1000L, 10000L)
        this.hapticFeedbackEnabled = hapticFeedback
        this.snapToEdgeEnabled = snapToEdge
        updatePaintAlpha(this.idleAlpha)
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (!isDragging) {
                android.util.Log.i("AssistX", "FloatingButtonView: onSingleTapConfirmed")
                triggerHaptic()
                listener?.onSingleTap()
                return true
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            android.util.Log.i("AssistX", "FloatingButtonView: onDoubleTap")
            triggerHaptic()
            listener?.onDoubleTap()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (!isDragging) {
                android.util.Log.i("AssistX", "FloatingButtonView: onLongPress")
                triggerHaptic()
                listener?.onLongPress()
            }
        }
    })

    init {
        updatePaintAlpha(idleAlpha)
    }

    private fun updatePaintAlpha(alpha: Float) {
        currentAlpha = alpha
        val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
        outerPaint.color = Color.argb(alphaInt, 33, 33, 33)
        ringPaint.alpha = (alpha * 220).toInt().coerceIn(0, 255)
        innerDotPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
        invalidate()
    }

    private fun fadeToActiveAlpha() {
        handler.removeCallbacks(idleFadeRunnable)
        if (currentAlpha != activeAlpha) {
            animateAlpha(currentAlpha, activeAlpha)
        }
    }

    private fun scheduleIdleFade() {
        handler.removeCallbacks(idleFadeRunnable)
        handler.postDelayed(idleFadeRunnable, idleFadeDelayMs)
    }

    private fun fadeToIdleAlpha() {
        animateAlpha(currentAlpha, idleAlpha)
    }

    private fun animateAlpha(from: Float, to: Float) {
        ValueAnimator.ofFloat(from, to).apply {
            duration = 300
            addUpdateListener { animator ->
                updatePaintAlpha(animator.animatedValue as Float)
            }
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Feed into gesture detector for double-tap and long-press
        val gestureHandled = gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                fadeToActiveAlpha()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY

                if (!isDragging && hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                    isDragging = true
                }

                if (isDragging) {
                    listener?.onDragMoved(dx, dy)
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    triggerHaptic()
                    listener?.onDragReleased()
                }
                scheduleIdleFade()
                return true
            }
        }

        return gestureHandled || super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = width.coerceAtMost(height) / 2f - 4f

        if (radius <= 0) return

        // 1. Draw dark outer circle
        canvas.drawCircle(cx, cy, radius, outerPaint)

        // 2. Draw subtle inner ring
        canvas.drawCircle(cx, cy, radius * 0.70f, ringPaint)

        // 3. Draw center core dot
        canvas.drawCircle(cx, cy, radius * 0.35f, innerDotPaint)
    }
}

