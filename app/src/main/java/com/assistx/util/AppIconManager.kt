package com.assistx.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.assistx.R

/**
 * Curated signature Metro launcher icon variants for AssistX.
 */
enum class AppIconVariant(
    val id: String,
    val label: String,
    val accentHex: String,
    val componentClassName: String,
    val iconResId: Int
) {
    VIOLET(
        id = "VIOLET",
        label = "signature violet",
        accentHex = "#AA00FF",
        componentClassName = "com.assistx.ui.MainActivity",
        iconResId = R.mipmap.ic_launcher_violet
    ),
    PURPLE(
        id = "PURPLE",
        label = "metro purple",
        accentHex = "#6A00FF",
        componentClassName = "com.assistx.ui.MainActivityAliasPurple",
        iconResId = R.mipmap.ic_launcher_purple
    ),
    COBALT(
        id = "COBALT",
        label = "windows cobalt",
        accentHex = "#0050EF",
        componentClassName = "com.assistx.ui.MainActivityAliasCobalt",
        iconResId = R.mipmap.ic_launcher_cobalt
    ),
    EMERALD(
        id = "EMERALD",
        label = "metro emerald",
        accentHex = "#008A00",
        componentClassName = "com.assistx.ui.MainActivityAliasEmerald",
        iconResId = R.mipmap.ic_launcher_emerald
    ),
    CRIMSON(
        id = "CRIMSON",
        label = "deep crimson",
        accentHex = "#A20025",
        componentClassName = "com.assistx.ui.MainActivityAliasCrimson",
        iconResId = R.mipmap.ic_launcher_crimson
    ),
    AMBER(
        id = "AMBER",
        label = "cyber amber",
        accentHex = "#F0A30A",
        componentClassName = "com.assistx.ui.MainActivityAliasAmber",
        iconResId = R.mipmap.ic_launcher_amber
    ),
    CYAN(
        id = "CYAN",
        label = "electric cyan",
        accentHex = "#1BA1E2",
        componentClassName = "com.assistx.ui.MainActivityAliasCyan",
        iconResId = R.mipmap.ic_launcher_cyan
    ),
    MONOCHROME(
        id = "MONOCHROME",
        label = "pure minimal",
        accentHex = "#FFFFFF",
        componentClassName = "com.assistx.ui.MainActivityAliasMonochrome",
        iconResId = R.mipmap.ic_launcher_monochrome
    );

    companion object {
        fun fromId(id: String?): AppIconVariant {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: VIOLET
        }
    }
}

/**
 * Controller for switching launcher icon variants via Android's PackageManager and activity-alias.
 */
object AppIconManager {

    /**
     * Applies the selected launcher icon variant by enabling its corresponding activity/alias
     * and disabling the others using PackageManager.DONT_KILL_APP.
     */
    fun applyIconVariant(context: Context, variant: AppIconVariant): Boolean {
        val pm = context.packageManager
        val packageName = context.packageName

        return try {
            val targetComponent = ComponentName(packageName, variant.componentClassName)
            // 1. Enable target component first so at least one launcher entry is active
            pm.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // 2. Disable all other variants
            for (v in AppIconVariant.values()) {
                if (v != variant) {
                    val otherComponent = ComponentName(packageName, v.componentClassName)
                    pm.setComponentEnabledSetting(
                        otherComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

