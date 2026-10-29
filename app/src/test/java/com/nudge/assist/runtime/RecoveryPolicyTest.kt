package com.nudge.assist.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RecoveryPolicyTest {

    @Test
    fun testInitialState() {
        val policy = RecoveryPolicy()
        assertEquals(0, policy.currentRetryCount)
        assertFalse(policy.isExhausted)
        assertTrue(policy.canAttemptRecovery(isUserDisabled = false))
    }

    @Test
    fun testUserDisabledSuppression() {
        val policy = RecoveryPolicy()
        assertFalse(policy.canAttemptRecovery(isUserDisabled = true))
    }

    @Test
    fun testSequentialAttemptsAndBackoffDelays() {
        var currentTime = 1000L
        val policy = RecoveryPolicy(timeProvider = { currentTime })

        // Attempt 1: 1000ms backoff
        assertTrue(policy.canAttemptRecovery(isUserDisabled = false))
        val delay1 = policy.recordAttempt()
        assertEquals(1000L, delay1)
        assertEquals(1, policy.currentRetryCount)
        assertFalse(policy.isExhausted)

        // Attempt 2: 2000ms backoff
        assertTrue(policy.canAttemptRecovery(isUserDisabled = false))
        val delay2 = policy.recordAttempt()
        assertEquals(2000L, delay2)
        assertEquals(2, policy.currentRetryCount)
        assertFalse(policy.isExhausted)

        // Attempt 3: 5000ms backoff
        assertTrue(policy.canAttemptRecovery(isUserDisabled = false))
        val delay3 = policy.recordAttempt()
        assertEquals(5000L, delay3)
        assertEquals(3, policy.currentRetryCount)
        assertTrue(policy.isExhausted)

        // Attempt 4: Should be exhausted and throw IllegalStateException
        assertFalse(policy.canAttemptRecovery(isUserDisabled = false))
        try {
            policy.recordAttempt()
            fail("Expected IllegalStateException when exhausted")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("exhausted") == true)
        }
    }

    @Test
    fun testCooldownResetAfter60Seconds() {
        var currentTime = 10_000L
        val policy = RecoveryPolicy(timeProvider = { currentTime })

        // 2 attempts made
        policy.recordAttempt()
        policy.recordAttempt()
        assertEquals(2, policy.currentRetryCount)

        // Advance time by 30 seconds (< 60s cooldown)
        currentTime += 30_000L
        assertTrue(policy.canAttemptRecovery(isUserDisabled = false))
        assertEquals(2, policy.currentRetryCount)

        // Advance time to 60+ seconds past last attempt
        currentTime += 30_000L // Total +60s
        assertTrue(policy.canAttemptRecovery(isUserDisabled = false))
        assertEquals(0, policy.currentRetryCount)
        assertFalse(policy.isExhausted)
    }

    @Test
    fun testExplicitResetBudget() {
        val policy = RecoveryPolicy()
        policy.recordAttempt()
        policy.recordAttempt()
        policy.recordAttempt()
        assertTrue(policy.isExhausted)

        policy.resetBudget()
        assertEquals(0, policy.currentRetryCount)
        assertFalse(policy.isExhausted)
        assertTrue(policy.canAttemptRecovery(isUserDisabled = false))
    }
}

