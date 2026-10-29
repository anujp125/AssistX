package com.nudge.assist.runtime

/**
 * Encapsulates the bounded failure recovery policy for AssistX.
 *
 * Parameters approved under D-018 & ADR-012:
 * - Max retries: 3 attempts
 * - Exponential backoff delays: 1000ms, 2000ms, 5000ms
 * - Cooldown reset: 60,000ms of continuous healthy execution resets the retry budget
 */
class RecoveryPolicy(
    val maxRetries: Int = 3,
    val backoffDelaysMs: List<Long> = listOf(1000L, 2000L, 5000L),
    val resetCooldownMs: Long = 60_000L,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {

    private var retryCount = 0
    private var lastRecoveryTimeMs = 0L
    private var lastHealthyTimeMs = 0L

    val currentRetryCount: Int
        get() = retryCount

    val isExhausted: Boolean
        get() = retryCount >= maxRetries

    /**
     * Determines whether an automatic recovery attempt is permitted.
     * Invariant: If isUserDisabled is true, recovery is NEVER permitted.
     */
    fun canAttemptRecovery(isUserDisabled: Boolean): Boolean {
        if (isUserDisabled) {
            return false
        }
        checkCooldownReset()
        return retryCount < maxRetries
    }

    /**
     * Records an automatic recovery attempt and returns the backoff delay in milliseconds.
     * Throws IllegalStateException if recovery attempts are already exhausted.
     */
    fun recordAttempt(): Long {
        checkCooldownReset()
        if (retryCount >= maxRetries) {
            throw IllegalStateException("Recovery attempts exhausted ($retryCount/$maxRetries)")
        }
        val delay = backoffDelaysMs.getOrElse(retryCount) { backoffDelaysMs.last() }
        retryCount++
        lastRecoveryTimeMs = timeProvider()
        return delay
    }

    /**
     * Signals that the runtime has achieved healthy, uninterrupted operation.
     */
    fun onHealthyObserved() {
        lastHealthyTimeMs = timeProvider()
        checkCooldownReset()
    }

    /**
     * Explicit user action (e.g. toggling the master switch in Studio) resets the retry budget.
     */
    fun resetBudget() {
        retryCount = 0
        lastRecoveryTimeMs = 0L
        lastHealthyTimeMs = timeProvider()
    }

    private fun checkCooldownReset() {
        val now = timeProvider()
        if (retryCount > 0 && lastRecoveryTimeMs > 0L) {
            if (now - lastRecoveryTimeMs >= resetCooldownMs) {
                // 60 seconds of stability has elapsed; reset the retry budget
                retryCount = 0
                lastRecoveryTimeMs = 0L
            }
        }
    }
}
