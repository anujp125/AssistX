package com.nudge.assist.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.nudge.assist.data.diagnostics.DiagnosticRecorder
import com.nudge.assist.service.AssistAccessibilityService
import com.nudge.assist.state.AccessibilityState
import com.nudge.assist.state.StateCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Centralized Accessibility State Manager.
 *
 * Invariants:
 * 1. Identifies AssistX's own Accessibility service using its actual ComponentName.
 * 2. Distinguishes between enabled in system settings vs. actively connected/bound.
 * 3. Observes connection lifecycle events directly from AssistAccessibilityService.
 * 4. Reconciles state in real-time without relying on stale persisted booleans.
 * 5. Does not conflate general system accessibility (AccessibilityManager.isEnabled)
 *    with AssistX's own service state.
 */
class AccessibilityStateManager(
    private val context: Context,
    private val stateCoordinator: StateCoordinator,
    private val diagnosticRecorder: DiagnosticRecorder,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : AccessibilityStateManagerContract {

    private val _state = MutableStateFlow(AccessibilityState.UNKNOWN)
    override val state: StateFlow<AccessibilityState> = _state.asStateFlow()

    init {
        // Collect real-time service connection callbacks
        scope.launch {
            AssistAccessibilityService.isConnected.collect {
                checkCurrentState(context)
            }
        }
    }

    override fun getAccessibilityState(context: Context?): AccessibilityState {
        return checkCurrentState(context)
    }

    override fun checkCurrentState(context: Context?): AccessibilityState {
        val targetContext = context ?: this.context
        val enabledInSettings = isSettingsEnabled(targetContext)
        val connected = isServiceConnected()

        val newState = when {
            !enabledInSettings -> AccessibilityState.DISABLED
            connected -> AccessibilityState.ENABLED_CONNECTED
            else -> AccessibilityState.ENABLED_DISCONNECTED
        }

        if (_state.value != newState) {
            _state.value = newState
            stateCoordinator.updateAccessibilityState(newState)
            diagnosticRecorder.recordEvent(
                eventType = "ACCESSIBILITY_STATE_CHANGED",
                component = "A11Y_MANAGER",
                details = "Accessibility state updated to: $newState (enabledInSettings=$enabledInSettings, connected=$connected)"
            )
        }
        return newState
    }

    override fun isSettingsEnabled(context: Context?): Boolean {
        val ctx = context ?: this.context
        return try {
            val expectedComponentName = ComponentName(ctx, AssistAccessibilityService::class.java).flattenToString()
            val shortComponentName = ComponentName(ctx, AssistAccessibilityService::class.java).flattenToShortString()

            // 1. Inspect Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            val enabledServices = Settings.Secure.getString(
                ctx.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            val inSecureSettings = enabledServices?.split(":")?.any {
                it.equals(expectedComponentName, ignoreCase = true) ||
                        it.equals(shortComponentName, ignoreCase = true)
            } == true

            if (inSecureSettings) return true

            // 2. Cross-verify with AccessibilityManager enabled services list specifically for AssistX
            val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabledList = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            val inAmList = enabledList?.any {
                val serviceInfo = it.resolveInfo.serviceInfo
                serviceInfo.packageName == ctx.packageName &&
                        serviceInfo.name == AssistAccessibilityService::class.java.name
            } == true

            inAmList
        } catch (e: Exception) {
            false
        }
    }

    override fun isServiceConnected(): Boolean {
        return AssistAccessibilityService.isConnected.value
    }

    override fun createSettingsIntent(context: Context?): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun openAccessibilitySettings(context: Context): Boolean {
        diagnosticRecorder.recordEvent(
            eventType = "NAVIGATE_ACCESSIBILITY_SETTINGS",
            component = "A11Y_MANAGER",
            details = "Opening accessibility settings for action permission prompt"
        )
        return try {
            val intent = createSettingsIntent(context)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (e2: Exception) {
                diagnosticRecorder.recordEvent(
                    eventType = "NAVIGATE_ACCESSIBILITY_SETTINGS_FAILED",
                    component = "A11Y_MANAGER",
                    result = "FAILURE",
                    details = e2.message ?: "Failed to open settings"
                )
                false
            }
        }
    }
}

