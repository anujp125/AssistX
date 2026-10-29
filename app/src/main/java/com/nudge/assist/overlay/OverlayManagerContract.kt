package com.nudge.assist.overlay

/**
 * Abstraction for managing overlay attachment lifecycle.
 */
interface OverlayManagerContract {
    val isAttached: Boolean
    fun attachOverlay(): Boolean
    fun detachOverlay(): Boolean
}

