package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Emerald700,
    onPrimary = White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = Teal600,
    onSecondary = White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal700,
    tertiary = Amber600,
    onTertiary = White,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Amber700,
    background = Stone50,
    onBackground = Stone900,
    surface = White,
    onSurface = Stone900,
    surfaceVariant = Stone100,
    onSurfaceVariant = Stone700,
    error = Crimson600,
    onError = White,
    errorContainer = Crimson100,
    onErrorContainer = Crimson700
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Stone950,
    primaryContainer = Emerald900,
    onPrimaryContainer = Emerald200,
    secondary = Teal100,
    onSecondary = Stone950,
    secondaryContainer = Teal700,
    onSecondaryContainer = Teal50,
    tertiary = Amber500,
    onTertiary = Stone950,
    tertiaryContainer = Amber700,
    onTertiaryContainer = Amber100,
    background = Stone950,
    onBackground = Stone100,
    surface = Stone900,
    onSurface = Stone100,
    surfaceVariant = Stone800,
    onSurfaceVariant = Stone300,
    error = Crimson500,
    onError = Stone950,
    errorContainer = Crimson700,
    onErrorContainer = Crimson100
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded Emerald colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
