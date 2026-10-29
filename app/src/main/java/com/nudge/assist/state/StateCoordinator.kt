package com.nudge.assist.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Thread-safe coordinator maintaining the observable multidimensional state of AssistX.
 * Guarantees atomic state updates and enforces lifecycle invariants.
 */
class StateCoordinator(
    initialSnapshot: AppStateSnapshot = AppStateSnapshot()
) {

    private val _state = MutableStateFlow(initialSnapshot)
    val state: StateFlow<AppStateSnapshot> = _state.asStateFlow()

    private val _userFacingStatus = MutableStateFlow(StatusEvaluator.deriveUserFacingStatus(initialSnapshot))
    val userFacingStatus: StateFlow<UserFacingStatus> = _userFacingStatus.asStateFlow()

    private val _actionReadiness = MutableStateFlow(StatusEvaluator.deriveActionReadiness(initialSnapshot))
    val actionReadiness: StateFlow<ActionReadiness> = _actionReadiness.asStateFlow()

    val currentSnapshot: AppStateSnapshot
        get() = _state.value

    val currentStatus: UserFacingStatus
        get() = _userFacingStatus.value

    val currentActionReadiness: ActionReadiness
        get() = _actionReadiness.value

    private fun updateState(mutation: (AppStateSnapshot) -> AppStateSnapshot) {
        _state.update(mutation)
        val updated = _state.value
        _userFacingStatus.value = StatusEvaluator.deriveUserFacingStatus(updated)
        _actionReadiness.value = StatusEvaluator.deriveActionReadiness(updated)
    }

    /**
     * Updates the user's desired state.
     * Invariant: Disabling the app cancels any active recovery attempt.
     */
    fun updateDesiredState(desired: DesiredState) {
        updateState { current ->
            val updatedRuntime = if (desired == DesiredState.DISABLED &&
                (current.runtimeState == RuntimeState.RECOVERING || current.runtimeState == RuntimeState.STARTING)) {
                RuntimeState.STOPPED
            } else {
                current.runtimeState
            }
            current.copy(
                desiredState = desired,
                runtimeState = updatedRuntime
            )
        }
    }

    fun updateOverlayPermission(permission: OverlayPermissionState) {
        updateState { it.copy(overlayPermissionState = permission) }
    }

    fun updateOverlayState(overlay: OverlayState) {
        updateState { it.copy(overlayState = overlay) }
    }

    fun updateRuntimeState(runtime: RuntimeState) {
        updateState { current ->
            // If the user has disabled AssistX, suppress transitioning into RECOVERING
            if (current.desiredState == DesiredState.DISABLED && runtime == RuntimeState.RECOVERING) {
                current.copy(runtimeState = RuntimeState.STOPPED)
            } else {
                current.copy(runtimeState = runtime)
            }
        }
    }

    fun updateAccessibilityState(accessibility: AccessibilityState) {
        updateState { it.copy(accessibilityState = accessibility) }
    }

    /**
     * Resets observed runtime states while preserving user's desired state.
     */
    fun resetRuntimeObservations() {
        updateState { current ->
            current.copy(
                overlayState = OverlayState.DETACHED,
                runtimeState = RuntimeState.STOPPED,
                accessibilityState = AccessibilityState.UNKNOWN
            )
        }
    }
}
