package com.nudge.assist.ui

import com.nudge.assist.accessibility.AccessibilityAdapterContract
import com.nudge.assist.accessibility.FakeAccessibilityAdapter
import com.nudge.assist.data.diagnostics.DiagnosticEventEntity
import com.nudge.assist.data.diagnostics.DiagnosticRecorderContract
import com.nudge.assist.overlay.FakeOverlayPermissionAdapter
import com.nudge.assist.overlay.OverlayPermissionAdapter
import com.nudge.assist.runtime.FakeOverlayManager
import com.nudge.assist.runtime.FakeServiceLauncher
import com.nudge.assist.runtime.FakeUserPreferences
import com.nudge.assist.runtime.RecoveryPolicy
import com.nudge.assist.runtime.RuntimeController
import com.nudge.assist.state.AccessibilityState
import com.nudge.assist.state.DesiredState
import com.nudge.assist.state.OverlayPermissionState
import com.nudge.assist.state.RuntimeState
import com.nudge.assist.state.StateCoordinator
import com.nudge.assist.state.UserFacingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeDiagnosticRecorder : DiagnosticRecorderContract {
    val recordedEvents = mutableListOf<DiagnosticEventEntity>()
    private val flow = MutableStateFlow<List<DiagnosticEventEntity>>(emptyList())

    override fun recordEvent(
        eventType: String,
        component: String,
        result: String,
        severity: String,
        details: String
    ) {
        val entity = DiagnosticEventEntity(
            eventType = eventType,
            component = component,
            result = result,
            severity = severity,
            details = details
        )
        recordedEvents.add(entity)
        flow.value = recordedEvents.toList()
    }

    override fun getRecentEvents(limit: Int): Flow<List<DiagnosticEventEntity>> = flow

    override suspend fun clearAll() {
        recordedEvents.clear()
        flow.value = emptyList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModelTest {

    private lateinit var stateCoordinator: StateCoordinator
    private lateinit var overlayManager: FakeOverlayManager
    private lateinit var permissionAdapter: FakeOverlayPermissionAdapter
    private lateinit var accessibilityAdapter: FakeAccessibilityAdapter
    private lateinit var diagnosticRecorder: FakeDiagnosticRecorder
    private lateinit var userPreferences: FakeUserPreferences
    private lateinit var runtimeController: RuntimeController
    private lateinit var viewModel: StudioViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        stateCoordinator = StateCoordinator()
        overlayManager = FakeOverlayManager(shouldSucceed = true)
        permissionAdapter = FakeOverlayPermissionAdapter(OverlayPermissionState.GRANTED)
        accessibilityAdapter = FakeAccessibilityAdapter(settingsEnabled = true, serviceConnected = true)
        diagnosticRecorder = FakeDiagnosticRecorder()
        userPreferences = FakeUserPreferences()

        runtimeController = RuntimeController(
            context = null,
            stateCoordinator = stateCoordinator,
            overlayManager = overlayManager,
            recoveryPolicy = RecoveryPolicy(),
            permissionAdapter = permissionAdapter,
            preferences = userPreferences,
            serviceLauncher = FakeServiceLauncher(),
            scope = testScope,
            ioDispatcher = testDispatcher
        )

        viewModel = StudioViewModel(
            stateCoordinator = stateCoordinator,
            runtimeController = runtimeController,
            permissionAdapter = permissionAdapter,
            accessibilityAdapter = accessibilityAdapter,
            diagnosticRecorder = diagnosticRecorder,
            userPreferences = userPreferences
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testToggleMasterEnableStartsAssistant() = testScope.runTest {
        viewModel.onToggleMaster(true)
        advanceUntilIdle()

        assertEquals(DesiredState.ENABLED, stateCoordinator.currentSnapshot.desiredState)
        assertEquals(RuntimeState.ACTIVE, stateCoordinator.currentSnapshot.runtimeState)
        assertTrue(overlayManager.isAttached)
        assertTrue(diagnosticRecorder.recordedEvents.any { it.eventType == "USER_ENABLE_CLICKED" })
    }

    @Test
    fun testToggleMasterDisableStopsAssistant() = testScope.runTest {
        viewModel.onToggleMaster(true)
        advanceUntilIdle()

        viewModel.onToggleMaster(false)
        advanceUntilIdle()

        assertEquals(DesiredState.DISABLED, stateCoordinator.currentSnapshot.desiredState)
        assertEquals(RuntimeState.STOPPED, stateCoordinator.currentSnapshot.runtimeState)
        assertTrue(diagnosticRecorder.recordedEvents.any { it.eventType == "USER_DISABLE_CLICKED" })
    }

    @Test
    fun testClearDiagnostics() = testScope.runTest {
        diagnosticRecorder.recordEvent("TEST_EVT", "TEST_COMP")
        assertEquals(1, diagnosticRecorder.recordedEvents.size)

        viewModel.onClearDiagnostics()
        advanceUntilIdle()

        assertTrue(diagnosticRecorder.recordedEvents.isEmpty())
    }

    @Test
    fun testSettingsMutations() = testScope.runTest {
        viewModel.updateButtonSize(64)
        viewModel.updateIdleOpacity(50)
        viewModel.updateIdleFadeDelay(5)
        viewModel.updatePopoutDismissDelay(6)
        viewModel.updateHapticFeedback(false)
        viewModel.updateSnapToEdge(false)
        advanceUntilIdle()

        val settings = userPreferences.internalSettingsFlow.value
        assertEquals(64, settings.buttonSizeDp)
        assertEquals(50, settings.idleOpacityPercent)
        assertEquals(5, settings.idleFadeSeconds)
        assertEquals(6, settings.popoutDismissSeconds)
        assertEquals(false, settings.hapticFeedbackEnabled)
        assertEquals(false, settings.snapToEdgeEnabled)
    }

    @Test
    fun testResetSettingsToDefault() = testScope.runTest {
        viewModel.updateButtonSize(48)
        advanceUntilIdle()
        assertEquals(48, userPreferences.internalSettingsFlow.value.buttonSizeDp)

        viewModel.resetSettingsToDefault()
        advanceUntilIdle()

        val settings = userPreferences.internalSettingsFlow.value
        assertEquals(56, settings.buttonSizeDp)
        assertEquals(75, settings.idleOpacityPercent)
        assertEquals(3, settings.idleFadeSeconds)
        assertEquals(4, settings.popoutDismissSeconds)
        assertEquals(true, settings.hapticFeedbackEnabled)
        assertEquals(true, settings.snapToEdgeEnabled)
    }

    @Test
    fun testPauseForPaymentsAndResume() = testScope.runTest {
        viewModel.onToggleMaster(true)
        advanceUntilIdle()
        assertEquals(DesiredState.ENABLED, stateCoordinator.currentSnapshot.desiredState)

        viewModel.onPauseForPayments()
        advanceUntilIdle()
        assertEquals(DesiredState.PAUSED_FOR_PAYMENTS, stateCoordinator.currentSnapshot.desiredState)
        assertEquals(UserFacingStatus.PAUSED_FOR_PAYMENTS, stateCoordinator.currentStatus)
        assertTrue(diagnosticRecorder.recordedEvents.any { it.eventType == "PAUSE_FOR_PAYMENTS_TRIGGERED" })

        viewModel.onResumeFromPayments()
        advanceUntilIdle()
        assertEquals(DesiredState.ENABLED, stateCoordinator.currentSnapshot.desiredState)
        assertTrue(diagnosticRecorder.recordedEvents.any { it.eventType == "RESUME_FROM_PAYMENTS_TRIGGERED" })
    }
}

