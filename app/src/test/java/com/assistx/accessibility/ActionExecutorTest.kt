package com.assistx.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeActionExecutor(
    var shouldLockSucceed: Boolean = true,
    var shouldScreenshotSucceed: Boolean = true,
    var shouldHomeSucceed: Boolean = true,
    var shouldBackSucceed: Boolean = true,
    var shouldVolumeSucceed: Boolean = true,
    var shouldPowerSucceed: Boolean = true,
    var shouldDisableA11ySucceed: Boolean = true
) : ActionExecutorContract {

    var lockScreenCallCount = 0
    var takeScreenshotCallCount = 0
    var performHomeCallCount = 0
    var performBackCallCount = 0
    var openVolumePanelCallCount = 0
    var openPowerDialogCallCount = 0
    var disableA11yCallCount = 0

    override fun lockScreen(): Boolean {
        lockScreenCallCount++
        return shouldLockSucceed
    }

    override fun takeScreenshot(): Boolean {
        takeScreenshotCallCount++
        return shouldScreenshotSucceed
    }

    override fun performHome(): Boolean {
        performHomeCallCount++
        return shouldHomeSucceed
    }

    override fun performBack(): Boolean {
        performBackCallCount++
        return shouldBackSucceed
    }

    override fun openVolumePanel(): Boolean {
        openVolumePanelCallCount++
        return shouldVolumeSucceed
    }

    override fun openPowerDialog(): Boolean {
        openPowerDialogCallCount++
        return shouldPowerSucceed
    }

    override fun disableAccessibilityService(): Boolean {
        disableA11yCallCount++
        return shouldDisableA11ySucceed
    }
}

class ActionExecutorTest {

    @Test
    fun testLockScreenExecution() {
        val executor = FakeActionExecutor(shouldLockSucceed = true)
        assertTrue(executor.lockScreen())
        assertEquals(1, executor.lockScreenCallCount)

        executor.shouldLockSucceed = false
        assertFalse(executor.lockScreen())
        assertEquals(2, executor.lockScreenCallCount)
    }

    @Test
    fun testTakeScreenshotExecution() {
        val executor = FakeActionExecutor(shouldScreenshotSucceed = true)
        assertTrue(executor.takeScreenshot())
        assertEquals(1, executor.takeScreenshotCallCount)

        executor.shouldScreenshotSucceed = false
        assertFalse(executor.takeScreenshot())
        assertEquals(2, executor.takeScreenshotCallCount)
    }

    @Test
    fun testPerformHomeExecution() {
        val executor = FakeActionExecutor(shouldHomeSucceed = true)
        assertTrue(executor.performHome())
        assertEquals(1, executor.performHomeCallCount)

        executor.shouldHomeSucceed = false
        assertFalse(executor.performHome())
        assertEquals(2, executor.performHomeCallCount)
    }

    @Test
    fun testPerformBackExecution() {
        val executor = FakeActionExecutor(shouldBackSucceed = true)
        assertTrue(executor.performBack())
        assertEquals(1, executor.performBackCallCount)

        executor.shouldBackSucceed = false
        assertFalse(executor.performBack())
        assertEquals(2, executor.performBackCallCount)
    }

    @Test
    fun testOpenVolumePanelExecution() {
        val executor = FakeActionExecutor(shouldVolumeSucceed = true)
        assertTrue(executor.openVolumePanel())
        assertEquals(1, executor.openVolumePanelCallCount)

        executor.shouldVolumeSucceed = false
        assertFalse(executor.openVolumePanel())
        assertEquals(2, executor.openVolumePanelCallCount)
    }

    @Test
    fun testOpenPowerDialogExecution() {
        val executor = FakeActionExecutor(shouldPowerSucceed = true)
        assertTrue(executor.openPowerDialog())
        assertEquals(1, executor.openPowerDialogCallCount)

        executor.shouldPowerSucceed = false
        assertFalse(executor.openPowerDialog())
        assertEquals(2, executor.openPowerDialogCallCount)
    }

    @Test
    fun testDisableAccessibilityServiceExecution() {
        val executor = FakeActionExecutor(shouldDisableA11ySucceed = true)
        assertTrue(executor.disableAccessibilityService())
        assertEquals(1, executor.disableA11yCallCount)

        executor.shouldDisableA11ySucceed = false
        assertFalse(executor.disableAccessibilityService())
        assertEquals(2, executor.disableA11yCallCount)
    }
}
