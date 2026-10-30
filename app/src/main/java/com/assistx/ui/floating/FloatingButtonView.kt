package com.assistx.ui.floating

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.assistx.R
import com.assistx.ui.haptics.MetroHaptics
import kotlin.math.hypot

/**
 * Windows Phone "Metro" Restyled Floating Assistant View.
 *
 * Characteristics:
 * - Flat solid aesthetic with 2dp solid outline.
 * - Customizable Shape: CIRCLE, SQUARE (0dp Metro), ROUNDED (12dp squircle).
 * - Customizable Style: METRO_OUTLINE, SOLID_ACCENT, MONOLINE_RING.
 * - Customizable Color: ACCENT (follows theme) or custom color override.
 * - Integrated MetroHaptics feedback.
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
    private val haptics = MetroHaptics(context)

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var totalDragDistance = 0f

    private var idleAlpha = 0.75f
    private val activeAlpha = 1.0f
    private var currentAlpha = idleAlpha
    private var idleFadeDelayMs = 3000L
    private var hapticFeedbackEnabled = true
    var snapToEdgeEnabled = true
        private set

    // Customization Properties
    var buttonShape: String = "CIRCLE"
        private set
    var buttonStyle: String = "METRO_OUTLINE"
        private set
    var buttonColorMode: String = "ACCENT"
        private set

    // Metro Accent Color (Defaults to Cobalt #0050EF)
    private var accentColorInt = Color.parseColor("#0050EF")

    // Flat Base Paint
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    // 2dp Metro Outline
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * context.resources.displayMetrics.density
        color = accentColorInt
    }

    // Inner Accent Ring (Status Indicator)
    private val innerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * context.resources.displayMetrics.density
        color = accentColorInt
    }

    // Center Core Dot Paint (Clean Assistive Dot replacing confusing power glyph)
    private val centerCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    fun updateSettings(
        idleOpacityPercent: Int,
        idleFadeSeconds: Int,
        hapticFeedback: Boolean,
        snapToEdge: Boolean,
        accentHex: String = "#0050EF",
        buttonShape: String = "CIRCLE",
        buttonStyle: String = "METRO_OUTLINE",
        buttonColorMode: String = "ACCENT"
    ) {
        this.idleAlpha = (idleOpacityPercent / 100f).coerceIn(0.2f, 1.0f)
        this.idleFadeDelayMs = (idleFadeSeconds * 1000L).coerceIn(1000L, 10000L)
        this.hapticFeedbackEnabled = hapticFeedback
        this.haptics.isEnabled = hapticFeedback
        this.snapToEdgeEnabled = snapToEdge
        this.buttonShape = buttonShape.uppercase()
        this.buttonStyle = buttonStyle.uppercase()
        this.buttonColorMode = buttonColorMode

        val effectiveHex = if (buttonColorMode.startsWith("#")) buttonColorMode else accentHex
        try {
            this.accentColorInt = Color.parseColor(effectiveHex)
        } catch (e: Exception) {
            this.accentColorInt = Color.parseColor("#0050EF")
        }

        updatePaintAlpha(this.idleAlpha)
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (!isDragging) {
                android.util.Log.i("AssistX", "FloatingButtonView: onSingleTapConfirmed")
                haptics.tap(this@FloatingButtonView)
                listener?.onSingleTap()
                return true
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            android.util.Log.i("AssistX", "FloatingButtonView: onDoubleTap")
            haptics.select(this@FloatingButtonView)
            listener?.onDoubleTap()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (!isDragging) {
                android.util.Log.i("AssistX", "FloatingButtonView: onLongPress")
                haptics.select(this@FloatingButtonView)
                listener?.onLongPress()
            }
        }
    })

    private var activeAlphaAnimator: ValueAnimator? = null

    // Reusable geometry cache for zero allocations in onDraw
    private val outerRect = RectF()
    private val innerRect = RectF()
    private val glyphRect = RectF()

    init {
        filterTouchesWhenObscured = true
        updatePaintAlpha(idleAlpha)
    }

    private fun updatePaintAlpha(alpha: Float) {
        currentAlpha = alpha
        val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
        val isLightColor = (accentColorInt == Color.WHITE || (Color.red(accentColorInt) > 215 && Color.green(accentColorInt) > 215 && Color.blue(accentColorInt) > 215))

        when (buttonStyle) {
            "SOLID_ACCENT" -> {
                basePaint.color = accentColorInt
                basePaint.alpha = alphaInt
                borderPaint.color = if (isLightColor) Color.BLACK else Color.WHITE
                borderPaint.alpha = (alpha * 200).toInt().coerceIn(0, 255)
                innerRingPaint.color = if (isLightColor) Color.argb(96, 0, 0, 0) else Color.WHITE
                innerRingPaint.alpha = (alpha * 120).toInt().coerceIn(0, 255)
                centerCorePaint.color = if (isLightColor) Color.BLACK else Color.WHITE
                centerCorePaint.alpha = (alpha * 240).toInt().coerceIn(0, 255)
            }
            "MONOLINE_RING" -> {
                basePaint.color = Color.BLACK
                basePaint.alpha = (alpha * 160).toInt().coerceIn(0, 255)
                borderPaint.color = accentColorInt
                borderPaint.alpha = alphaInt
                innerRingPaint.color = accentColorInt
                innerRingPaint.alpha = alphaInt
                centerCorePaint.color = accentColorInt
                centerCorePaint.alpha = (alpha * 220).toInt().coerceIn(0, 255)
            }
            else -> { // "METRO_OUTLINE" default
                basePaint.color = Color.BLACK
                basePaint.alpha = alphaInt
                borderPaint.color = accentColorInt
                borderPaint.alpha = alphaInt
                innerRingPaint.color = accentColorInt
                innerRingPaint.alpha = (alpha * 180).toInt().coerceIn(0, 255)
                centerCorePaint.color = if (isLightColor) accentColorInt else Color.WHITE
                centerCorePaint.alpha = (alpha * 220).toInt().coerceIn(0, 255)
            }
        }
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
        activeAlphaAnimator?.cancel()
        activeAlphaAnimator = ValueAnimator.ofFloat(from, to).apply {
            duration = 200
            addUpdateListener { animator ->
                updatePaintAlpha(animator.animatedValue as Float)
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
        activeAlphaAnimator?.cancel()
        activeAlphaAnimator = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val gestureHandled = gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                totalDragDistance = 0f
                fadeToActiveAlpha()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                if (!isDragging && dist > touchSlop) {
                    isDragging = true
                    haptics.light(this)
                }

                if (isDragging) {
                    listener?.onDragMoved(dx, dy)
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    totalDragDistance += dist
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    haptics.light(this)
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
        val density = context.resources.displayMetrics.density
        val strokeHalf = borderPaint.strokeWidth / 2f
        val outerRadius = (width.coerceAtMost(height) / 2f) - strokeHalf - (1f * density)

        if (outerRadius <= 0) return

        val innerRadius = outerRadius * 0.60f
        val coreRadius = outerRadius * 0.22f
        outerRect.set(cx - outerRadius, cy - outerRadius, cx + outerRadius, cy + outerRadius)
        innerRect.set(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius)
        glyphRect.set(cx - coreRadius, cy - coreRadius, cx + coreRadius, cy + coreRadius)

        when (buttonShape) {
            "SQUARE" -> {
                // Flat Metro Square (0dp radius)
                canvas.drawRect(outerRect, basePaint)
                canvas.drawRect(outerRect, borderPaint)
                canvas.drawRect(innerRect, innerRingPaint)
                canvas.drawRect(glyphRect, centerCorePaint)
            }
            "ROUNDED" -> {
                // Metro Rounded Squircle (12dp corner radius)
                val cr = 12f * density
                canvas.drawRoundRect(outerRect, cr, cr, basePaint)
                canvas.drawRoundRect(outerRect, cr, cr, borderPaint)
                canvas.drawRoundRect(innerRect, cr * 0.60f, cr * 0.60f, innerRingPaint)
                canvas.drawRoundRect(glyphRect, cr * 0.22f, cr * 0.22f, centerCorePaint)
            }
            else -> {
                // Metro Circle
                canvas.drawCircle(cx, cy, outerRadius, basePaint)
                canvas.drawCircle(cx, cy, outerRadius, borderPaint)
                canvas.drawCircle(cx, cy, innerRadius, innerRingPaint)
                canvas.drawCircle(cx, cy, coreRadius, centerCorePaint)
            }
        }
    }
}
