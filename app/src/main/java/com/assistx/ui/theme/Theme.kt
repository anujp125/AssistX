package com.assistx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

val LocalMetroAccent = compositionLocalOf { MetroAccent.COBALT }
val LocalMetroIsDark = compositionLocalOf { true }

val MetroShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

enum class ThemeMode(val label: String) {
    DARK("dark"),
    LIGHT("light"),
    SYSTEM("system")
}

@Composable
fun AssistXTheme(
    accent: MetroAccent = MetroAccent.COBALT,
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = accent.color,
            onPrimary = MetroWhite,
            primaryContainer = accent.color,
            onPrimaryContainer = MetroWhite,
            secondary = accent.color,
            onSecondary = MetroWhite,
            background = MetroBlack,
            onBackground = MetroWhite,
            surface = MetroBlack,
            onSurface = MetroWhite,
            surfaceVariant = MetroDarkTile,
            onSurfaceVariant = MetroDarkTextSecondary,
            outline = MetroDarkBorder,
            error = MetroStatusRed,
            onError = MetroWhite
        )
    } else {
        lightColorScheme(
            primary = accent.color,
            onPrimary = MetroWhite,
            primaryContainer = accent.color,
            onPrimaryContainer = MetroWhite,
            secondary = accent.color,
            onSecondary = MetroWhite,
            background = MetroWhite,
            onBackground = MetroBlack,
            surface = MetroWhite,
            onSurface = MetroBlack,
            surfaceVariant = MetroLightTile,
            onSurfaceVariant = MetroLightTextSecondary,
            outline = MetroLightBorder,
            error = MetroStatusRed,
            onError = MetroWhite
        )
    }

    CompositionLocalProvider(
        LocalMetroAccent provides accent,
        LocalMetroIsDark provides isDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MetroTypography,
            shapes = MetroShapes
        ) {
            androidx.compose.material3.ProvideTextStyle(
                value = androidx.compose.material3.LocalTextStyle.current.copy(fontFamily = SelawikFontFamily),
                content = content
            )
        }
    }
}
