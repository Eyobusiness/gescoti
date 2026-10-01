package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GescotiColorScheme = lightColorScheme(
    primary = GescotiMarianNavy,
    onPrimary = Color.White,
    primaryContainer = GescotiNavyContainer,
    onPrimaryContainer = GescotiMarianNavy,
    secondary = GescotiSkyBlue,
    onSecondary = Color.White,
    secondaryContainer = GescotiSkyBlueContainer,
    onSecondaryContainer = GescotiMarianNavy,
    tertiary = GescotiMagentaPink,
    onTertiary = Color.White,
    tertiaryContainer = GescotiMagentaContainer,
    background = GescotiPureWhite,
    onBackground = GescotiTextPrimary,
    surface = GescotiPureWhite,
    onSurface = GescotiTextPrimary,
    surfaceVariant = GescotiSurfaceLight,
    onSurfaceVariant = GescotiTextSecondary,
    outline = GescotiBorderLight
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GescotiColorScheme,
        typography = Typography,
        content = content
    )
}
