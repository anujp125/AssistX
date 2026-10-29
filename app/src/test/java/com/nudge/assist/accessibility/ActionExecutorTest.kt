package com.nudge.assist.accessibility

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeActionExecutor(
    var shouldLockSucceed: Boolean = true,
    var shouldHomeSucceed: Boolean = true,
    var shouldBackSucceed: Boolean = true
) : ActionExecutorContract {

    var lockScreenCallCount = 0
    var performHomeCallCount = 0
    var performBackCallCount = 0

    override fun lockScreen(): Boolean {
        lockScreenCallCount++
        return shouldLockSucceed
    }

    override fun performHome(): Boolean {
        performHomeCallCount++
        return shouldHomeSucceed
    }

    override fun performBack(): Boolean {
        performBackCallCount++
        return shouldBackSucceed
    }
}

class ActionExecutorTest {

    @Test
    fun testLockScreenExecution() {
        val executor = FakeActionExecutor(shouldLockSucceed = true)
        assertTrue(executor.lockScreen())
        org.junit.Assert.assertEquals(1, executor.lockScreenCallCount)

        executor.shouldLockSucceed = false
        assertFalse(executor.lockScreen())
        org.junit.Assert.assertEquals(2, executor.lockScreenCallCount)
    }

    @Test
    fun testPerformHomeExecution() {
        val executor = FakeActionExecutor(shouldHomeSucceed = true)
        assertTrue(executor.performHome())
        org.junit.Assert.assertEquals(1, executor.performHomeCallCount)

        executor.shouldHomeSucceed = false
        assertFalse(executor.performHome())
        org.junit.Assert.assertEquals(2, executor.performHomeCallCount)
    }

    @Test
    fun testPerformBackExecution() {
        val executor = FakeActionExecutor(shouldBackSucceed = true)
        assertTrue(executor.performBack())
        org.junit.Assert.assertEquals(1, executor.performBackCallCount)

        executor.shouldBackSucceed = false
        assertFalse(executor.performBack())
        org.junit.Assert.assertEquals(2, executor.performBackCallCount)
    }
}

