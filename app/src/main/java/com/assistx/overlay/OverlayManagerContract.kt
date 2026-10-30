package com.assistx.overlay

/**
 * Abstraction for managing overlay attachment lifecycle.
 */
interface OverlayManagerContract {
    val isAttached: Boolean
    fun attachOverlay(): Boolean
    fun detachOverlay(): Boolean
    fun onConfigurationChanged() {}
    fun onScreenTurnedOff() {}
    fun onScreenWoken() {}
}

