package com.assistx.ui.floating

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import com.assistx.R
import com.assistx.ui.haptics.MetroHaptics
import kotlin.math.hypot

enum class PopoutAction {
    SCREENSHOT,
    VOLUME,
    POWER,
    PAYMENT_PAUSE
}

/**
 * 3 or 4 separate circular popout buttons expanding vertically downwards or upwards from the floating button.
 * - Minimalist textless design using custom user-selected iconography (Screenshot, Volume, Power, Payment Pause).
 * - Full customizable array settings: spacing, margin from anchor, orientation (auto/down/up), order.
 * - Customizable background capsule container element behind the buttons.
 * - Integrated MetroHaptics tactile feedback.
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
        fun onPaymentPauseClicked()
        fun onDismissRequested()
    }

    var listener: PopoutListener? = null

    private val density = context.resources.displayMetrics.density
    private val handler = Handler(Looper.getMainLooper())
    private val autoDismissRunnable = Runnable { listener?.onDismissRequested() }
    private val haptics = MetroHaptics(context)

    // Anchor positioning parameters (screen coordinates and dimensions of floating button)
    private var anchorX = 0f
    private var anchorY = 0f
    private var anchorWidth = 56f * density
    private var anchorHeight = 56f * density

    // Customization Settings
    var popoutButtonSizeDp: Int = 54
        private set
    var popoutOrientation: String = "AUTO"
        private set
    var popoutItemSpacingDp: Int = 10
        private set
    var popoutMarginDp: Int = 12
        private set
    var popoutOrder: String = "SCREENSHOT_VOLUME_POWER"
        private set
    var showPaymentPauseInPopout: Boolean = true
        private set

    // Background Capsule Settings
    var capsuleEnabled: Boolean = false
        private set
    var capsuleStyle: String = "OBSIDIAN"
        private set
    var capsuleCornerRadius: String = "PILL"
        private set
    private var accentColorInt: Int = Color.parseColor("#0050EF")

    // Target icon footprint: ~45% of button size
    private val iconTargetSpan: Float
        get() = (popoutButtonSizeDp * 0.45f) * density

    // Shared immutable icon bitmaps from cache
    private val screenshotBitmap: Bitmap?
        get() = OverlayBitmapCache.getScreenshotBitmap(context)
    private val volumeBitmap: Bitmap?
        get() = OverlayBitmapCache.getVolumeBitmap(context)
    private val powerBitmap: Bitmap?
        get() = OverlayBitmapCache.getPowerBitmap(context)
    private val pauseBitmap: Bitmap?
        get() = OverlayBitmapCache.getPauseBitmap(context)

    // Paint filters
    private val whiteIconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    }

    private val redIconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = PorterDuffColorFilter(Color.rgb(248, 113, 113), PorterDuff.Mode.SRC_IN)
    }

    // StatusAmber (#F0A30A) accents for Payment Safety Mode
    private val amberIconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = PorterDuffColorFilter(Color.parseColor("#F0A30A"), PorterDuff.Mode.SRC_IN)
    }

    // Dismiss scrim
    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    // Button Base Paints
    private val darkBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(240, 22, 22, 26)
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(60, 0, 0, 0)
    }

    private val whiteAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(18, 255, 255, 255)
    }

    private val whiteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.3f * density
        color = Color.argb(45, 255, 255, 255)
    }

    private val redAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(25, 239, 68, 68)
    }

    private val redBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.3f * density
        color = Color.argb(80, 239, 68, 68)
    }

    private val amberAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(25, 240, 163, 10)
    }

    private val amberBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.3f * density
        color = Color.argb(80, 240, 163, 10)
    }

    // Capsule Element Paints
    private val capsuleBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val capsuleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.8f * density
    }

    private var animProgress = 0f
    private var dismissDelayMs = 4000L
    private var popoutAnimator: ValueAnimator? = null

    // Reusable geometry cache for zero allocations in onDraw (up to 4 buttons)
    private val capsuleRect = RectF()
    private val iconDstRect = RectF()
    private val buttonCenters = Array(4) { PointF() }

    init {
        filterTouchesWhenObscured = true
        popoutAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
        popoutAnimator?.cancel()
        popoutAnimator = null
    }

    fun configureAnchor(btnX: Float, btnY: Float, btnWidth: Float, btnHeight: Float) {
        this.anchorX = btnX
        this.anchorY = btnY
        this.anchorWidth = btnWidth
        this.anchorHeight = btnHeight
        invalidate()
    }

    fun configureCustomizations(
        buttonSizeDp: Int = 54,
        orientation: String,
        itemSpacingDp: Int,
        marginDp: Int,
        order: String,
        showPaymentPause: Boolean = true,
        capsuleEnabled: Boolean,
        capsuleStyle: String,
        capsuleCornerRadius: String,
        accentHex: String
    ) {
        this.popoutButtonSizeDp = buttonSizeDp.coerceIn(36, 72)
        this.popoutOrientation = orientation.uppercase()
        this.popoutItemSpacingDp = itemSpacingDp.coerceIn(4, 24)
        this.popoutMarginDp = marginDp.coerceIn(4, 30)
        this.popoutOrder = order.uppercase()
        this.showPaymentPauseInPopout = showPaymentPause
        this.capsuleEnabled = capsuleEnabled
        this.capsuleStyle = capsuleStyle.uppercase()
        this.capsuleCornerRadius = capsuleCornerRadius.uppercase()
        try {
            this.accentColorInt = Color.parseColor(accentHex)
        } catch (e: Exception) {
            this.accentColorInt = Color.parseColor("#0050EF")
        }
        invalidate()
    }

    fun setDismissDelay(seconds: Int) {
        this.dismissDelayMs = (seconds * 1000L).coerceIn(1000L, 10000L)
        resetAutoDismissTimer()
    }

    fun setHapticFeedbackPreference(enabled: Boolean) {
        haptics.isEnabled = enabled
    }

    fun resetAutoDismissTimer() {
        handler.removeCallbacks(autoDismissRunnable)
        handler.postDelayed(autoDismissRunnable, dismissDelayMs)
    }

    private fun getCircleRadius(): Float = (popoutButtonSizeDp / 2f) * density

    private fun getActionOrder(): List<PopoutAction> {
        val baseList = when (popoutOrder) {
            "POWER_VOLUME_SCREENSHOT" -> listOf(PopoutAction.POWER, PopoutAction.VOLUME, PopoutAction.SCREENSHOT)
            "VOLUME_SCREENSHOT_POWER" -> listOf(PopoutAction.VOLUME, PopoutAction.SCREENSHOT, PopoutAction.POWER)
            "PAUSE_SCREENSHOT_VOLUME_POWER" -> listOf(PopoutAction.PAYMENT_PAUSE, PopoutAction.SCREENSHOT, PopoutAction.VOLUME, PopoutAction.POWER)
            "POWER_VOLUME_SCREENSHOT_PAUSE" -> listOf(PopoutAction.POWER, PopoutAction.VOLUME, PopoutAction.SCREENSHOT, PopoutAction.PAYMENT_PAUSE)
            "VOLUME_SCREENSHOT_POWER_PAUSE" -> listOf(PopoutAction.VOLUME, PopoutAction.SCREENSHOT, PopoutAction.POWER, PopoutAction.PAYMENT_PAUSE)
            else -> listOf(PopoutAction.SCREENSHOT, PopoutAction.VOLUME, PopoutAction.POWER)
        }
        return if (showPaymentPauseInPopout && !baseList.contains(PopoutAction.PAYMENT_PAUSE)) {
            baseList + PopoutAction.PAYMENT_PAUSE
        } else if (!showPaymentPauseInPopout) {
            baseList.filter { it != PopoutAction.PAYMENT_PAUSE }
        } else {
            baseList
        }
    }

    /**
     * Calculates the center coordinates for the popup buttons based on margins and orientation.
     * Mutates reusable PointF instances to achieve zero heap allocations per frame.
     */
    private fun updateCenterPoints(): Array<PointF> {
        val actions = getActionOrder()
        val count = actions.size.coerceAtMost(buttonCenters.size)
        val radius = getCircleRadius()
        val strictMargin = popoutMarginDp * density
        val itemSpacing = popoutItemSpacingDp * density
        val diameter = radius * 2f

        val popoutLoc = IntArray(2)
        getLocationOnScreen(popoutLoc)
        val relAnchorX = anchorX - popoutLoc[0]
        val relAnchorY = anchorY - popoutLoc[1]

        var screenW = if (width > 0) width.toFloat() else context.resources.displayMetrics.widthPixels.toFloat()
        var screenH = if (height > 0) height.toFloat() else context.resources.displayMetrics.heightPixels.toFloat()

        if (width <= 0 || height <= 0) {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager
            val disp = display ?: displayManager?.getDisplay(android.view.Display.DEFAULT_DISPLAY)
            if (disp != null) {
                val dm = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                disp.getRealMetrics(dm)
                val rawW = dm.widthPixels.toFloat()
                val rawH = dm.heightPixels.toFloat()
                val isLand = (disp.rotation == android.view.Surface.ROTATION_90 || disp.rotation == android.view.Surface.ROTATION_270)
                screenW = if (isLand) maxOf(rawW, rawH) else minOf(rawW, rawH)
                screenH = if (isLand) minOf(rawW, rawH) else maxOf(rawW, rawH)
            }
        }

        val isHorizontal = popoutOrientation.contains("HORIZ") || popoutOrientation == "LEFT" || popoutOrientation == "RIGHT"

        if (isHorizontal) {
            val btnCenterY = relAnchorY + (anchorHeight / 2f)
            val minY = radius + (44f * density)
            val maxY = screenH - radius - (56f * density)
            val cy = btnCenterY.coerceIn(minY, maxY)

            val arrayWidth = (count * diameter) + ((count - 1) * itemSpacing)
            val edgeBuffer = 16f * density
            val spaceNeededLeft = strictMargin + arrayWidth + edgeBuffer
            val spaceNeededRight = strictMargin + arrayWidth + edgeBuffer

            val buttonRight = relAnchorX + anchorWidth
            val buttonLeft = relAnchorX
            val remainingLeft = buttonLeft
            val remainingRight = screenW - buttonRight

            val isExpandingRight = when (popoutOrientation) {
                "RIGHT" -> true
                "LEFT" -> false
                else -> { // "HORIZONTAL", "HORIZ_AUTO"
                    if (remainingRight >= spaceNeededRight) true
                    else if (remainingLeft >= spaceNeededLeft) false
                    else remainingRight >= remainingLeft
                }
            }

            for (i in 0 until count) {
                val offset = strictMargin + radius + (i * (diameter + itemSpacing))
                val cx = if (isExpandingRight) buttonRight + offset else buttonLeft - offset
                buttonCenters[i].set(cx, cy)
            }
            return buttonCenters
        }

        // VERTICAL layout
        val btnCenterX = relAnchorX + (anchorWidth / 2f)
        val minX = radius + (8f * density)
        val maxX = screenW - radius - (8f * density)
        val cx = btnCenterX.coerceIn(minX, maxX)

        val arrayHeight = (count * diameter) + ((count - 1) * itemSpacing)
        val topEdgeBuffer = 44f * density
        val bottomEdgeBuffer = 56f * density

        val totalSpaceNeededDown = strictMargin + arrayHeight + bottomEdgeBuffer
        val totalSpaceNeededUp = strictMargin + arrayHeight + topEdgeBuffer

        val buttonBottom = relAnchorY + anchorHeight
        val buttonTop = relAnchorY
        val remainingDown = screenH - buttonBottom
        val remainingUp = buttonTop

        val isExpandingDownwards = when (popoutOrientation) {
            "DOWN" -> true
            "UP" -> false
            else -> { // "AUTO", "VERTICAL"
                when {
                    remainingDown >= totalSpaceNeededDown -> true
                    remainingUp >= totalSpaceNeededUp -> false
                    else -> remainingDown >= remainingUp
                }
            }
        }

        for (i in 0 until count) {
            val offset = strictMargin + radius + (i * (diameter + itemSpacing))
            val cy = if (isExpandingDownwards) buttonBottom + offset else buttonTop - offset
            buttonCenters[i].set(cx, cy)
        }
        return buttonCenters
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        resetAutoDismissTimer()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val touchX = event.x
                val touchY = event.y
                val points = updateCenterPoints()
                val touchRadius = getCircleRadius() + (14f * density)
                val actions = getActionOrder()
                val count = actions.size.coerceAtMost(buttonCenters.size)

                for (i in 0 until count) {
                    val pt = points[i]
                    if (hypot(touchX - pt.x, touchY - pt.y) <= touchRadius) {
                        haptics.tap(this)
                        when (actions[i]) {
                            PopoutAction.SCREENSHOT -> {
                                android.util.Log.i("AssistX", "ActionPopoutView: Screenshot clicked")
                                listener?.onScreenshotClicked()
                            }
                            PopoutAction.VOLUME -> {
                                android.util.Log.i("AssistX", "ActionPopoutView: Volume clicked")
                                listener?.onVolumeClicked()
                            }
                            PopoutAction.POWER -> {
                                android.util.Log.i("AssistX", "ActionPopoutView: Power clicked")
                                listener?.onPowerClicked()
                            }
                            PopoutAction.PAYMENT_PAUSE -> {
                                android.util.Log.i("AssistX", "ActionPopoutView: Payment Pause clicked")
                                listener?.onPaymentPauseClicked()
                            }
                        }
                        return true
                    }
                }
                // Tapped outside
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

        // 1. Scrim
        scrimPaint.alpha = (animProgress * 45).toInt().coerceIn(0, 45)
        canvas.drawRect(0f, 0f, w, h, scrimPaint)

        val radius = getCircleRadius()
        val points = updateCenterPoints()
        val actions = getActionOrder()
        val count = actions.size.coerceAtMost(buttonCenters.size)

        // 2. Background Capsule Element (if enabled)
        if (capsuleEnabled) {
            drawBackgroundCapsule(canvas, points, count, radius)
        }

        // 3. Draw Circular Action Buttons
        for (i in 0 until count) {
            val pt = points[i]
            val cx = pt.x
            val cy = pt.y
            val action = actions[i]
            val animOffset = i * 0.08f
            val isPower = (action == PopoutAction.POWER)
            val isPaymentPause = (action == PopoutAction.PAYMENT_PAUSE)

            drawCircularButton(
                canvas = canvas,
                cx = cx,
                cy = cy,
                radius = radius,
                animOffset = animOffset,
                isPower = isPower,
                isPaymentPause = isPaymentPause
            ) { c, btnCx, btnCy ->
                when (action) {
                    PopoutAction.SCREENSHOT -> drawCustomScreenshotIcon(c, btnCx, btnCy)
                    PopoutAction.VOLUME -> drawCustomVolumeIcon(c, btnCx, btnCy)
                    PopoutAction.POWER -> drawCustomPowerIcon(c, btnCx, btnCy)
                    PopoutAction.PAYMENT_PAUSE -> drawCustomPauseIcon(c, btnCx, btnCy)
                }
            }
        }
    }

    private fun drawBackgroundCapsule(canvas: Canvas, points: Array<PointF>, count: Int, radius: Float) {
        if (count <= 0) return
        var minX = points[0].x
        var maxX = points[0].x
        var minY = points[0].y
        var maxY = points[0].y
        for (i in 1 until count) {
            if (points[i].x < minX) minX = points[i].x
            if (points[i].x > maxX) maxX = points[i].x
            if (points[i].y < minY) minY = points[i].y
            if (points[i].y > maxY) maxY = points[i].y
        }
        val capPad = 8f * density

        val capLeft = minX - radius - capPad
        val capRight = maxX + radius + capPad
        val capTop = minY - radius - capPad
        val capBottom = maxY + radius + capPad
        capsuleRect.set(capLeft, capTop, capRight, capBottom)

        val cornerR = when (capsuleCornerRadius) {
            "METRO_SQUARE" -> 0f
            "ROUNDED" -> 14f * density
            else -> minOf(capRight - capLeft, capBottom - capTop) / 2f // "PILL"
        }

        when (capsuleStyle) {
            "METRO_BLACK" -> {
                capsuleBasePaint.color = Color.BLACK
                capsuleBasePaint.alpha = (animProgress * 240).toInt().coerceIn(0, 240)
                capsuleBorderPaint.color = accentColorInt
                capsuleBorderPaint.alpha = (animProgress * 255).toInt().coerceIn(0, 255)
            }
            "SOLID_ACCENT" -> {
                capsuleBasePaint.color = accentColorInt
                capsuleBasePaint.alpha = (animProgress * 230).toInt().coerceIn(0, 230)
                capsuleBorderPaint.color = Color.WHITE
                capsuleBorderPaint.alpha = (animProgress * 180).toInt().coerceIn(0, 180)
            }
            "OUTLINE_ONLY" -> {
                capsuleBasePaint.color = Color.TRANSPARENT
                capsuleBorderPaint.color = accentColorInt
                capsuleBorderPaint.alpha = (animProgress * 255).toInt().coerceIn(0, 255)
            }
            else -> { // "OBSIDIAN" default
                capsuleBasePaint.color = Color.argb(225, 18, 18, 22)
                capsuleBasePaint.alpha = (animProgress * 225).toInt().coerceIn(0, 225)
                capsuleBorderPaint.color = Color.argb(75, 255, 255, 255)
                capsuleBorderPaint.alpha = (animProgress * 75).toInt().coerceIn(0, 75)
            }
        }

        if (capsuleStyle != "OUTLINE_ONLY") {
            canvas.drawRoundRect(capsuleRect, cornerR, cornerR, capsuleBasePaint)
        }
        canvas.drawRoundRect(capsuleRect, cornerR, cornerR, capsuleBorderPaint)
    }

    private fun drawCircularButton(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        animOffset: Float,
        isPower: Boolean,
        isPaymentPause: Boolean = false,
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
        when {
            isPower -> {
                redAccentPaint.alpha = (itemProgress * 25).toInt().coerceIn(0, 25)
                redBorderPaint.alpha = (itemProgress * 80).toInt().coerceIn(0, 80)
                redIconPaint.alpha = alphaInt
                canvas.drawCircle(cx, cy, radius, redAccentPaint)
                canvas.drawCircle(cx, cy, radius, redBorderPaint)
            }
            isPaymentPause -> {
                amberAccentPaint.alpha = (itemProgress * 25).toInt().coerceIn(0, 25)
                amberBorderPaint.alpha = (itemProgress * 80).toInt().coerceIn(0, 80)
                amberIconPaint.alpha = alphaInt
                canvas.drawCircle(cx, cy, radius, amberAccentPaint)
                canvas.drawCircle(cx, cy, radius, amberBorderPaint)
            }
            else -> {
                whiteAccentPaint.alpha = (itemProgress * 18).toInt().coerceIn(0, 18)
                whiteBorderPaint.alpha = (itemProgress * 45).toInt().coerceIn(0, 45)
                whiteIconPaint.alpha = alphaInt
                canvas.drawCircle(cx, cy, radius, whiteAccentPaint)
                canvas.drawCircle(cx, cy, radius, whiteBorderPaint)
            }
        }

        // Custom Tinted Icon
        drawIcon(canvas, cx, cy)

        canvas.restore()
    }

    private fun drawCustomScreenshotIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bmp = screenshotBitmap ?: return
        val halfSpan = iconTargetSpan / 2f
        iconDstRect.set(cx - halfSpan, cy - halfSpan, cx + halfSpan, cy + halfSpan)
        canvas.drawBitmap(bmp, null, iconDstRect, whiteIconPaint)
    }

    private fun drawCustomVolumeIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bmp = volumeBitmap ?: return
        val halfSpan = iconTargetSpan / 2f
        iconDstRect.set(cx - halfSpan, cy - halfSpan, cx + halfSpan, cy + halfSpan)
        canvas.drawBitmap(bmp, null, iconDstRect, whiteIconPaint)
    }

    private fun drawCustomPowerIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bmp = powerBitmap ?: return
        val halfSpan = iconTargetSpan / 2f
        iconDstRect.set(cx - halfSpan, cy - halfSpan, cx + halfSpan, cy + halfSpan)
        canvas.drawBitmap(bmp, null, iconDstRect, redIconPaint)
    }

    private fun drawCustomPauseIcon(canvas: Canvas, cx: Float, cy: Float) {
        val bmp = pauseBitmap ?: return
        val halfSpan = iconTargetSpan / 2f
        iconDstRect.set(cx - halfSpan, cy - halfSpan, cx + halfSpan, cy + halfSpan)
        canvas.drawBitmap(bmp, null, iconDstRect, amberIconPaint)
    }
}
