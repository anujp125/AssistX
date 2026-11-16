package com.assistx.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.Gravity
import android.view.Surface
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import com.assistx.accessibility.ActionDispatcher
import com.assistx.accessibility.ActionDispatcherContract
import com.assistx.accessibility.ActionExecutorContract
import com.assistx.accessibility.AccessibilityStateManager
import com.assistx.accessibility.AccessibilityStateManagerContract
import com.assistx.accessibility.AndroidActionExecutor
import com.assistx.accessibility.AssistAction
import com.assistx.data.OverlayPreferences
import com.assistx.data.UserPreferencesContract
import com.assistx.service.AssistAccessibilityService
import com.assistx.state.OverlayPermissionState
import com.assistx.state.OverlayState
import com.assistx.state.StateCoordinator
import com.assistx.ui.floating.AccessibilityRequiredOverlayDialog
import com.assistx.ui.floating.ActionPopoutView
import com.assistx.ui.floating.FloatingButtonView
import com.assistx.ui.floating.PaymentSafetyOverlayDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages the lifecycle, positioning, WindowManager attachment, and snap-to-edge
 * animation of the AssistX floating overlay window.
 *
 * Guarantees the single-overlay invariant: repeated calls to attach will never create
 * multiple overlay views on screen.
 */
class OverlayManager(
    private val context: Context,
    private val stateCoordinator: StateCoordinator,
    private val permissionAdapter: OverlayPermissionAdapter = AndroidOverlayPermissionAdapter(),
    private val preferences: UserPreferencesContract = OverlayPreferences(context),
    private val actionExecutor: ActionExecutorContract = AndroidActionExecutor(context),
    private val accessibilityStateManager: AccessibilityStateManagerContract? = null,
    private val actionDispatcher: ActionDispatcherContract? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : OverlayManagerContract {

    interface FailureListener {
        fun onOverlayInterrupted(reason: String)
    }

    var failureListener: FailureListener? = null

    private val windowLock = Any()
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var floatingView: FloatingButtonView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var popoutView: ActionPopoutView? = null
    private var popoutLayoutParams: WindowManager.LayoutParams? = null

    private var accessibilityDialogView: AccessibilityRequiredOverlayDialog? = null
    private var accessibilityDialogLayoutParams: WindowManager.LayoutParams? = null

    val isAccessibilityDialogShowing: Boolean
        get() = accessibilityDialogView != null

    private var paymentSafetyDialogView: PaymentSafetyOverlayDialog? = null
    private var paymentSafetyDialogLayoutParams: WindowManager.LayoutParams? = null

    val isPaymentSafetyDialogShowing: Boolean
        get() = paymentSafetyDialogView != null

    var onPauseForPaymentsRequested: (() -> Unit)? = null

    private val effectiveA11yManager: AccessibilityStateManagerContract by lazy {
        accessibilityStateManager ?: AccessibilityStateManager(
            context = context,
            stateCoordinator = stateCoordinator,
            diagnosticRecorder = com.assistx.AssistApplication.instance.diagnosticRecorder
        )
    }

    private val effectiveActionDispatcher: ActionDispatcherContract by lazy {
        actionDispatcher ?: ActionDispatcher(
            actionExecutor = actionExecutor,
            accessibilityStateManager = effectiveA11yManager,
            diagnosticRecorder = com.assistx.AssistApplication.instance.diagnosticRecorder
        )
    }

    @Volatile
    override var isAttached: Boolean = false
        private set

    private var isDetachedForScreenOff = false
    private var isAnchoredToRight: Boolean = true
    private var lastScreenWidth: Int = 0
    private var lastScreenHeight: Int = 0

    private var isUserInteracting: Boolean = false
    private var pendingOrientationCheckRunnable: Runnable? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var snapAnimator: ValueAnimator? = null

    private val displayManager by lazy {
        context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            handleOrientationOrDisplayChange()
        }
    }

    private val componentCallbacks = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
            handleOrientationOrDisplayChange()
        }
        override fun onLowMemory() {}
        override fun onTrimMemory(level: Int) {}
    }

    val isPopoutShowing: Boolean
        get() = popoutView != null

    private var currentSettings = com.assistx.data.OverlaySettings()

    private val buttonSizePx: Int
        get() {
            val density = context.resources.displayMetrics.density
            return (currentSettings.buttonSizeDp * density).toInt()
        }

    data class ScreenBounds(
        val minX: Int,
        val maxX: Int,
        val minY: Int,
        val maxY: Int,
        val screenWidth: Int,
        val screenHeight: Int
    )

    fun getSafeBounds(sizePx: Int): ScreenBounds {
        val display = floatingView?.display
            ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.display
                } catch (e: Exception) {
                    displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
                }
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            } ?: displayManager?.getDisplay(Display.DEFAULT_DISPLAY)

        val rotation = display?.rotation ?: Surface.ROTATION_0
        val isLandscape = (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)

        val realMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        display?.getRealMetrics(realMetrics)

        var rawW = realMetrics.widthPixels
        var rawH = realMetrics.heightPixels

        if (rawW <= 0 || rawH <= 0) {
            val dm = context.resources.displayMetrics
            rawW = dm.widthPixels
            rawH = dm.heightPixels
        }

        // True physical screen width and height tied to hardware orientation.
        // Guarantees landscape mode always receives the full long dimension,
        // eliminating race conditions from stale Application-context metrics.
        val screenW = if (isLandscape) {
            maxOf(rawW, rawH)
        } else {
            minOf(rawW, rawH)
        }

        val screenH = if (isLandscape) {
            minOf(rawW, rawH)
        } else {
            maxOf(rawW, rawH)
        }

        // Determine display cutouts and system insets
        var insetLeft = 0
        var insetTop = 0
        var insetRight = 0
        var insetBottom = 0

        val cutout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            display?.cutout
        } else null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val viewInsets = floatingView?.takeIf { it.isAttachedToWindow }?.rootWindowInsets?.getInsetsIgnoringVisibility(
                android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout()
            )
            if (viewInsets != null) {
                insetLeft = maxOf(viewInsets.left, cutout?.safeInsetLeft ?: 0)
                insetTop = maxOf(viewInsets.top, cutout?.safeInsetTop ?: 0)
                insetRight = maxOf(viewInsets.right, cutout?.safeInsetRight ?: 0)
                insetBottom = maxOf(viewInsets.bottom, cutout?.safeInsetBottom ?: 0)
            } else {
                insetLeft = cutout?.safeInsetLeft ?: 0
                insetTop = cutout?.safeInsetTop ?: 0
                insetRight = cutout?.safeInsetRight ?: 0
                insetBottom = cutout?.safeInsetBottom ?: 0
            }
        } else {
            insetLeft = cutout?.safeInsetLeft ?: 0
            insetTop = cutout?.safeInsetTop ?: 0
            insetRight = cutout?.safeInsetRight ?: 0
            insetBottom = cutout?.safeInsetBottom ?: 0
        }

        val density = context.resources.displayMetrics.density
        val padding = (8 * density).toInt()

        val minX = insetLeft + padding
        val maxX = (screenW - insetRight - sizePx - padding).coerceAtLeast(minX)
        val minY = insetTop + padding
        val maxY = (screenH - insetBottom - sizePx - padding).coerceAtLeast(minY)

        return ScreenBounds(minX, maxX, minY, maxY, screenW, screenH)
    }

    init {
        scope.launch {
            preferences.settingsFlow.collect { settings ->
                currentSettings = settings
                applySettings(settings)
            }
        }
    }

    private fun applySettings(settings: com.assistx.data.OverlaySettings) = synchronized(windowLock) {
        val view = floatingView ?: return@synchronized
        val params = layoutParams ?: return@synchronized
        if (!isAttached) return@synchronized

        val newSizePx = (settings.buttonSizeDp * context.resources.displayMetrics.density).toInt()
        var needsRelayout = false

        if (params.width != newSizePx || params.height != newSizePx) {
            params.width = newSizePx
            params.height = newSizePx
            needsRelayout = true
        }

        if (needsRelayout) {
            try {
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                // Layout transition
            }
        }

        view.updateSettings(
            idleOpacityPercent = settings.idleOpacityPercent,
            idleFadeSeconds = settings.idleFadeSeconds,
            hapticFeedback = settings.hapticFeedbackEnabled,
            snapToEdge = settings.snapToEdgeEnabled,
            accentHex = settings.accentHex,
            buttonShape = settings.buttonShape,
            buttonStyle = settings.buttonStyle,
            buttonColorMode = settings.buttonColorMode
        )
    }

    /**
     * Attaches the single floating overlay window if permissions exist and it is not already attached.
     * Thread-safe and atomic; guarantees the single-overlay invariant.
     */
    override fun attachOverlay(): Boolean = synchronized(windowLock) {
        if (isAttached) {
            return@synchronized true
        }

        // Verify overlay permission before attempting WindowManager attachment
        val permission = permissionAdapter.checkPermission(context)
        stateCoordinator.updateOverlayPermission(permission)
        if (permission != OverlayPermissionState.GRANTED) {
            stateCoordinator.updateOverlayState(OverlayState.ERROR)
            return@synchronized false
        }

        stateCoordinator.updateOverlayState(OverlayState.ATTACHING)

        return@synchronized try {
            val size = buttonSizePx
            val bounds = getSafeBounds(size)

            val defaultX = bounds.maxX
            val defaultY = (bounds.screenHeight * 0.40f).toInt().coerceIn(bounds.minY, bounds.maxY)
            lastScreenWidth = bounds.screenWidth
            lastScreenHeight = bounds.screenHeight
            isAnchoredToRight = true

            val params = WindowManager.LayoutParams(
                size,
                size,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                }
                x = defaultX
                y = defaultY
            }

            val view = FloatingButtonView(context).apply {
                updateSettings(
                    idleOpacityPercent = currentSettings.idleOpacityPercent,
                    idleFadeSeconds = currentSettings.idleFadeSeconds,
                    hapticFeedback = currentSettings.hapticFeedbackEnabled,
                    snapToEdge = currentSettings.snapToEdgeEnabled,
                    accentHex = currentSettings.accentHex,
                    buttonShape = currentSettings.buttonShape,
                    buttonStyle = currentSettings.buttonStyle,
                    buttonColorMode = currentSettings.buttonColorMode
                )
                listener = object : FloatingButtonView.InteractionListener {
                    override fun onSingleTap() {
                        handleSingleTap()
                    }

                    override fun onDoubleTap() {
                        dismissPopout()
                        handleDoubleTap()
                    }

                    override fun onLongPress() {
                        dismissPopout()
                        handleLongPress()
                    }

                    override fun onDragStarted() {
                        synchronized(windowLock) {
                            isUserInteracting = true
                            snapAnimator?.cancel()
                            snapAnimator = null
                            pendingOrientationCheckRunnable?.let { mainHandler.removeCallbacks(it) }
                        }
                    }

                    override fun onDragMoved(deltaX: Float, deltaY: Float) {
                        dismissPopout()
                        handleDragMoved(deltaX, deltaY)
                    }

                    override fun onDragReleased() {
                        handleDragReleased()
                    }

                    override fun onOrientationChanged() {
                        handleOrientationOrDisplayChange()
                    }
                }
            }

            windowManager.addView(view, params)
            floatingView = view
            layoutParams = params
            isAttached = true
            stateCoordinator.updateOverlayState(OverlayState.ATTACHED)

            // Register dynamic display and component orientation observers
            displayManager?.registerDisplayListener(displayListener, mainHandler)
            try {
                context.registerComponentCallbacks(componentCallbacks)
            } catch (e: Exception) {
                // Ignore if context does not support callbacks
            }

            // Restore saved coordinates asynchronously if available
            restoreSavedPositionAsync()

            true
        } catch (e: Exception) {
            isAttached = false
            floatingView = null
            layoutParams = null
            stateCoordinator.updateOverlayState(OverlayState.ERROR)
            failureListener?.onOverlayInterrupted(e.message ?: "WindowManager addView failed")
            false
        }
    }

    /**
     * Removes the floating overlay window from WindowManager.
     */
    override fun detachOverlay(): Boolean = synchronized(windowLock) {
        dismissPopout()
        dismissAccessibilityDialog()
        snapAnimator?.cancel()
        snapAnimator = null
        pendingOrientationCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        isUserInteracting = false

        displayManager?.unregisterDisplayListener(displayListener)
        try {
            context.unregisterComponentCallbacks(componentCallbacks)
        } catch (e: Exception) {
            // Ignore
        }

        dismissPaymentSafetyDialog()
        dismissAccessibilityDialog()
        dismissPopout()

        if (!isAttached || floatingView == null) {
            stateCoordinator.updateOverlayState(OverlayState.DETACHED)
            return@synchronized true
        }

        stateCoordinator.updateOverlayState(OverlayState.DETACHING)

        return@synchronized try {
            windowManager.removeView(floatingView)
            floatingView = null
            layoutParams = null
            isAttached = false
            stateCoordinator.updateOverlayState(OverlayState.DETACHED)
            true
        } catch (e: Exception) {
            floatingView = null
            layoutParams = null
            isAttached = false
            stateCoordinator.updateOverlayState(OverlayState.ERROR)
            false
        }
    }

    private fun handleSingleTap() {
        android.util.Log.i("AssistX", "OverlayManager: handleSingleTap called, popoutView=$popoutView")
        if (popoutView != null) {
            dismissPopout()
        } else {
            showPopout()
        }
    }

    private fun handleDoubleTap() {
        // Primary Action: Lock Screen via ActionDispatcher
        effectiveActionDispatcher.dispatch(AssistAction.LOCK_SCREEN, context) {
            showAccessibilityRequiredDialog()
        }
    }

    private fun handleLongPress() {
        // Navigation Action: Go Home via ActionDispatcher
        effectiveActionDispatcher.dispatch(AssistAction.HOME, context) {
            showAccessibilityRequiredDialog()
        }
    }

    /**
     * Displays the action popout pill adjacent to the floating button.
     * Thread-safe; respects orientation (vertical or horizontal) and screen bounds.
     */
    fun showPopout() = synchronized(windowLock) {
        if (!isAttached || floatingView == null) return@synchronized
        if (popoutView != null) return@synchronized

        val buttonParams = layoutParams ?: return@synchronized

        // Full-screen touch interceptor window to provide a modal dismiss scrim
        // and protect background apps from accidental touches while popout is active
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            x = 0
            y = 0
        }

        val location = IntArray(2)
        val buttonView = floatingView
        val (btnX, btnY) = if (buttonView != null && buttonView.isAttachedToWindow) {
            buttonView.getLocationOnScreen(location)
            Pair(location[0].toFloat(), location[1].toFloat())
        } else {
            Pair(buttonParams.x.toFloat(), buttonParams.y.toFloat())
        }
        val btnW = (buttonView?.width?.takeIf { it > 0 } ?: buttonSizePx).toFloat()
        val btnH = (buttonView?.height?.takeIf { it > 0 } ?: buttonSizePx).toFloat()

        val popout = ActionPopoutView(context).apply {
            setDismissDelay(currentSettings.popoutDismissSeconds)
            setHapticFeedbackPreference(currentSettings.hapticFeedbackEnabled)
            configureCustomizations(
                buttonSizeDp = currentSettings.popoutButtonSizeDp,
                orientation = currentSettings.popoutOrientation,
                itemSpacingDp = currentSettings.popoutItemSpacingDp,
                marginDp = currentSettings.popoutMarginDp,
                order = currentSettings.popoutOrder,
                showPaymentPause = currentSettings.showPaymentPauseInPopout,
                capsuleEnabled = currentSettings.capsuleEnabled,
                capsuleStyle = currentSettings.capsuleStyle,
                capsuleCornerRadius = currentSettings.capsuleCornerRadius,
                accentHex = currentSettings.accentHex
            )
            configureAnchor(
                btnX = btnX,
                btnY = btnY,
                btnWidth = btnW,
                btnHeight = btnH
            )
            listener = object : ActionPopoutView.PopoutListener {
                override fun onScreenshotClicked() {
                    dismissPopout()
                    floatingView?.postDelayed({
                        effectiveActionDispatcher.dispatch(AssistAction.TAKE_SCREENSHOT, context) {
                            showAccessibilityRequiredDialog()
                        }
                    }, 150)
                }

                override fun onVolumeClicked() {
                    dismissPopout()
                    effectiveActionDispatcher.dispatch(AssistAction.VOLUME_CONTROL, context)
                }

                override fun onPowerClicked() {
                    dismissPopout()
                    effectiveActionDispatcher.dispatch(AssistAction.POWER_DIALOG, context) {
                        showAccessibilityRequiredDialog()
                    }
                }

                override fun onPaymentPauseClicked() {
                    dismissPopout()
                    floatingView?.postDelayed({
                        showPaymentSafetyDialog()
                    }, 100)
                }

                override fun onDismissRequested() {
                    dismissPopout()
                }
            }
        }

        try {
            windowManager.addView(popout, params)
            popoutView = popout
            popoutLayoutParams = params
        } catch (e: Exception) {
            popoutView = null
            popoutLayoutParams = null
        }
    }

    /**
     * Dismisses the action popout pill if currently showing.
     */
    fun dismissPopout() = synchronized(windowLock) {
        val popout = popoutView ?: return@synchronized
        try {
            windowManager.removeView(popout)
        } catch (e: Exception) {
            // View might already be detached
        } finally {
            popoutView = null
            popoutLayoutParams = null
        }
    }

    /**
     * Displays the Metro-styled Accessibility Required overlay dialog.
     * Prevents duplicate dialogs if already visible.
     */
    fun showAccessibilityRequiredDialog() = synchronized(windowLock) {
        if (!isAttached) return@synchronized
        if (accessibilityDialogView != null) return@synchronized // Guard against duplicates

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        val dialog = AccessibilityRequiredOverlayDialog(
            context = context,
            accentHex = currentSettings.accentHex,
            onGoToSettings = {
                dismissAccessibilityDialog()
                effectiveA11yManager.openAccessibilitySettings(context)
            },
            onCancel = {
                dismissAccessibilityDialog()
            }
        )

        try {
            windowManager.addView(dialog, params)
            accessibilityDialogView = dialog
            accessibilityDialogLayoutParams = params
        } catch (e: Exception) {
            accessibilityDialogView = null
            accessibilityDialogLayoutParams = null
        }
    }

    /**
     * Dismisses the Accessibility Required overlay dialog if currently showing.
     */
    fun dismissAccessibilityDialog() = synchronized(windowLock) {
        val dialog = accessibilityDialogView ?: return@synchronized
        try {
            if (dialog.isAttachedToWindow) {
                windowManager.removeView(dialog)
            }
        } catch (e: Exception) {
            // View might already be detached
        } finally {
            accessibilityDialogView = null
            accessibilityDialogLayoutParams = null
        }
    }

    /**
     * Displays the Metro-styled Payment Safety double-approval overlay dialog.
     * Prevents duplicate dialogs if already visible.
     */
    fun showPaymentSafetyDialog() = synchronized(windowLock) {
        if (!isAttached) return@synchronized
        if (paymentSafetyDialogView != null) return@synchronized // Guard against duplicates

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        val dialog = PaymentSafetyOverlayDialog(
            context = context,
            onConfirmPause = {
                dismissPaymentSafetyDialog()
                onPauseForPaymentsRequested?.invoke()
            },
            onCancel = {
                dismissPaymentSafetyDialog()
            }
        )

        try {
            windowManager.addView(dialog, params)
            paymentSafetyDialogView = dialog
            paymentSafetyDialogLayoutParams = params
        } catch (e: Exception) {
            paymentSafetyDialogView = null
            paymentSafetyDialogLayoutParams = null
        }
    }

    /**
     * Dismisses the Payment Safety overlay dialog if currently showing.
     */
    fun dismissPaymentSafetyDialog() = synchronized(windowLock) {
        val dialog = paymentSafetyDialogView ?: return@synchronized
        try {
            if (dialog.isAttachedToWindow) {
                windowManager.removeView(dialog)
            }
        } catch (e: Exception) {
            // View might already be detached
        } finally {
            paymentSafetyDialogView = null
            paymentSafetyDialogLayoutParams = null
        }
    }

    private fun handleDragMoved(deltaX: Float, deltaY: Float) = synchronized(windowLock) {
        val params = layoutParams ?: return@synchronized
        val view = floatingView ?: return@synchronized
        val bounds = getSafeBounds(buttonSizePx)

        // Softly clamp X during drag allowing slight overshoot for natural touch feel
        val overscroll = buttonSizePx / 3
        params.x = (params.x + deltaX.toInt()).coerceIn(bounds.minX - overscroll, bounds.maxX + overscroll)
        params.y = (params.y + deltaY.toInt()).coerceIn(bounds.minY, bounds.maxY)

        // Dynamically track anchor intent during drag
        isAnchoredToRight = (params.x + buttonSizePx / 2) >= (bounds.screenWidth / 2)

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            // Ignored during drag animation transitions
        }
    }

    private fun handleDragReleased() = synchronized(windowLock) {
        isUserInteracting = false
        val params = layoutParams ?: return@synchronized
        val view = floatingView ?: return@synchronized
        val size = buttonSizePx
        val bounds = getSafeBounds(size)

        // Clamp Y coordinate inside safe display bounds
        params.y = params.y.coerceIn(bounds.minY, bounds.maxY)
        lastScreenWidth = bounds.screenWidth
        lastScreenHeight = bounds.screenHeight

        val isRight = (params.x + size / 2) >= (bounds.screenWidth / 2)
        isAnchoredToRight = isRight

        if (!currentSettings.snapToEdgeEnabled) {
            params.x = params.x.coerceIn(bounds.minX, bounds.maxX)
            try {
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                // View may be detaching
            }
            scope.launch(Dispatchers.IO) {
                preferences.savePosition(
                    params.x,
                    params.y,
                    isAnchoredToRight,
                    bounds.screenWidth,
                    bounds.screenHeight
                )
            }
            return@synchronized
        }

        // Determine nearest safe edge (left or right)
        val targetX = if (isRight) bounds.maxX else bounds.minX

        // Cancel previous snap animator
        snapAnimator?.cancel()
        val startX = params.x
        snapAnimator = ValueAnimator.ofInt(startX, targetX).apply {
            duration = 250
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                synchronized(windowLock) {
                    if (isAttached && floatingView === view) {
                        params.x = (animator.animatedValue as Int).coerceIn(bounds.minX, bounds.maxX)
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (e: Exception) {
                            // View may be detaching
                        }
                    }
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    synchronized(windowLock) {
                        if (snapAnimator === animation) {
                            snapAnimator = null
                        }
                    }
                }
            })
            start()
        }

        // Persist new coordinates in preferences with orientation context
        scope.launch(Dispatchers.IO) {
            preferences.savePosition(
                targetX,
                params.y,
                isAnchoredToRight,
                bounds.screenWidth,
                bounds.screenHeight
            )
        }
    }

    override fun onConfigurationChanged() {
        handleOrientationOrDisplayChange()
    }

    private fun handleOrientationOrDisplayChange() {
        pendingOrientationCheckRunnable?.let { mainHandler.removeCallbacks(it) }

        mainHandler.post {
            synchronized(windowLock) {
                val view = floatingView ?: return@synchronized
                val params = layoutParams ?: return@synchronized
                if (!isAttached) return@synchronized

                snapAnimator?.cancel()
                snapAnimator = null
                dismissPopout()

                val size = buttonSizePx
                val bounds = getSafeBounds(size)
                if (bounds.screenWidth <= 0 || bounds.screenHeight <= 0) return@synchronized

                // Adapt Y coordinate preserving relative vertical position
                if (lastScreenHeight > 0 && lastScreenHeight != bounds.screenHeight) {
                    val relativeY = params.y.toFloat() / lastScreenHeight.toFloat()
                    params.y = (relativeY * bounds.screenHeight).toInt().coerceIn(bounds.minY, bounds.maxY)
                } else {
                    params.y = params.y.coerceIn(bounds.minY, bounds.maxY)
                }

                // Adapt X coordinate based on edge anchor or relative ratio
                if (currentSettings.snapToEdgeEnabled) {
                    params.x = if (isAnchoredToRight) bounds.maxX else bounds.minX
                } else {
                    if (lastScreenWidth > 0 && lastScreenWidth != bounds.screenWidth) {
                        val relativeX = params.x.toFloat() / lastScreenWidth.toFloat()
                        params.x = (relativeX * bounds.screenWidth).toInt().coerceIn(bounds.minX, bounds.maxX)
                    } else {
                        params.x = params.x.coerceIn(bounds.minX, bounds.maxX)
                    }
                }

                // Strict safety constraint: button must NEVER be outside safe bounds
                params.x = params.x.coerceIn(bounds.minX, bounds.maxX)

                lastScreenWidth = bounds.screenWidth
                lastScreenHeight = bounds.screenHeight

                try {
                    windowManager.updateViewLayout(view, params)
                    scope.launch(Dispatchers.IO) {
                        preferences.savePosition(
                            params.x,
                            params.y,
                            isAnchoredToRight,
                            bounds.screenWidth,
                            bounds.screenHeight
                        )
                    }
                } catch (e: Exception) {
                    // Layout transition
                }
            }
        }

        // Secondary settlement pass after system bars finish transition (120ms)
        val runnable = Runnable {
            synchronized(windowLock) {
                if (!isAttached || isUserInteracting) return@synchronized
                val view = floatingView ?: return@synchronized
                val params = layoutParams ?: return@synchronized
                val size = buttonSizePx
                val latestBounds = getSafeBounds(size)

                if (latestBounds.screenWidth > 0 && latestBounds.screenHeight > 0) {
                    val targetX = if (currentSettings.snapToEdgeEnabled) {
                        if (isAnchoredToRight) latestBounds.maxX else latestBounds.minX
                    } else {
                        params.x.coerceIn(latestBounds.minX, latestBounds.maxX)
                    }
                    val targetY = if (lastScreenHeight > 0 && lastScreenHeight != latestBounds.screenHeight) {
                        val relY = params.y.toFloat() / lastScreenHeight.toFloat()
                        (relY * latestBounds.screenHeight).toInt().coerceIn(latestBounds.minY, latestBounds.maxY)
                    } else {
                        params.y.coerceIn(latestBounds.minY, latestBounds.maxY)
                    }

                    if (params.x != targetX || params.y != targetY) {
                        params.x = targetX
                        params.y = targetY
                        lastScreenWidth = latestBounds.screenWidth
                        lastScreenHeight = latestBounds.screenHeight
                        try {
                            windowManager.updateViewLayout(view, params)
                            scope.launch(Dispatchers.IO) {
                                preferences.savePosition(
                                    params.x,
                                    params.y,
                                    isAnchoredToRight,
                                    latestBounds.screenWidth,
                                    latestBounds.screenHeight
                                )
                            }
                        } catch (e: Exception) {
                            // Layout transition
                        }
                    }
                }
            }
        }
        pendingOrientationCheckRunnable = runnable
        mainHandler.postDelayed(runnable, 120)
    }

    override fun onScreenTurnedOff() {
        synchronized(windowLock) {
            if (isAttached) {
                android.util.Log.i("AssistX", "OverlayManager: Screen off received, detaching overlay")
                isDetachedForScreenOff = true
                detachOverlay()
            }
        }
    }

    override fun onScreenWoken() {
        synchronized(windowLock) {
            if (isDetachedForScreenOff) {
                android.util.Log.i("AssistX", "OverlayManager: Screen wake received, reattaching overlay")
                isDetachedForScreenOff = false
                attachOverlay()
            }
        }
    }

    private fun restoreSavedPositionAsync() {
        scope.launch(Dispatchers.IO) {
            val pos = preferences.positionFlow.first()
            if (pos.x >= 0 && pos.y >= 0) {
                withContext(Dispatchers.Main) {
                    synchronized(windowLock) {
                        val params = layoutParams ?: return@withContext
                        val view = floatingView ?: return@withContext
                        val size = buttonSizePx
                        val bounds = getSafeBounds(size)

                        isAnchoredToRight = pos.isAnchoredToRight

                        // Preserve relative vertical position if screen height was recorded
                        params.y = if (pos.lastScreenHeight > 0 && pos.lastScreenHeight != bounds.screenHeight) {
                            val relY = pos.y.toFloat() / pos.lastScreenHeight.toFloat()
                            (relY * bounds.screenHeight).toInt().coerceIn(bounds.minY, bounds.maxY)
                        } else {
                            pos.y.coerceIn(bounds.minY, bounds.maxY)
                        }

                        // Restore horizontal position
                        params.x = if (currentSettings.snapToEdgeEnabled) {
                            if (isAnchoredToRight) bounds.maxX else bounds.minX
                        } else {
                            if (pos.lastScreenWidth > 0 && pos.lastScreenWidth != bounds.screenWidth) {
                                val relX = pos.x.toFloat() / pos.lastScreenWidth.toFloat()
                                (relX * bounds.screenWidth).toInt().coerceIn(bounds.minX, bounds.maxX)
                            } else {
                                pos.x.coerceIn(bounds.minX, bounds.maxX)
                            }
                        }
                        params.x = params.x.coerceIn(bounds.minX, bounds.maxX)

                        lastScreenWidth = bounds.screenWidth
                        lastScreenHeight = bounds.screenHeight
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (e: Exception) {
                            // View may not be active
                        }
                    }
                }
            }
        }
    }
}
