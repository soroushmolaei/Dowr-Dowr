package com.dorino.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DorinoDarkColorScheme = darkColorScheme(
    primary = DorinoPrimary,
    onPrimary = DorinoOnBackground,
    primaryContainer = DorinoPrimaryVariant,
    secondary = DorinoSecondary,
    onSecondary = DorinoBackground,
    background = DorinoBackground,
    onBackground = DorinoOnBackground,
    surface = DorinoSurface,
    onSurface = DorinoOnBackground,
    surfaceVariant = DorinoSurfaceElevated,
    onSurfaceVariant = DorinoOnSurfaceMuted,
    error = DorinoError
)

/**
 * تم اصلی دورینو. همیشه تیره (Dark Premium) و همیشه راست‌به‌چپ است،
 * صرف‌نظر از تنظیمات سیستم، چون بازی فقط فارسی است.
 */
@Composable
fun DorinoTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = DorinoDarkColorScheme,
            typography = DorinoTypography,
            shapes = DorinoShapes,
            content = content
        )
    }
}
