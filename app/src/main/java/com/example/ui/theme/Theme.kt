package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.AppStrings
import com.example.core.BalansStrings
import com.example.data.ThemePreference

val LocalAppStrings = staticCompositionLocalOf { AppStrings.uz }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.UZ }

private val BalansDarkColorScheme = darkColorScheme(
    primary = EmeraldDark,
    onPrimary = EmeraldOnPrimary,
    secondary = AuroraViolet,
    onSecondary = DarkText,
    tertiary = AuroraCyan,
    onTertiary = EmeraldOnPrimary,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkElevated,
    onSurfaceVariant = DarkMuted,
    outline = DarkBorder,
    error = StatusDanger,
    onError = DarkText
)

private val BalansLightColorScheme = lightColorScheme(
    primary = EmeraldLightFill,
    onPrimary = EmeraldOnPrimary,
    secondary = AuroraViolet,
    onSecondary = LightSurface,
    tertiary = AuroraCyan,
    onTertiary = LightText,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightElevated,
    onSurfaceVariant = LightMuted,
    outline = LightBorder,
    error = StatusDanger,
    onError = LightSurface
)

val BalansShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun BalansTheme(
    themePreference: ThemePreference = ThemePreference.DARK,
    language: AppLanguage = AppLanguage.UZ,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    val palette = if (isDark) DarkBalansPalette else LightBalansPalette
    val colorScheme = if (isDark) BalansDarkColorScheme else BalansLightColorScheme
    val strings = AppStrings.forLang(language)

    CompositionLocalProvider(
        LocalBalansPalette provides palette,
        LocalAppStrings provides strings,
        LocalAppLanguage provides language
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BalansTypography,
            shapes = BalansShapes,
            content = content
        )
    }
}
