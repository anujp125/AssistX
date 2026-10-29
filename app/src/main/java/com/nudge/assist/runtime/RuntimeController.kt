package com.nudge.assist.runtime

import android.content.Context
import com.nudge.assist.data.OverlayPreferences
import com.nudge.assist.data.UserPreferencesContract
import com.nudge.assist.overlay.AndroidOverlayPermissionAdapter
import com.nudge.assist.overlay.OverlayManagerContract
import com.nudge.assist.overlay.OverlayPermissionAdapter
import com.nudge.assist.service.AndroidServiceLauncher
import com.nudge.assist.service.ServiceLauncher
import com.nudge.assist.state.DesiredState
import com.nudge.assist.state.OverlayPermissionState
import com.nudge.assist.state.RuntimeState
import com.nudge.assist.state.StateCoordinator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private var recoveryJob: Job? = null

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
                val shouldEnable = prefs.desiredEnabledFlow.first()
                if (shouldEnable) {
                    scope.launch {
                        startAssistant()
                    }
                }
            }
        }
    }
}
