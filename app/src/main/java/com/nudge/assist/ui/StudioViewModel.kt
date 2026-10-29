package com.nudge.assist.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nudge.assist.accessibility.AccessibilityAdapterContract
import com.nudge.assist.data.OverlaySettings
import com.nudge.assist.data.UserPreferencesContract
import com.nudge.assist.data.diagnostics.DiagnosticEventEntity
import com.nudge.assist.data.diagnostics.DiagnosticRecorderContract
import com.nudge.assist.overlay.OverlayPermissionAdapter
import com.nudge.assist.runtime.RuntimeController
import com.nudge.assist.state.AppStateSnapshot
import com.nudge.assist.state.DesiredState
import com.nudge.assist.state.StateCoordinator
import com.nudge.assist.state.UserFacingStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudioViewModel(
    val stateCoordinator: StateCoordinator,
    val runtimeController: RuntimeController,
    val permissionAdapter: OverlayPermissionAdapter,
    val accessibilityAdapter: AccessibilityAdapterContract,
    val diagnosticRecorder: DiagnosticRecorderContract,
    val userPreferences: UserPreferencesContract
) : ViewModel() {

    val snapshot: StateFlow<AppStateSnapshot> = stateCoordinator.state
    val userFacingStatus: StateFlow<UserFacingStatus> = stateCoordinator.userFacingStatus

    val settings: StateFlow<OverlaySettings> = userPreferences.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverlaySettings())

    val recentDiagnostics: StateFlow<List<DiagnosticEventEntity>> = diagnosticRecorder
        .getRecentEvents(100)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onScreenResumed(context: Context) {
        val perm = permissionAdapter.checkPermission(context)
        stateCoordinator.updateOverlayPermission(perm)

        val accState = accessibilityAdapter.getAccessibilityState(context)
        stateCoordinator.updateAccessibilityState(accState)

        diagnosticRecorder.recordEvent(
            eventType = "STUDIO_RESUMED",
            component = "STUDIO",
            details = "Permission: $perm, Accessibility: $accState"
        )
    }

    fun onToggleMaster(enable: Boolean) {
        if (enable) {
            diagnosticRecorder.recordEvent(
                eventType = "USER_ENABLE_CLICKED",
                component = "STUDIO"
            )
            runtimeController.startAssistant()
        } else {
            diagnosticRecorder.recordEvent(
                eventType = "USER_DISABLE_CLICKED",
                component = "STUDIO"
            )
            runtimeController.stopAssistant()
        }
    }

    fun openOverlaySettings(context: Context) {
        diagnosticRecorder.recordEvent(
            eventType = "NAVIGATE_OVERLAY_SETTINGS",
            component = "STUDIO"
        )
        try {
            val intent = permissionAdapter.createSettingsIntent(context)
            context.startActivity(intent)
        } catch (e: Exception) {
            diagnosticRecorder.recordEvent(
                eventType = "NAVIGATE_OVERLAY_SETTINGS_FAILED",
                component = "STUDIO",
                result = "FAILURE",
                details = e.message ?: "Unknown error"
            )
        }
    }

    fun openAccessibilitySettings(context: Context) {
        diagnosticRecorder.recordEvent(
            eventType = "NAVIGATE_ACCESSIBILITY_SETTINGS",
            component = "STUDIO"
        )
        try {
            val intent = accessibilityAdapter.createSettingsIntent(context)
            context.startActivity(intent)
        } catch (e: Exception) {
            diagnosticRecorder.recordEvent(
                eventType = "NAVIGATE_ACCESSIBILITY_SETTINGS_FAILED",
                component = "STUDIO",
                result = "FAILURE",
                details = e.message ?: "Unknown error"
            )
        }
    }

    fun onClearDiagnostics() {
        viewModelScope.launch {
            diagnosticRecorder.clearAll()
        }
    }

    fun updateButtonSize(sizeDp: Int) {
        viewModelScope.launch {
            userPreferences.setButtonSizeDp(sizeDp)
        }
    }

    fun updateIdleOpacity(opacityPercent: Int) {
        viewModelScope.launch {
            userPreferences.setIdleOpacityPercent(opacityPercent)
        }
    }

    fun updateIdleFadeDelay(seconds: Int) {
        viewModelScope.launch {
            userPreferences.setIdleFadeSeconds(seconds)
        }
    }

    fun updatePopoutDismissDelay(seconds: Int) {
        viewModelScope.launch {
            userPreferences.setPopoutDismissSeconds(seconds)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setHapticFeedbackEnabled(enabled)
        }
    }

    fun updateSnapToEdge(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setSnapToEdgeEnabled(enabled)
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            userPreferences.updateSettings(OverlaySettings())
        }
    }
}
