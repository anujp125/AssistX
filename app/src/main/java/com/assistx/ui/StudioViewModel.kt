package com.assistx.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistx.accessibility.AccessibilityAdapterContract
import com.assistx.data.OverlaySettings
import com.assistx.data.UserPreferencesContract
import com.assistx.data.diagnostics.DiagnosticEventEntity
import com.assistx.data.diagnostics.DiagnosticRecorderContract
import com.assistx.overlay.OverlayPermissionAdapter
import com.assistx.runtime.RuntimeController
import com.assistx.state.AppStateSnapshot
import com.assistx.state.DesiredState
import com.assistx.state.StateCoordinator
import com.assistx.state.UserFacingStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), userPreferences.initialSettings)

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

    fun onPauseForPayments() {
        diagnosticRecorder.recordEvent(
            eventType = "PAUSE_FOR_PAYMENTS_TRIGGERED",
            component = "STUDIO",
            details = "User triggered explicit payment pause"
        )
        runtimeController.pauseForPayments()
    }

    fun onResumeFromPayments() {
        diagnosticRecorder.recordEvent(
            eventType = "RESUME_FROM_PAYMENTS_TRIGGERED",
            component = "STUDIO",
            details = "User requested resume from payment pause"
        )
        runtimeController.startAssistant()
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

    private var opacityDebounceJob: Job? = null

    fun updateIdleOpacity(opacityPercent: Int) {
        opacityDebounceJob?.cancel()
        opacityDebounceJob = viewModelScope.launch {
            delay(150L)
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

    fun updateAccent(hex: String) {
        viewModelScope.launch {
            userPreferences.setAccentHex(hex)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    // Floating Button Customization
    fun updateButtonShape(shape: String) {
        viewModelScope.launch {
            userPreferences.setButtonShape(shape)
        }
    }

    fun updateButtonStyle(style: String) {
        viewModelScope.launch {
            userPreferences.setButtonStyle(style)
        }
    }

    fun updateButtonColorMode(colorMode: String) {
        viewModelScope.launch {
            userPreferences.setButtonColorMode(colorMode)
        }
    }

    // Popout Array Settings
    fun updatePopoutButtonSize(sizeDp: Int) {
        viewModelScope.launch {
            userPreferences.setPopoutButtonSizeDp(sizeDp)
        }
    }

    fun updatePopoutOrientation(orientation: String) {
        viewModelScope.launch {
            userPreferences.setPopoutOrientation(orientation)
        }
    }

    fun updatePopoutItemSpacing(spacingDp: Int) {
        viewModelScope.launch {
            userPreferences.setPopoutItemSpacingDp(spacingDp)
        }
    }

    fun updatePopoutMargin(marginDp: Int) {
        viewModelScope.launch {
            userPreferences.setPopoutMarginDp(marginDp)
        }
    }

    fun updatePopoutOrder(order: String) {
        viewModelScope.launch {
            userPreferences.setPopoutOrder(order)
        }
    }

    // Background Capsule Element
    fun updateCapsuleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setCapsuleEnabled(enabled)
        }
    }

    fun updateCapsuleStyle(style: String) {
        viewModelScope.launch {
            userPreferences.setCapsuleStyle(style)
        }
    }

    fun updateCapsuleCornerRadius(radius: String) {
        viewModelScope.launch {
            userPreferences.setCapsuleCornerRadius(radius)
        }
    }

    fun updateAppIconVariant(context: Context, variant: String) {
        val v = com.assistx.util.AppIconVariant.fromId(variant)
        com.assistx.util.AppIconManager.applyIconVariant(context.applicationContext, v)
        viewModelScope.launch {
            userPreferences.setAppIconVariant(v.id)
            diagnosticRecorder.recordEvent(
                eventType = "APP_ICON_VARIANT_CHANGED",
                component = "STUDIO",
                details = "Applied variant: ${v.label} (${v.id})"
            )
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            userPreferences.updateSettings(OverlaySettings())
        }
    }
}
