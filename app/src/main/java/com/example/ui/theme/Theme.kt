package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Apple iOS Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = AppleSystemBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF003D82),
    onPrimaryContainer = Color(0xFFD6E8FF),
    secondary = AppleSystemIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2C2C2E),
    onSecondaryContainer = Color.White,
    tertiary = AppleSystemGreen,
    onTertiary = Color.White,
    background = AppleDarkGroupedBackground,
    onBackground = AppleDarkLabel,
    surface = AppleDarkSurface,
    onSurface = AppleDarkLabel,
    surfaceVariant = AppleDarkSecondarySurface,
    onSurfaceVariant = AppleDarkSecondaryLabel,
    outline = AppleDarkSeparator,
    error = AppleSystemRed,
    onError = Color.White
)

// Apple iOS Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = AppleSystemBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F1FF),
    onPrimaryContainer = Color(0xFF0051B3),
    secondary = AppleSystemIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2F2F7),
    onSecondaryContainer = Color(0xFF1C1C1E),
    tertiary = AppleSystemGreen,
    onTertiary = Color.White,
    background = AppleLightGroupedBackground,
    onBackground = AppleLightLabel,
    surface = AppleLightSurface,
    onSurface = AppleLightLabel,
    surfaceVariant = AppleLightSecondarySurface,
    onSurfaceVariant = AppleLightSecondaryLabel,
    outline = AppleLightSeparator,
    error = AppleSystemRed,
    onError = Color.White
)

// iOS Squircle Soft Shapes
val AppleShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EspacioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppleShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    EspacioTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
