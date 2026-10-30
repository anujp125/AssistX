package com.assistx.accessibility

import android.content.Context
import android.media.AudioManager
import com.assistx.service.AssistAccessibilityService

/**
 * Abstraction for triggering global platform accessibility and system actions.
 */
interface ActionExecutorContract {
    fun lockScreen(): Boolean
    fun takeScreenshot(): Boolean
    fun performHome(): Boolean
    fun performBack(): Boolean
    fun openVolumePanel(): Boolean
    fun openPowerDialog(): Boolean
    fun disableAccessibilityService(): Boolean
}

class AndroidActionExecutor(private val context: Context? = null) : ActionExecutorContract {
    override fun lockScreen(): Boolean = AssistAccessibilityService.lockScreen()
    override fun takeScreenshot(): Boolean = AssistAccessibilityService.takeScreenshot()
    override fun performHome(): Boolean = AssistAccessibilityService.performHome()
    override fun performBack(): Boolean = AssistAccessibilityService.performBack()
    override fun openPowerDialog(): Boolean = AssistAccessibilityService.openPowerDialog()
    override fun disableAccessibilityService(): Boolean = AssistAccessibilityService.disableSelfSafely()
    override fun openVolumePanel(): Boolean {
        return try {
            val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                audioManager.adjustSuggestedStreamVolume(
                    AudioManager.ADJUST_SAME,
                    AudioManager.USE_DEFAULT_STREAM_TYPE,
                    AudioManager.FLAG_SHOW_UI
                )
                android.util.Log.i("AssistX", "openVolumePanel executed successfully")
                true
            } else {
                android.util.Log.w("AssistX", "openVolumePanel failed: audioManager is null (context=$context)")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("AssistX", "openVolumePanel error", e)
            false
        }
    }
}
