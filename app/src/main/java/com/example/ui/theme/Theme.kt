package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FortressColorScheme = darkColorScheme(
    primary = GuardGreen,
    secondary = SecureBlue,
    tertiary = AccentGold,
    background = OledBlack,
    surface = CardDark,
    onPrimary = OledBlack,
    onSecondary = OledBlack,
    onTertiary = OledBlack,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = PanelDark,
    onSurfaceVariant = TextGray,
    error = DangerRed,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force secure dark mode
    dynamicColor: Boolean = false, // Pure custom styling
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FortressColorScheme,
        typography = Typography,
        content = content
    )
}
