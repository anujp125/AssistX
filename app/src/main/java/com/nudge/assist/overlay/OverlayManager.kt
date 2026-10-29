package com.nudge.assist.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import com.nudge.assist.accessibility.ActionExecutorContract
import com.nudge.assist.accessibility.AndroidActionExecutor
import com.nudge.assist.data.OverlayPreferences
import com.nudge.assist.data.UserPreferencesContract
import com.nudge.assist.service.AssistAccessibilityService
import com.nudge.assist.state.OverlayPermissionState
import com.nudge.assist.state.OverlayState
import com.nudge.assist.state.StateCoordinator
import com.nudge.assist.ui.floating.ActionPopoutView
import com.nudge.assist.ui.floating.FloatingButtonView
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
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : OverlayManagerContract {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var floatingView: FloatingButtonView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var popoutView: ActionPopoutView? = null
    private var popoutLayoutParams: WindowManager.LayoutParams? = null

    @Volatile
    override var isAttached: Boolean = false
        private set

    val isPopoutShowing: Boolean
        get() = popoutView != null

    private val buttonSizePx: Int
        get() {
            val density = context.resources.displayMetrics.density
            return (56 * density).toInt()
        }

    /**
     * Attaches the single floating overlay window if permissions exist and it is not already attached.
     * Returns true if attached successfully or already attached; false if blocked by permission or error.
     */
    override fun attachOverlay(): Boolean {
        if (isAttached) {
            return true
        }

        // Verify overlay permission before attempting WindowManager attachment
        val permission = permissionAdapter.checkPermission(context)
        stateCoordinator.updateOverlayPermission(permission)
        if (permission != OverlayPermissionState.GRANTED) {
            stateCoordinator.updateOverlayState(OverlayState.ERROR)
            return false
        }

        stateCoordinator.updateOverlayState(OverlayState.ATTACHING)

        return try {
            val displayMetrics = context.resources.displayMetrics
            val size = buttonSizePx

            // Default position: right edge, 40% of screen height
            val defaultX = displayMetrics.widthPixels - size
            val defaultY = (displayMetrics.heightPixels * 0.40f).toInt()

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
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = defaultX
                y = defaultY
            }

            val view = FloatingButtonView(context).apply {
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
            false
        }
    }

    /**
     * Removes the floating overlay window from WindowManager.
     */
    override fun detachOverlay(): Boolean {
        dismissPopout()

        if (!isAttached || floatingView == null) {
            stateCoordinator.updateOverlayState(OverlayState.DETACHED)
            return true
        }

        stateCoordinator.updateOverlayState(OverlayState.DETACHING)

        return try {
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
        // Primary Action: Lock Screen via Accessibility Service
        actionExecutor.lockScreen()
    }

    private fun handleLongPress() {
        // Navigation Action: Go Home via Accessibility Service
        actionExecutor.performHome()
    }

    /**
     * Displays the 2-action popout pill [ 📸 Shot ] and [ 🔊 Vol ] adjacent to the floating button.
     */
    fun showPopout() {
        if (!isAttached || floatingView == null) return
        if (popoutView != null) return

        val buttonParams = layoutParams ?: return
        val displayMetrics = context.resources.displayMetrics
        val density = displayMetrics.density
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val popoutWidth = (58 * density).toInt()
        val popoutHeight = (186 * density).toInt()
        val margin = (10 * density).toInt()
        val btnSize = buttonSizePx

        // Horizontally align popout directly with the floating button
        val popoutX = (buttonParams.x + (btnSize - popoutWidth) / 2)
            .coerceIn(margin, screenWidth - popoutWidth - margin)

        // Expand vertically downwards by default; if near screen bottom, expand upwards
        val navBarBuffer = (70 * density).toInt()
        val defaultDownwardsY = buttonParams.y + btnSize + margin
        val popoutY = if (defaultDownwardsY + popoutHeight <= screenHeight - navBarBuffer) {
            defaultDownwardsY
        } else {
            (buttonParams.y - popoutHeight - margin).coerceAtLeast(margin)
        }

        val params = WindowManager.LayoutParams(
            popoutWidth,
            popoutHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = popoutX
            y = popoutY
        }

        val popout = ActionPopoutView(context).apply {
            listener = object : ActionPopoutView.PopoutListener {
                override fun onScreenshotClicked() {
                    dismissPopout()
                    // Allow popout removal to finish before taking screenshot
                    floatingView?.postDelayed({
                        actionExecutor.takeScreenshot()
                    }, 150)
                }

                override fun onVolumeClicked() {
                    dismissPopout()
                    actionExecutor.openVolumePanel()
                }

                override fun onPowerClicked() {
                    dismissPopout()
                    actionExecutor.openPowerDialog()
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
    fun dismissPopout() {
        val popout = popoutView ?: return
        try {
            windowManager.removeView(popout)
        } catch (e: Exception) {
            // View might already be detached
        } finally {
            popoutView = null
            popoutLayoutParams = null
        }
    }

    private fun handleDragMoved(deltaX: Float, deltaY: Float) {
        val params = layoutParams ?: return
        val view = floatingView ?: return

        params.x = (params.x + deltaX.toInt())
        params.y = (params.y + deltaY.toInt())

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            // Ignored during drag animation transitions
        }
    }

    private fun handleDragReleased() {
        val params = layoutParams ?: return
        val view = floatingView ?: return
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val size = buttonSizePx

        // Determine nearest edge (left or right)
        val targetX = if (params.x + size / 2 < screenWidth / 2) {
            0
        } else {
            screenWidth - size
        }

        // Clamp Y coordinate inside display bounds
        val minY = 50
        val maxY = displayMetrics.heightPixels - size - 50
        params.y = params.y.coerceIn(minY, maxY)

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

    private fun restoreSavedPositionAsync() {
        scope.launch(Dispatchers.IO) {
            val pos = preferences.positionFlow.first()
            if (pos.x >= 0 && pos.y >= 0) {
                withContext(Dispatchers.Main) {
                    val params = layoutParams ?: return@withContext
                    val view = floatingView ?: return@withContext
                    params.x = pos.x
                    params.y = pos.y
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
