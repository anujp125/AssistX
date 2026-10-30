package com.assistx.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Windows Phone "Metro" (Modern UI) Color Palette.
 * True black #000000 base with 20 classic flat accent colors.
 */

// True Black & Pure White Foundations
val MetroBlack = Color(0xFF000000)
val MetroDarkTile = Color(0xFF161616)
val MetroDarkBorder = Color(0xFF2E2E2E)
val MetroDarkTextSecondary = Color(0xFF888888)

val MetroWhite = Color(0xFFFFFFFF)
val MetroLightTile = Color(0xFFF0F0F0)
val MetroLightBorder = Color(0xFFCCCCCC)
val MetroLightTextSecondary = Color(0xFF666666)

// Status Cues (Flat solid)
val MetroStatusGreen = Color(0xFF008A00) // Emerald
val MetroStatusAmber = Color(0xFFF0A30A) // Amber
val MetroStatusRed = Color(0xFFE51400)   // Crimson / Red

val StatusGreen = MetroStatusGreen
val StatusAmber = MetroStatusAmber
val StatusRed = MetroStatusRed

/**
 * The 20 Classic Windows Phone Metro Accent Colors.
 */
enum class MetroAccent(val label: String, val color: Color, val hex: String) {
    LIME("lime", Color(0xFFA4C400), "#A4C400"),
    GREEN("green", Color(0xFF60A917), "#60A917"),
    EMERALD("emerald", Color(0xFF008A00), "#008A00"),
    TEAL("teal", Color(0xFF00ABA9), "#00ABA9"),
    CYAN("cyan", Color(0xFF1BA1E2), "#1BA1E2"),
    COBALT("cobalt", Color(0xFF0050EF), "#0050EF"), // Default
    INDIGO("indigo", Color(0xFF6A00FF), "#6A00FF"),
    VIOLET("violet", Color(0xFFAA00FF), "#AA00FF"),
    PINK("pink", Color(0xFFF472D0), "#F472D0"),
    MAGENTA("magenta", Color(0xFFD80073), "#D80073"),
    CRIMSON("crimson", Color(0xFFA20025), "#A20025"),
    RED("red", Color(0xFFE51400), "#E51400"),
    ORANGE("orange", Color(0xFFFA6800), "#FA6800"),
    AMBER("amber", Color(0xFFF0A30A), "#F0A30A"),
    YELLOW("yellow", Color(0xFFE3C800), "#E3C800"),
    BROWN("brown", Color(0xFF825A2C), "#825A2C"),
    OLIVE("olive", Color(0xFF6D8764), "#6D8764"),
    STEEL("steel", Color(0xFF647687), "#647687"),
    MAUVE("mauve", Color(0xFF76608A), "#76608A"),
    TAUPE("taupe", Color(0xFF87794E), "#87794E");

    companion object {
        fun fromHex(hex: String?): MetroAccent {
            return values().firstOrNull { it.hex.equals(hex, ignoreCase = true) } ?: COBALT
        }
    }
}
