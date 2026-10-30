package com.nudge.assist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nudge.assist.accessibility.AndroidAccessibilityAdapter
import com.nudge.assist.accessibility.AndroidActionExecutor
import com.nudge.assist.data.OverlayPreferences
import com.nudge.assist.data.diagnostics.DiagnosticDatabase
import com.nudge.assist.data.diagnostics.DiagnosticRecorder
import com.nudge.assist.overlay.AndroidOverlayPermissionAdapter
import com.nudge.assist.overlay.OverlayManager
import com.nudge.assist.runtime.RecoveryPolicy
import com.nudge.assist.runtime.RuntimeController
import com.nudge.assist.service.AndroidServiceLauncher
import com.nudge.assist.state.StateCoordinator

class AssistApplication : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "assistx_foreground_service"
        lateinit var instance: AssistApplication
            private set
    }

    lateinit var stateCoordinator: StateCoordinator
        private set

    lateinit var overlayPermissionAdapter: AndroidOverlayPermissionAdapter
        private set

    lateinit var accessibilityAdapter: AndroidAccessibilityAdapter
        private set

    lateinit var accessibilityStateManager: com.nudge.assist.accessibility.AccessibilityStateManager
        private set

    lateinit var actionDispatcher: com.nudge.assist.accessibility.ActionDispatcher
        private set

    lateinit var diagnosticRecorder: DiagnosticRecorder
        private set

    lateinit var overlayPreferences: OverlayPreferences
        private set

    lateinit var overlayManager: OverlayManager
        private set

    lateinit var runtimeController: RuntimeController
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()

        // 1. Core State & Adapters
        stateCoordinator = StateCoordinator()
        overlayPermissionAdapter = AndroidOverlayPermissionAdapter()
        accessibilityAdapter = AndroidAccessibilityAdapter()
        overlayPreferences = OverlayPreferences(this)

        // 2. Diagnostics
        val database = DiagnosticDatabase.getInstance(this)
        diagnosticRecorder = DiagnosticRecorder(database.diagnosticDao())

        // 3. Centralized Accessibility State Manager & Action Dispatcher
        accessibilityStateManager = com.nudge.assist.accessibility.AccessibilityStateManager(
            context = this,
            stateCoordinator = stateCoordinator,
            diagnosticRecorder = diagnosticRecorder
        )
        actionDispatcher = com.nudge.assist.accessibility.ActionDispatcher(
            actionExecutor = AndroidActionExecutor(this),
            accessibilityStateManager = accessibilityStateManager,
            diagnosticRecorder = diagnosticRecorder
        )

        // 4. Overlay Manager
        overlayManager = OverlayManager(
            context = this,
            stateCoordinator = stateCoordinator,
            permissionAdapter = overlayPermissionAdapter,
            preferences = overlayPreferences,
            actionExecutor = AndroidActionExecutor(this),
            accessibilityStateManager = accessibilityStateManager,
            actionDispatcher = actionDispatcher
        )

        // 5. Runtime Controller
        runtimeController = RuntimeController(
            context = this,
            stateCoordinator = stateCoordinator,
            overlayManager = overlayManager,
            recoveryPolicy = RecoveryPolicy(),
            permissionAdapter = overlayPermissionAdapter,
            preferences = overlayPreferences,
            serviceLauncher = AndroidServiceLauncher(this),
            actionExecutor = AndroidActionExecutor(this)
        )
        overlayManager.failureListener = runtimeController

        // 6. Restore saved user state
        runtimeController.restoreStateOnLaunch()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
