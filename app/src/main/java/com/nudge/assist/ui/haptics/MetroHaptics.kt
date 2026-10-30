package com.nudge.assist.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Central Haptics Engine for Metro UI.
 * Provides unified tactile feedback across Compose screens and WindowManager overlay views.
 */
class MetroHaptics(private val context: Context) {

    var isEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Standard UI tap feedback (tiles, buttons, swatches).
     */
    fun tap(view: View? = null) {
        if (!isEnabled) return
        performDirectEffect(
            view = view,
            predefinedEffect = VibrationEffect.EFFECT_CLICK,
            fallbackDurationMs = 12L,
            fallbackAmplitude = 120,
            viewConstant = HapticFeedbackConstants.KEYBOARD_TAP
        )
    }

    /**
     * Pivot header change or section select.
     */
    fun select(view: View? = null) {
        if (!isEnabled) return
        performDirectEffect(
            view = view,
            predefinedEffect = VibrationEffect.EFFECT_TICK,
            fallbackDurationMs = 16L,
            fallbackAmplitude = 150,
            viewConstant = HapticFeedbackConstants.CLOCK_TICK
        )
    }

    /**
     * Very subtle drag tracking tick.
     */
    fun light(view: View? = null) {
        if (!isEnabled) return
        performDirectEffect(
            view = view,
            predefinedEffect = VibrationEffect.EFFECT_TICK,
            fallbackDurationMs = 8L,
            fallbackAmplitude = 70,
            viewConstant = HapticFeedbackConstants.CLOCK_TICK
        )
    }

    /**
     * Action confirmed / success feedback.
     */
    fun success(view: View? = null) {
        if (!isEnabled) return
        try {
            if (vibrator?.hasVibrator() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 15, 60, 20)
                val amplitudes = intArrayOf(0, 140, 0, 180)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            }
        } catch (e: Exception) {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
        }
    }

    /**
     * Warning or required action prompt.
     */
    fun warning(view: View? = null) {
        if (!isEnabled) return
        try {
            if (vibrator?.hasVibrator() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 30, 80, 30)
                val amplitudes = intArrayOf(0, 160, 0, 160)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            }
        } catch (e: Exception) {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
        }
    }

    /**
     * Error feedback.
     */
    fun error(view: View? = null) {
        if (!isEnabled) return
        performDirectEffect(
            view = view,
            predefinedEffect = VibrationEffect.EFFECT_HEAVY_CLICK,
            fallbackDurationMs = 35L,
            fallbackAmplitude = 220,
            viewConstant = HapticFeedbackConstants.REJECT
        )
    }

    private fun performDirectEffect(
        view: View?,
        predefinedEffect: Int,
        fallbackDurationMs: Long,
        fallbackAmplitude: Int,
        viewConstant: Int
    ) {
        var vibrated = false
        try {
            if (vibrator?.hasVibrator() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        vibrator?.vibrate(VibrationEffect.createPredefined(predefinedEffect))
                        vibrated = true
                    } catch (e: Exception) {
                        // Predefined effect unsupported on hardware, fallback to one-shot
                    }
                }
                if (!vibrated) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(fallbackDurationMs, fallbackAmplitude))
                    vibrated = true
                }
            }
        } catch (e: Exception) {
            // Permission or hardware exception
        }

        if (!vibrated && view != null) {
            try {
                // FLAG_IGNORE_VIEW_SETTING ensures haptics fire even if window lacks focus (e.g. overlay)
                view.performHapticFeedback(viewConstant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            } catch (e: Exception) {
                view.performHapticFeedback(viewConstant)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: MetroHaptics? = null

        fun from(context: Context): MetroHaptics {
            return instance ?: synchronized(this) {
                instance ?: MetroHaptics(context.applicationContext).also { instance = it }
            }
        }
    }
}

