package com.shadowcore.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple60,
    onPrimary = Color.White,
    primaryContainer = Purple20,
    onPrimaryContainer = Purple80,
    secondary = Violet40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2D2D44),
    onSecondaryContainer = Violet80,
    tertiary = Color(0xFF40C4FF),
    onTertiary = Color.Black,
    background = SurfaceDark,
    onBackground = Color(0xFFE6E1E5),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color(0xFF0A0A14),
    surfaceContainerLow = Color(0xFF121220),
    surfaceContainer = SurfaceDarkElevated,
    surfaceContainerHigh = SurfaceDarkCard,
    surfaceContainerHighest = Color(0xFF24243D),
    outline = Color(0xFF49454F),
    outlineVariant = Color(0xFF2E2E3E),
    error = VeError,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DEF8),
    onPrimaryContainer = Purple20,
    secondary = Violet40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF4A1E60),
    tertiary = Color(0xFF0091EA),
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Color(0xFF1C1B1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF3EDF7),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerHighest = Color(0xFFE6E0E9),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

/**
 * @param themeMode 0 = Auto (system), 1 = Light, 2 = Dark
 */
@Composable
fun ShadowCoreTheme(
    themeMode: Int = 0,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        1 -> false   // Light
        2 -> true    // Dark
        else -> isSystemInDarkTheme()  // Auto
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShadowCoreTypography,
        shapes = ShadowCoreShapes,
        content = content,
    )
}
