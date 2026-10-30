package com.assistx.runtime

import com.assistx.data.OverlayPosition
import com.assistx.data.UserPreferencesContract
import com.assistx.overlay.FakeOverlayPermissionAdapter
import com.assistx.overlay.OverlayManagerContract
import com.assistx.service.ServiceLauncher
import com.assistx.state.DesiredState
import com.assistx.state.OverlayPermissionState
import com.assistx.state.RuntimeState
import com.assistx.state.StateCoordinator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeOverlayManager(
    var shouldSucceed: Boolean = true
) : OverlayManagerContract {
    override var isAttached: Boolean = false

    override fun attachOverlay(): Boolean {
        if (shouldSucceed) {
            isAttached = true
            return true
        }
        isAttached = false
        return false
    }

    override fun detachOverlay(): Boolean {
        isAttached = false
        return true
    }
}

class FakeServiceLauncher : ServiceLauncher {
    var isStarted: Boolean = false

    override fun startForegroundService(): Boolean {
        isStarted = true
        return true
    }

    override fun stopForegroundService(): Boolean {
        isStarted = false
        return true
    }
}

class FakeUserPreferences : UserPreferencesContract {
    val enabledFlow = MutableStateFlow(false)
    val isPausedFlow = MutableStateFlow(false)
    val posFlow = MutableStateFlow(OverlayPosition(-1, -1))
    val internalSettingsFlow = MutableStateFlow(com.assistx.data.OverlaySettings())

    override val desiredEnabledFlow: Flow<Boolean> = enabledFlow
    override val isPausedForPaymentsFlow: Flow<Boolean> = isPausedFlow
    override val positionFlow: Flow<OverlayPosition> = posFlow
    override val settingsFlow: Flow<com.assistx.data.OverlaySettings> = internalSettingsFlow

    override suspend fun setDesiredEnabled(enabled: Boolean) {
        enabledFlow.value = enabled
        if (enabled) {
            isPausedFlow.value = false
        }
    }

    override suspend fun setPausedForPayments(paused: Boolean) {
        isPausedFlow.value = paused
        if (paused) {
            enabledFlow.value = false
        }
    }

    override suspend fun savePosition(x: Int, y: Int) {
        posFlow.value = OverlayPosition(x, y)
    }

    override suspend fun updateSettings(settings: com.assistx.data.OverlaySettings) {
        internalSettingsFlow.value = settings
    }

