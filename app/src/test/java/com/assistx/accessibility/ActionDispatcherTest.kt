package com.assistx.accessibility

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.assistx.data.FakeDiagnosticDao
import com.assistx.data.diagnostics.DiagnosticRecorder
import com.assistx.state.AccessibilityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAccessibilityStateManager(
    var currentState: AccessibilityState = AccessibilityState.DISABLED
) : AccessibilityStateManagerContract {

    private val _state = MutableStateFlow(currentState)
    override val state: StateFlow<AccessibilityState> = _state.asStateFlow()

    var openSettingsCallCount = 0

    override fun checkCurrentState(context: Context?): AccessibilityState {
        _state.value = currentState
        return currentState
    }

    override fun getAccessibilityState(context: Context?): AccessibilityState = checkCurrentState(context)

    override fun isSettingsEnabled(context: Context?): Boolean {
        return currentState == AccessibilityState.ENABLED_CONNECTED || currentState == AccessibilityState.ENABLED_DISCONNECTED
    }

    override fun isServiceConnected(): Boolean {
        return currentState == AccessibilityState.ENABLED_CONNECTED
    }

    override fun createSettingsIntent(context: Context?): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    override fun openAccessibilitySettings(context: Context): Boolean {
        openSettingsCallCount++
        return true
    }
}

class ActionDispatcherTest {

    private lateinit var fakeActionExecutor: FakeActionExecutor
    private lateinit var fakeA11yManager: FakeAccessibilityStateManager
    private lateinit var diagnosticRecorder: DiagnosticRecorder
    private lateinit var dispatcher: ActionDispatcher
    private lateinit var dummyContext: Context

    @Before
    fun setUp() {
        fakeActionExecutor = FakeActionExecutor()
        fakeA11yManager = FakeAccessibilityStateManager()
        diagnosticRecorder = DiagnosticRecorder(FakeDiagnosticDao())
        dispatcher = ActionDispatcher(
            actionExecutor = fakeActionExecutor,
            accessibilityStateManager = fakeA11yManager,
            diagnosticRecorder = diagnosticRecorder
        )
        dummyContext = Class.forName("android.app.Activity").getDeclaredConstructor().newInstance() as Context
    }

    @Test
    fun testActionClassification_accessibilityRequirements() {
        // Actions that MUST require accessibility
        assertTrue(AssistAction.LOCK_SCREEN.requiresAccessibility)
        assertTrue(AssistAction.HOME.requiresAccessibility)
        assertTrue(AssistAction.BACK.requiresAccessibility)
        assertTrue(AssistAction.RECENT_APPS.requiresAccessibility)
        assertTrue(AssistAction.TAKE_SCREENSHOT.requiresAccessibility)
        assertTrue(AssistAction.POWER_DIALOG.requiresAccessibility)

        // Actions that MUST NOT require accessibility
        assertFalse(AssistAction.VOLUME_CONTROL.requiresAccessibility)
        assertFalse(AssistAction.TOGGLE_POPOUT.requiresAccessibility)
        assertFalse(AssistAction.DRAG_REPOSITION.requiresAccessibility)
        assertFalse(AssistAction.EDGE_SNAP.requiresAccessibility)
        assertFalse(AssistAction.IDLE_FADE.requiresAccessibility)
        assertFalse(AssistAction.PAYMENT_SAFETY_MODE.requiresAccessibility)
    }

    @Test
    fun testNonAccessibilityAction_executesWithoutCheckingAccessibility() {
        // Accessibility is DISABLED
        fakeA11yManager.currentState = AccessibilityState.DISABLED

        var permissionRequiredCalled = false
        val result = dispatcher.dispatch(AssistAction.VOLUME_CONTROL, dummyContext) {
            permissionRequiredCalled = true
        }

        // Must succeed without prompting for permissions
        assertEquals(ExecutionResult.SUCCESS, result)
        assertEquals(1, fakeActionExecutor.openVolumePanelCallCount)
        assertFalse(permissionRequiredCalled)
    }

    @Test
    fun testAccessibilityAction_whenConnected_executesActionWithoutPrompt() {
        fakeA11yManager.currentState = AccessibilityState.ENABLED_CONNECTED

        var permissionRequiredCalled = false
        val result = dispatcher.dispatch(AssistAction.LOCK_SCREEN, dummyContext) {
            permissionRequiredCalled = true
        }

        assertEquals(ExecutionResult.SUCCESS, result)
        assertEquals(1, fakeActionExecutor.lockScreenCallCount)
        assertFalse(permissionRequiredCalled)
    }

    @Test
    fun testAccessibilityAction_whenDisabled_blocksExecutionAndInvokesCallback() {
        fakeA11yManager.currentState = AccessibilityState.DISABLED

        var blockedAction: AssistAction? = null
        val result = dispatcher.dispatch(AssistAction.LOCK_SCREEN, dummyContext) { action ->
            blockedAction = action
        }

        assertEquals(ExecutionResult.PERMISSION_REQUIRED, result)
        assertEquals(0, fakeActionExecutor.lockScreenCallCount)
        assertEquals(AssistAction.LOCK_SCREEN, blockedAction)
    }

    @Test
    fun testAccessibilityAction_whenDisconnected_blocksExecutionAndInvokesCallback() {
        fakeA11yManager.currentState = AccessibilityState.ENABLED_DISCONNECTED

        var blockedAction: AssistAction? = null
        val result = dispatcher.dispatch(AssistAction.HOME, dummyContext) { action ->
            blockedAction = action
        }

        assertEquals(ExecutionResult.PERMISSION_REQUIRED, result)
        assertEquals(0, fakeActionExecutor.performHomeCallCount)
        assertEquals(AssistAction.HOME, blockedAction)
    }

    @Test
    fun testFailedAction_isNeverAutomaticallyReplayedWhenStateBecomesConnected() {
        // Step 1: User attempts Lock Screen while accessibility is disabled
        fakeA11yManager.currentState = AccessibilityState.DISABLED
        var permissionRequiredCalled = false
        dispatcher.dispatch(AssistAction.LOCK_SCREEN, dummyContext) {
            permissionRequiredCalled = true
        }
        assertTrue(permissionRequiredCalled)
        assertEquals(0, fakeActionExecutor.lockScreenCallCount)

        // Step 2: User enables accessibility in Settings -> State becomes ENABLED_CONNECTED
        fakeA11yManager.currentState = AccessibilityState.ENABLED_CONNECTED

        // Step 3: Verify that the dispatcher has NOT replayed the lock screen action
        assertEquals(0, fakeActionExecutor.lockScreenCallCount)

        // Step 4: Only when user explicitly triggers the gesture again does it execute
        val nextResult = dispatcher.dispatch(AssistAction.LOCK_SCREEN, dummyContext)
        assertEquals(ExecutionResult.SUCCESS, nextResult)
        assertEquals(1, fakeActionExecutor.lockScreenCallCount)
    }
}

