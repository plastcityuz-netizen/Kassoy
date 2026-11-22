package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Dark palette tokens
val DarkBg = Color(0xFF060A12)
val DarkSurface = Color(0xFF0D1422)
val DarkElevated = Color(0xFF131C2E)
val DarkBorder = Color(0x14FFFFFF) // rgba(255,255,255,0.08)
val DarkGlassHighlight = Color(0x2EFFFFFF) // rgba(255,255,255,0.18)
val DarkText = Color(0xFFEAF0FA)
val DarkMuted = Color(0xFF8B97AD)

// Light palette tokens
val LightBg = Color(0xFFF5F7FA)
val LightSurface = Color(0xFFFFFFFF)
val LightElevated = Color(0xFFF0F4FA)
val LightBorder = Color(0x140A1428) // rgba(10,20,40,0.08)
val LightGlassHighlight = Color(0xA6FFFFFF) // rgba(255,255,255,0.65)
val LightText = Color(0xFF0A1220)
val LightMuted = Color(0xFF5B6779)

// Brand & AI accents
val EmeraldDark = Color(0xFF19E3A6)
val EmeraldLightFill = Color(0xFF00A97A)
val EmeraldLightText = Color(0xFF007A59)
val EmeraldOnPrimary = Color(0xFF04130E)

val AuroraViolet = Color(0xFF7C5CFF)
val AuroraCyan = Color(0xFF2DD4FF)

// Semantic status
val StatusSuccess = Color(0xFF22C55E)
val StatusWarning = Color(0xFFFFB020)
val StatusDanger = Color(0xFFFF5C7A)
val StatusInfo = Color(0xFF38BDF8)

@Immutable
data class BalansPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val border: Color,
    val glassStart: Color,
    val glassEnd: Color,
    val glassRim: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val emerald: Color,
    val emeraldText: Color,
    val onEmerald: Color,
    val violet: Color,
    val cyan: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color
)

val DarkBalansPalette = BalansPalette(
    isDark = true,
    background = DarkBg,
    surface = DarkSurface,
    elevated = DarkElevated,
    border = DarkBorder,
    glassStart = Color(0x1AFFFFFF),
    glassEnd = Color(0x0AFFFFFF),
    glassRim = Color(0x2EFFFFFF),
    textPrimary = DarkText,
    textMuted = DarkMuted,
    emerald = EmeraldDark,
    emeraldText = EmeraldDark,
    onEmerald = EmeraldOnPrimary,
    violet = AuroraViolet,
    cyan = AuroraCyan,
    success = StatusSuccess,
    warning = StatusWarning,
    danger = StatusDanger,
    info = StatusInfo
)

val LightBalansPalette = BalansPalette(
    isDark = false,
    background = LightBg,
    surface = LightSurface,
    elevated = LightElevated,
    border = LightBorder,
    glassStart = Color(0xD9FFFFFF),
    glassEnd = Color(0xA6FFFFFF),
    glassRim = Color(0xCCFFFFFF),
    textPrimary = LightText,
    textMuted = LightMuted,
    emerald = EmeraldLightFill,
    emeraldText = EmeraldLightText,
    onEmerald = EmeraldOnPrimary,
    violet = AuroraViolet,
    cyan = AuroraCyan,
    success = StatusSuccess,
    warning = StatusWarning,
    danger = StatusDanger,
    info = StatusInfo
)

val LocalBalansPalette = staticCompositionLocalOf { DarkBalansPalette }
