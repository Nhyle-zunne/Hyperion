package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7FAEEB),
    onPrimary = Color(0xFF06203D),
    primaryContainer = Color(0xFF132F52),
    onPrimaryContainer = Color(0xFFD4E3FC),
    secondary = Color(0xFF90B8E4),
    onSecondary = Color(0xFF0F2B48),
    secondaryContainer = Color(0xFF1B395E),
    onSecondaryContainer = Color(0xFFD8E7FA),
    tertiary = MaritimeGoldLight,
    onTertiary = Color(0xFF3E2C00),
    tertiaryContainer = Color(0xFF5A4100),
    onTertiaryContainer = Color(0xFFFFDF9B),
    background = DarkBg,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBAC7D5),
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = MaritimeNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E5F8),
    onPrimaryContainer = Color(0xFF07203E),
    secondary = MaritimeOceanBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6F5),
    onSecondaryContainer = Color(0xFF0B2138),
    tertiary = Color(0xFF9A7200),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDF9E),
    onTertiaryContainer = Color(0xFF281C00),
    background = LightBg,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF384656),
    outline = LightOutline
)

@Composable
fun HyperionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep maritime identity strong by default
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
