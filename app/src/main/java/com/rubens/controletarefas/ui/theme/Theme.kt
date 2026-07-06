package com.rubens.controletarefas.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = DarkGray,
    onPrimaryContainer = White,
    secondary = MediumGray,
    onSecondary = White,
    secondaryContainer = LightGray,
    onSecondaryContainer = Black,
    tertiary = AccentGray,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = SoftWhite,
    onSurfaceVariant = MediumGray,
    outline = BorderGray,
    outlineVariant = LightGray
)

@Composable
fun ControleTarefasTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
