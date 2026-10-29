package com.nudge.assist.ui.floating

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import com.nudge.assist.R
import kotlin.math.hypot

/**
 * 3 separate circular popout buttons expanding vertically downwards or upwards from the floating button.
 * - Minimalist textless design using custom user-selected iconography (Screenshot, Volume, Power).
 * - Custom icons scaled to acquire 40-50% (~25dp span) of each 54dp popup button.
 * - Screenshot & Volume tinted pure white with subtle white accents on dark obsidian base.
 * - Power tinted vivid coral red (#F87171) with soft red accents on dark obsidian base.
 * - Strict 12dp margin from the floating button with adaptive direction selection based on remaining screen pixels.
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

    // Anchor positioning parameters (screen coordinates and dimensions of floating button)
    private var anchorX = 0f
    private var anchorY = 0f
    private var anchorWidth = 56f * density
    private var anchorHeight = 56f * density

    // Target icon footprint: ~45% of 54dp circle = 24.3dp (~24.5dp)
    private val iconTargetSpan = 24.5f * density

    // User-uploaded icon bitmaps (decoded once from drawables)
    private val screenshotBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_action_screenshot)
    }
    private val volumeBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_action_volume)
    }
    private val powerBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_action_power)
    }

    // Paint filters to recolor/tint icons appropriately
    // Screenshot & Volume: Tinted Pure White
    private val whiteIconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    }

    // Power: Tinted Vivid Coral Red (#F87171)
    private val redIconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = PorterDuffColorFilter(Color.rgb(248, 113, 113), PorterDuff.Mode.SRC_IN)
    }

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

    fun configureAnchor(btnX: Float, btnY: Float, btnWidth: Float, btnHeight: Float) {
        this.anchorX = btnX
        this.anchorY = btnY
        this.anchorWidth = btnWidth
        this.anchorHeight = btnHeight
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

    /**
     * Calculates the exact center coordinates for the 3 circular popout buttons.
     * Enforces a strict margin from the floating button and chooses upward or downward
     * arrangement based on hardcoded remaining pixel thresholds.
     */
    private fun getCenterPoints(): Array<Pair<Float, Float>> {
        val radius = getCircleRadius()
        val strictMargin = 12f * density // Strict pixel margin between floating button and nearest popup button
        val itemSpacing = 10f * density  // Strict spacing between adjacent popup buttons
        val diameter = radius * 2f

        // Convert anchor screen coordinates to local view coordinates to cancel any window insets/cutout offsets
        val popoutLoc = IntArray(2)
        getLocationOnScreen(popoutLoc)
        val relAnchorX = anchorX - popoutLoc[0]
        val relAnchorY = anchorY - popoutLoc[1]

        val btnCenterX = relAnchorX + (anchorWidth / 2f)
        val minX = radius + (8f * density)
        val maxX = (if (width > 0) width.toFloat() else context.resources.displayMetrics.widthPixels.toFloat()) - radius - (8f * density)
        val cx = btnCenterX.coerceIn(minX, maxX)

        // Array height: 3 buttons (54dp each) + 2 spacing gaps (10dp each) = 182dp
        val arrayHeight = (3f * diameter) + (2f * itemSpacing)

        // Hardcoded edge buffers for comfortable margins in either direction
        val topEdgeBuffer = 44f * density // Top safe buffer (clears status bar & display cutout)
        val bottomEdgeBuffer = 56f * density // Bottom safe buffer (clears navigation bar & gesture pill)

        val totalSpaceNeededDown = strictMargin + arrayHeight + bottomEdgeBuffer
        val totalSpaceNeededUp = strictMargin + arrayHeight + topEdgeBuffer

        val screenH = if (height > 0) height.toFloat() else context.resources.displayMetrics.heightPixels.toFloat()
        val buttonBottom = relAnchorY + anchorHeight
        val buttonTop = relAnchorY

        val remainingDown = screenH - buttonBottom
        val remainingUp = buttonTop

        val isExpandingDownwards = when {
            remainingDown >= totalSpaceNeededDown -> true
            remainingUp >= totalSpaceNeededUp -> false
            else -> remainingDown >= remainingUp
        }

        return if (isExpandingDownwards) {
            // Arrange downwards: Circle 0 is strictMargin below button, Circle 1 next, Circle 2 lowest
            val cy0 = buttonBottom + strictMargin + radius
            val cy1 = cy0 + diameter + itemSpacing
            val cy2 = cy1 + diameter + itemSpacing
            arrayOf(Pair(cx, cy0), Pair(cx, cy1), Pair(cx, cy2))
        } else {
            // Arrange upwards: Circle 0 is strictMargin above button, Circle 1 next, Circle 2 highest
            val cy0 = buttonTop - strictMargin - radius
            val cy1 = cy0 - diameter - itemSpacing
            val cy2 = cy1 - diameter - itemSpacing
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

        // 2. Circle 0: Screenshot [ Custom User Icon ]
        drawCircularButton(
            canvas = canvas,
            cx = points[0].first,
            cy = points[0].second,
            radius = radius,
            animOffset = 0f,
            isPower = false
        ) { c, cx, cy ->
            drawCustomScreenshotIcon(c, cx, cy)
        }

        // 3. Circle 1: Volume [ Custom User Icon ]
        drawCircularButton(
            canvas = canvas,
            cx = points[1].first,
            cy = points[1].second,
            radius = radius,
            animOffset = 0.1f,
            isPower = false
        ) { c, cx, cy ->
            drawCustomVolumeIcon(c, cx, cy)
        }

        // 4. Circle 2: Power [ Custom User Icon ]
        drawCircularButton(
            canvas = canvas,
            cx = points[2].first,
            cy = points[2].second,
            radius = radius,
            animOffset = 0.2f,
            isPower = true
        ) { c, cx, cy ->
            drawCustomPowerIcon(c, cx, cy)
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

        // Low-Opacity Specific Accents & Icon Alpha
        if (isPower) {
            redAccentPaint.alpha = (itemProgress * 25).toInt().coerceIn(0, 25)
            redBorderPaint.alpha = (itemProgress * 80).toInt().coerceIn(0, 80)
            redIconPaint.alpha = alphaInt
            canvas.drawCircle(cx, cy, radius, redAccentPaint)
            canvas.drawCircle(cx, cy, radius, redBorderPaint)
        } else {
            whiteAccentPaint.alpha = (itemProgress * 18).toInt().coerceIn(0, 18)
            whiteBorderPaint.alpha = (itemProgress * 45).toInt().coerceIn(0, 45)
            whiteIconPaint.alpha = alphaInt
            canvas.drawCircle(cx, cy, radius, whiteAccentPaint)
            canvas.drawCircle(cx, cy, radius, whiteBorderPaint)
        }

        // Custom Tinted Icon (40-50% of button space)
        drawIcon(canvas, cx, cy)

        canvas.restore()
    }

    /**
     * Renders the user's custom Screenshot icon (smartphone with screen crop & home dot).
     * Tinted crisp pure white, acquiring ~45% of the button space (24.5dp of 54dp circle).
     */
    private fun drawCustomScreenshotIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bmp = screenshotBitmap ?: return
        val halfSpan = iconTargetSpan / 2f
        val dstRect = RectF(cx - halfSpan, cy - halfSpan, cx + halfSpan, cy + halfSpan)
        canvas.drawBitmap(bmp, null, dstRect, whiteIconPaint)
    }

    /**
     * Renders the user's custom Volume icon (speaker with dual soundwaves).
     * Tinted crisp pure white, acquiring ~45% of the button space (24.5dp of 54dp circle).
     */
    private fun drawCustomVolumeIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bmp = volumeBitmap ?: return
        val halfSpan = iconTargetSpan / 2f
        val dstRect = RectF(cx - halfSpan, cy - halfSpan, cx + halfSpan, cy + halfSpan)
        canvas.drawBitmap(bmp, null, dstRect, whiteIconPaint)
    }

    /**
     * Renders the user's custom Power icon (circular ring enclosing IEC power symbol).
     * Tinted vibrant coral-red (#F87171), acquiring ~45% of the button space (24.5dp of 54dp circle).
     */
    private fun drawCustomPowerIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bmp = powerBitmap ?: return
        val halfSpan = iconTargetSpan / 2f
        val dstRect = RectF(cx - halfSpan, cy - halfSpan, cx + halfSpan, cy + halfSpan)
        canvas.drawBitmap(bmp, null, dstRect, redIconPaint)
    }
}
