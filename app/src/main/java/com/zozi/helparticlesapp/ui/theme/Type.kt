package com.zozi.helparticlesapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Modern Editorial typography.
// DESIGN.md specifies Playfair Display for headlines and Inter for body/UI.
// The project does not currently include those font resources, so these use
// platform Serif/SansSerif fallbacks while preserving the intended hierarchy.
val EditorialSerif = FontFamily.Serif
val EditorialSans = FontFamily.SansSerif

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.84).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 31.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 31.sp
    ),
    titleLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 31.sp
    ),
    titleMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = EditorialSans,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 29.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = EditorialSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = EditorialSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontFamily = EditorialSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.7.sp
    ),
    labelSmall = TextStyle(
        fontFamily = EditorialSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp
    )
)
