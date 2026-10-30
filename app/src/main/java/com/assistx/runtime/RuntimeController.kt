package com.assistx.runtime

import android.content.Context
import com.assistx.accessibility.ActionExecutorContract
import com.assistx.accessibility.AndroidActionExecutor
import com.assistx.data.OverlayPreferences
import com.assistx.data.UserPreferencesContract
import com.assistx.overlay.AndroidOverlayPermissionAdapter
import com.assistx.overlay.OverlayManager
import com.assistx.overlay.OverlayManagerContract
import com.assistx.overlay.OverlayPermissionAdapter
import com.assistx.service.AndroidServiceLauncher
import com.assistx.service.ServiceLauncher
import com.assistx.state.DesiredState
import com.assistx.state.OverlayPermissionState
import com.assistx.state.RuntimeState
import com.assistx.state.StateCoordinator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Coordinates application lifecycle, service start/stop, overlay attachment,
 * and bounded automatic recovery for AssistX.
 */
class RuntimeController(
    private val context: Context? = null,
    val stateCoordinator: StateCoordinator,
    val overlayManager: OverlayManagerContract,
    val recoveryPolicy: RecoveryPolicy = RecoveryPolicy(),
    private val permissionAdapter: OverlayPermissionAdapter = AndroidOverlayPermissionAdapter(),
    private val preferences: UserPreferencesContract? = context?.let { OverlayPreferences(it) },
    private val serviceLauncher: ServiceLauncher = AndroidServiceLauncher(context),
    private val actionExecutor: ActionExecutorContract = AndroidActionExecutor(context),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OverlayManager.FailureListener {

    private var recoveryJob: Job? = null

    init {
        if (overlayManager is OverlayManager) {
            overlayManager.failureListener = this
        }
    }

    override fun onOverlayInterrupted(reason: String) {
        handleUnexpectedInterruption(reason)
    }

    /**
     * Explicit user request to enable AssistX.
     * Transitions DesiredState to ENABLED, verifies permissions, starts foreground service,
     * and attaches the floating button.
     */
    fun startAssistant(): Boolean {
        recoveryJob?.cancel()
        recoveryPolicy.resetBudget()
        stateCoordinator.updateDesiredState(DesiredState.ENABLED)

        preferences?.let { prefs ->
            scope.launch(ioDispatcher) {
                prefs.setDesiredEnabled(true)
                prefs.setPausedForPayments(false)
            }
        }

        // 1. Check permission
        val permission = permissionAdapter.checkPermission(context)
        stateCoordinator.updateOverlayPermission(permission)
        if (permission != OverlayPermissionState.GRANTED) {
            stateCoordinator.updateRuntimeState(RuntimeState.STOPPED)
            return false
        }

        stateCoordinator.updateRuntimeState(RuntimeState.STARTING)

        // 2. Start Foreground Service for process survival
        serviceLauncher.startForegroundService()

        // 3. Attach Overlay
        val attached = overlayManager.attachOverlay()
        if (attached) {
            stateCoordinator.updateRuntimeState(RuntimeState.ACTIVE)
            recoveryPolicy.onHealthyObserved()
            return true
        } else {
            stateCoordinator.updateRuntimeState(RuntimeState.FAILED)
            return false
        }
    }

    /**
     * Explicit user request to disable AssistX.
     * Cancels any pending recovery, removes the overlay, and stops the foreground service.
     */
    fun stopAssistant() {
        recoveryJob?.cancel()
        recoveryPolicy.resetBudget()
        stateCoordinator.updateDesiredState(DesiredState.DISABLED)
        stateCoordinator.updateRuntimeState(RuntimeState.STOPPING)

        preferences?.let { prefs ->
            scope.launch(ioDispatcher) {
                prefs.setDesiredEnabled(false)
            }
        }

        // 1. Detach overlay
        overlayManager.detachOverlay()

        // 2. Stop Foreground Service
        serviceLauncher.stopForegroundService()

        stateCoordinator.updateRuntimeState(RuntimeState.STOPPED)
    }

    /**
     * Explicit user request to pause AssistX before opening financial/payment applications.
     * Detaches the floating overlay from WindowManager, stops the foreground service and notification,
     * and requests the active AccessibilityService to disable itself.
     */
    fun pauseForPayments() {
        recoveryJob?.cancel()
        recoveryPolicy.resetBudget()
        stateCoordinator.updateDesiredState(DesiredState.PAUSED_FOR_PAYMENTS)
        stateCoordinator.updateRuntimeState(RuntimeState.STOPPING)

        preferences?.let { prefs ->
            scope.launch(ioDispatcher) {
                prefs.setPausedForPayments(true)
                prefs.setDesiredEnabled(false)
            }
        }

        // 1. Immediately detach overlay window from WindowManager
        overlayManager.detachOverlay()

        // 2. Stop foreground service and remove ongoing notification
        serviceLauncher.stopForegroundService()

        // 3. Request AccessibilityService to disable itself
        actionExecutor.disableAccessibilityService()

        stateCoordinator.updateRuntimeState(RuntimeState.STOPPED)
    }

    /**
     * Triggered when an unexpected disruption occurs (e.g. overlay window error or service unbind).
     * Enforces the bounded recovery contract (max 3 retries with backoff).
     */
    fun handleUnexpectedInterruption(reason: String) {
        val isUserDisabled = stateCoordinator.currentSnapshot.desiredState == DesiredState.DISABLED
        if (isUserDisabled) {
            // Respect user intent: no recovery
            stateCoordinator.updateRuntimeState(RuntimeState.STOPPED)
            return
        }

        if (!recoveryPolicy.canAttemptRecovery(isUserDisabled = false)) {
            // Retry budget exhausted
            stateCoordinator.updateRuntimeState(RuntimeState.FAILED)
            return
        }

        val backoffDelayMs = try {
            recoveryPolicy.recordAttempt()
        } catch (e: IllegalStateException) {
            stateCoordinator.updateRuntimeState(RuntimeState.FAILED)
            return
        }

        stateCoordinator.updateRuntimeState(RuntimeState.RECOVERING)

        recoveryJob?.cancel()
        recoveryJob = scope.launch {
            delay(backoffDelayMs)
            executeRecoveryAttempt()
        }
    }

    private fun executeRecoveryAttempt() {
        // Double check user didn't disable during the delay
        if (stateCoordinator.currentSnapshot.desiredState == DesiredState.DISABLED) {
            stateCoordinator.updateRuntimeState(RuntimeState.STOPPED)
            return
        }

        val permission = permissionAdapter.checkPermission(context)
        stateCoordinator.updateOverlayPermission(permission)
        if (permission != OverlayPermissionState.GRANTED) {
            stateCoordinator.updateRuntimeState(RuntimeState.FAILED)
            return
        }

        val attached = overlayManager.attachOverlay()
        if (attached) {
            stateCoordinator.updateRuntimeState(RuntimeState.ACTIVE)
            recoveryPolicy.onHealthyObserved()
        } else {
            if (recoveryPolicy.isExhausted) {
                stateCoordinator.updateRuntimeState(RuntimeState.FAILED)
            } else {
                handleUnexpectedInterruption("Re-attachment failed")
            }
        }
    }

    /**
     * Reads saved user preference on startup and restores the enabled state if applicable.
     */
    fun restoreStateOnLaunch() {
        preferences?.let { prefs ->
            scope.launch(ioDispatcher) {
                val isPaused = prefs.isPausedForPaymentsFlow.first()
                if (isPaused) {
                    withContext(Dispatchers.Main) {
                        stateCoordinator.updateDesiredState(DesiredState.PAUSED_FOR_PAYMENTS)
                        stateCoordinator.updateRuntimeState(RuntimeState.STOPPED)
                    }
                    return@launch
                }
                val shouldEnable = prefs.desiredEnabledFlow.first()
                if (shouldEnable) {
                    withContext(Dispatchers.Main) {
                        startAssistant()
                    }
                }
            }
        }
    }
}
