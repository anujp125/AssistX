package com.nudge.assist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nudge.assist.R

/**
 * Microsoft Selawik open-source typeface (Segoe UI-compatible, SIL Open Font License).
 */
val SelawikFontFamily = FontFamily(
    Font(R.font.selawik_light, FontWeight.Light),
    Font(R.font.selawik_regular, FontWeight.Normal),
    Font(R.font.selawik_semibold, FontWeight.SemiBold),
    Font(R.font.selawik_bold, FontWeight.Bold)
)

val MetroTypography = Typography(
    // Large Metro page titles: light, ~42sp
    displayLarge = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    // Pivot / Panorama section headers
    headlineLarge = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 30.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    // Section headers
    titleLarge = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    // Body text
    bodyLarge = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Labels, buttons, tags
    labelLarge = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SelawikFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
