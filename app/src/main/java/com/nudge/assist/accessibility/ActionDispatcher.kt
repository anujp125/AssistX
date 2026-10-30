package com.nudge.assist.accessibility

import android.content.Context
import com.nudge.assist.data.diagnostics.DiagnosticRecorder
import com.nudge.assist.state.AccessibilityState

/**
 * Result of attempting to dispatch an AssistX action.
 */
enum class ExecutionResult {
    /** The action was successfully executed. */
    SUCCESS,

    /** The action was blocked because Accessibility is disabled or disconnected. */
    PERMISSION_REQUIRED,

    /** The action was dispatched but execution failed at the platform level. */
    FAILED
}

/**
 * Central action dispatcher contract.
 * Guarantees that all accessibility checks and gating occur in one centralized place.
 */
interface ActionDispatcherContract {
    fun dispatch(
        action: AssistAction,
        context: Context,
        onPermissionRequired: ((AssistAction) -> Unit)? = null
    ): ExecutionResult
}

/**
 * Default implementation of [ActionDispatcherContract].
 *
 * Rules:
 * 1. If an action does NOT require accessibility, it executes immediately without showing any popup.
 * 2. If an action DOES require accessibility:
 *    - Rechecks the fresh, non-stale accessibility state.
 *    - If ENABLED_CONNECTED, executes via [ActionExecutorContract].
 *    - If DISABLED or ENABLED_DISCONNECTED, blocks execution, logs diagnostics, and invokes [onPermissionRequired].
 * 3. Never stores or replays the failed action when returning from Settings (prevents accidental execution).
 */
class ActionDispatcher(
    private val actionExecutor: ActionExecutorContract,
    private val accessibilityStateManager: AccessibilityStateManagerContract,
    private val diagnosticRecorder: DiagnosticRecorder
) : ActionDispatcherContract {

    override fun dispatch(
        action: AssistAction,
        context: Context,
        onPermissionRequired: ((AssistAction) -> Unit)?
    ): ExecutionResult {
        // Step 1: If the action does not require accessibility, execute normally
        if (!action.requiresAccessibility) {
            val executed = executeAction(action)
            return if (executed) ExecutionResult.SUCCESS else ExecutionResult.FAILED
        }

        // Step 2: Fresh real-time accessibility state check before executing
        val state = accessibilityStateManager.checkCurrentState(context)
        if (state == AccessibilityState.ENABLED_CONNECTED) {
            val executed = executeAction(action)
            diagnosticRecorder.recordEvent(
                eventType = "ACTION_EXECUTED",
                component = "ACTION_DISPATCHER",
                details = "Executed ${action.id} successfully"
            )
            return if (executed) ExecutionResult.SUCCESS else ExecutionResult.FAILED
        } else {
            // Blocked: service is disabled or disconnected
            diagnosticRecorder.recordEvent(
                eventType = "ACTION_BLOCKED_ACCESSIBILITY_REQUIRED",
                component = "ACTION_DISPATCHER",
                details = "Blocked ${action.id}: Accessibility service is $state"
            )
            onPermissionRequired?.invoke(action)
            // Critical: Never store the action to replay later.
            // When returning from Settings, user must explicitly perform the gesture again.
            return ExecutionResult.PERMISSION_REQUIRED
        }
    }

    private fun executeAction(action: AssistAction): Boolean {
        return when (action) {
            AssistAction.LOCK_SCREEN -> actionExecutor.lockScreen()
            AssistAction.HOME -> actionExecutor.performHome()
            AssistAction.BACK -> actionExecutor.performBack()
            AssistAction.RECENT_APPS -> false // Reserved for future implementation
            AssistAction.TAKE_SCREENSHOT -> actionExecutor.takeScreenshot()
            AssistAction.POWER_DIALOG -> actionExecutor.openPowerDialog()
            AssistAction.VOLUME_CONTROL -> actionExecutor.openVolumePanel()
            AssistAction.PAYMENT_SAFETY_MODE -> actionExecutor.disableAccessibilityService()
            else -> true
        }
    }
}