    override suspend fun setButtonSizeDp(sizeDp: Int) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(buttonSizeDp = sizeDp)
    }

    override suspend fun setIdleOpacityPercent(opacityPercent: Int) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(idleOpacityPercent = opacityPercent)
    }

    override suspend fun setIdleFadeSeconds(seconds: Int) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(idleFadeSeconds = seconds)
    }

    override suspend fun setPopoutDismissSeconds(seconds: Int) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(popoutDismissSeconds = seconds)
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(hapticFeedbackEnabled = enabled)
    }

    override suspend fun setSnapToEdgeEnabled(enabled: Boolean) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(snapToEdgeEnabled = enabled)
    }

    override suspend fun setAccentHex(hex: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(accentHex = hex)
    }

    override suspend fun setThemeMode(mode: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(themeMode = mode)
    }

    override suspend fun setButtonShape(shape: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(buttonShape = shape)
    }

    override suspend fun setButtonStyle(style: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(buttonStyle = style)
    }

    override suspend fun setButtonColorMode(colorMode: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(buttonColorMode = colorMode)
    }

    override suspend fun setPopoutButtonSizeDp(sizeDp: Int) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(popoutButtonSizeDp = sizeDp)
    }

    override suspend fun setPopoutOrientation(orientation: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(popoutOrientation = orientation)
    }

    override suspend fun setPopoutItemSpacingDp(spacingDp: Int) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(popoutItemSpacingDp = spacingDp)
    }

    override suspend fun setPopoutMarginDp(marginDp: Int) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(popoutMarginDp = marginDp)
    }

    override suspend fun setPopoutOrder(order: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(popoutOrder = order)
    }

    override suspend fun setCapsuleEnabled(enabled: Boolean) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(capsuleEnabled = enabled)
    }

    override suspend fun setCapsuleStyle(style: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(capsuleStyle = style)
    }

    override suspend fun setCapsuleCornerRadius(radius: String) {
        internalSettingsFlow.value = internalSettingsFlow.value.copy(capsuleCornerRadius = radius)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RuntimeControllerTest {

    private lateinit var stateCoordinator: StateCoordinator
    private lateinit var overlayManager: FakeOverlayManager
    private lateinit var permissionAdapter: FakeOverlayPermissionAdapter
    private lateinit var serviceLauncher: FakeServiceLauncher
    private lateinit var preferences: FakeUserPreferences
    private lateinit var recoveryPolicy: RecoveryPolicy
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope
    private lateinit var runtimeController: RuntimeController

    @Before
    fun setup() {
        stateCoordinator = StateCoordinator()
        overlayManager = FakeOverlayManager(shouldSucceed = true)
        permissionAdapter = FakeOverlayPermissionAdapter(OverlayPermissionState.GRANTED)
        serviceLauncher = FakeServiceLauncher()
        preferences = FakeUserPreferences()
        recoveryPolicy = RecoveryPolicy()
        testDispatcher = StandardTestDispatcher()
        testScope = TestScope(testDispatcher)

        runtimeController = RuntimeController(
            context = null,
            stateCoordinator = stateCoordinator,
            overlayManager = overlayManager,
            recoveryPolicy = recoveryPolicy,
            permissionAdapter = permissionAdapter,
            preferences = preferences,
            serviceLauncher = serviceLauncher,
            scope = testScope,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun testStartAssistantSuccess() = testScope.runTest {
        permissionAdapter.state = OverlayPermissionState.GRANTED
        overlayManager.shouldSucceed = true

        val started = runtimeController.startAssistant()
        advanceUntilIdle()

        assertTrue(started)
        assertEquals(DesiredState.ENABLED, stateCoordinator.currentSnapshot.desiredState)
        assertEquals(RuntimeState.ACTIVE, stateCoordinator.currentSnapshot.runtimeState)
        assertTrue(overlayManager.isAttached)
        assertTrue(serviceLauncher.isStarted)
        assertTrue(preferences.enabledFlow.value)
    }

    @Test
    fun testStartAssistantBlockedByPermission() = testScope.runTest {
        permissionAdapter.state = OverlayPermissionState.REQUIRED

        val started = runtimeController.startAssistant()
        advanceUntilIdle()

        assertFalse(started)
        assertEquals(DesiredState.ENABLED, stateCoordinator.currentSnapshot.desiredState)
        assertEquals(OverlayPermissionState.REQUIRED, stateCoordinator.currentSnapshot.overlayPermissionState)
        assertEquals(RuntimeState.STOPPED, stateCoordinator.currentSnapshot.runtimeState)
        assertFalse(overlayManager.isAttached)
        assertFalse(serviceLauncher.isStarted)
    }

    @Test
    fun testStopAssistantDetachesAndStopsService() = testScope.runTest {
        runtimeController.startAssistant()
        advanceUntilIdle()
        assertTrue(overlayManager.isAttached)

        runtimeController.stopAssistant()
        advanceUntilIdle()

        assertEquals(DesiredState.DISABLED, stateCoordinator.currentSnapshot.desiredState)
        assertEquals(RuntimeState.STOPPED, stateCoordinator.currentSnapshot.runtimeState)
        assertFalse(overlayManager.isAttached)
        assertFalse(serviceLauncher.isStarted)
        assertFalse(preferences.enabledFlow.value)
    }

    @Test
    fun testHandleUnexpectedInterruptionRespectsUserDisabled() {
        stateCoordinator.updateDesiredState(DesiredState.DISABLED)

        runtimeController.handleUnexpectedInterruption("System service killed")

        assertEquals(RuntimeState.STOPPED, stateCoordinator.currentSnapshot.runtimeState)
        assertEquals(0, recoveryPolicy.currentRetryCount)
    }

    @Test
    fun testHandleUnexpectedInterruptionTransitionsToRecoveringAndRecovers() = testScope.runTest {
        runtimeController.startAssistant()
        advanceUntilIdle()

        runtimeController.handleUnexpectedInterruption("Overlay detached unexpectedly")

        assertEquals(RuntimeState.RECOVERING, stateCoordinator.currentSnapshot.runtimeState)
        assertEquals(1, recoveryPolicy.currentRetryCount)

        // Advance coroutines past the backoff delay (1000ms)
        advanceUntilIdle()

        assertEquals(RuntimeState.ACTIVE, stateCoordinator.currentSnapshot.runtimeState)
    }

    @Test
    fun testExhaustedRecoveryTransitionsToFailed() {
        // Force recovery policy to exhaustion
        recoveryPolicy.recordAttempt()
        recoveryPolicy.recordAttempt()
        recoveryPolicy.recordAttempt()
        assertTrue(recoveryPolicy.isExhausted)

        stateCoordinator.updateDesiredState(DesiredState.ENABLED)
        runtimeController.handleUnexpectedInterruption("Repeated crash")

        assertEquals(RuntimeState.FAILED, stateCoordinator.currentSnapshot.runtimeState)
    }

    @Test
    fun testPauseForPayments() = testScope.runTest {
        permissionAdapter.state = OverlayPermissionState.GRANTED
        overlayManager.shouldSucceed = true

        runtimeController.startAssistant()
        advanceUntilIdle()
        assertTrue(overlayManager.isAttached)
        assertTrue(serviceLauncher.isStarted)
        assertEquals(DesiredState.ENABLED, stateCoordinator.currentSnapshot.desiredState)

        runtimeController.pauseForPayments()
        advanceUntilIdle()

        assertEquals(DesiredState.PAUSED_FOR_PAYMENTS, stateCoordinator.currentSnapshot.desiredState)
        assertEquals(RuntimeState.STOPPED, stateCoordinator.currentSnapshot.runtimeState)
        assertFalse(overlayManager.isAttached)
        assertFalse(serviceLauncher.isStarted)
        assertTrue(preferences.isPausedFlow.value)
    }
}
