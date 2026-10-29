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

        runtimeController = RuntimeController(
            context = null,
            stateCoordinator = stateCoordinator,
            overlayManager = overlayManager,
            recoveryPolicy = RecoveryPolicy(),
            permissionAdapter = permissionAdapter,
            preferences = FakeUserPreferences(),
            serviceLauncher = FakeServiceLauncher(),
            scope = testScope,
            ioDispatcher = testDispatcher
        )

        viewModel = StudioViewModel(
            stateCoordinator = stateCoordinator,
            runtimeController = runtimeController,
            permissionAdapter = permissionAdapter,
            accessibilityAdapter = accessibilityAdapter,
            diagnosticRecorder = diagnosticRecorder
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
}
