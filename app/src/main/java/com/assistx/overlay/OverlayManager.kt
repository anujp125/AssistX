package com.assistx.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
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
        val displayMetrics = context.resources.displayMetrics
        var screenW = displayMetrics.widthPixels
        var screenH = displayMetrics.heightPixels
        var insetLeft = 0
        var insetTop = 0
        var insetRight = 0
        var insetBottom = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val windowMetrics = windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                screenW = bounds.width()
                screenH = bounds.height()

                val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                    android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout()
                )
                insetLeft = insets.left
                insetTop = insets.top
                insetRight = insets.right
                insetBottom = insets.bottom
            } catch (e: Exception) {
                // Fallback to display metrics
            }
        }

        val density = displayMetrics.density
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

                    override fun onDragMoved(deltaX: Float, deltaY: Float) {
                        dismissPopout()
                        handleDragMoved(deltaX, deltaY)
                    }

                    override fun onDragReleased() {
                        handleDragReleased()
                    }
                }
            }

            windowManager.addView(view, params)
            floatingView = view
            layoutParams = params
            isAttached = true
            stateCoordinator.updateOverlayState(OverlayState.ATTACHED)

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

    private fun handleDragMoved(deltaX: Float, deltaY: Float) = synchronized(windowLock) {
        val params = layoutParams ?: return@synchronized
        val view = floatingView ?: return@synchronized

        params.x = (params.x + deltaX.toInt())
        params.y = (params.y + deltaY.toInt())

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            // Ignored during drag animation transitions
        }
    }

    private fun handleDragReleased() = synchronized(windowLock) {
        val params = layoutParams ?: return@synchronized
        val view = floatingView ?: return@synchronized
        val size = buttonSizePx
        val bounds = getSafeBounds(size)

        // Clamp Y coordinate inside safe display bounds
        params.y = params.y.coerceIn(bounds.minY, bounds.maxY)

        if (!currentSettings.snapToEdgeEnabled) {
            params.x = params.x.coerceIn(bounds.minX, bounds.maxX)
            try {
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                // View may be detaching
            }
            scope.launch(Dispatchers.IO) {
                preferences.savePosition(params.x, params.y)
            }
            return@synchronized
        }

        // Determine nearest safe edge (left or right)
        val targetX = if (params.x + size / 2 < bounds.screenWidth / 2) {
            bounds.minX
        } else {
            bounds.maxX
        }

        // Animate snap to edge
        val startX = params.x
        ValueAnimator.ofInt(startX, targetX).apply {
            duration = 250
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                params.x = animator.animatedValue as Int
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    // View may be detaching
                }
            }
            start()
        }

        // Persist new coordinates in preferences
        scope.launch(Dispatchers.IO) {
            preferences.savePosition(targetX, params.y)
        }
    }

    override fun onConfigurationChanged() {
        synchronized(windowLock) {
            val view = floatingView ?: return
            val params = layoutParams ?: return
            if (!isAttached) return

            dismissPopout()

            val size = buttonSizePx
            val bounds = getSafeBounds(size)

            params.y = params.y.coerceIn(bounds.minY, bounds.maxY)
            val centerX = params.x + (size / 2)
            params.x = if (centerX < bounds.screenWidth / 2) {
                bounds.minX
            } else {
                bounds.maxX
            }

            try {
                windowManager.updateViewLayout(view, params)
                scope.launch(Dispatchers.IO) {
                    preferences.savePosition(params.x, params.y)
                }
            } catch (e: Exception) {
                // Layout transition
            }
        }
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

                        params.x = pos.x.coerceIn(bounds.minX, bounds.maxX)
                        params.y = pos.y.coerceIn(bounds.minY, bounds.maxY)
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
