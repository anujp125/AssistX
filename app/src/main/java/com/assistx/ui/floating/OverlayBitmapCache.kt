package com.assistx.ui.floating

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.assistx.R
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe singleton cache for immutable overlay bitmap assets.
 * Eliminates repeated BitmapFactory decoding on every ActionPopoutView and FloatingButtonView presentation.
 */
object OverlayBitmapCache {
    private val cache = ConcurrentHashMap<Int, Bitmap>()

    fun getBitmap(context: Context, resId: Int): Bitmap? {
        val existing = cache[resId]
        if (existing != null) return existing
        return try {
            val decoded = BitmapFactory.decodeResource(context.applicationContext.resources, resId)
            if (decoded != null) {
                cache[resId] = decoded
            }
            decoded
        } catch (e: Exception) {
            null
        }
    }

    fun getPowerBitmap(context: Context): Bitmap? = getBitmap(context, R.drawable.ic_action_power)
    fun getVolumeBitmap(context: Context): Bitmap? = getBitmap(context, R.drawable.ic_action_volume)
    fun getScreenshotBitmap(context: Context): Bitmap? = getBitmap(context, R.drawable.ic_action_screenshot)

    fun clear() {
        cache.clear()
    }
}
