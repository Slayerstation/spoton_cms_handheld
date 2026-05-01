package com.spoton.cms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── SpotOn Brand Colors ─────────────────────────────────────────────

val SpotOnOrange = Color(0xFFFF8800)
val SpotOnOrangeDark = Color(0xFFCC6D00)
val SpotOnOrangeLight = Color(0xFFFFAA44)

// ── Dark Theme ──────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary = SpotOnOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3D2000),
    onPrimaryContainer = SpotOnOrangeLight,

    secondary = Color(0xFFDDB892),
    onSecondary = Color(0xFF3E2700),
    secondaryContainer = Color(0xFF573B14),
    onSecondaryContainer = Color(0xFFF9DDB7),

    tertiary = Color(0xFFB0CAA5),
    onTertiary = Color(0xFF1D3517),
    tertiaryContainer = Color(0xFF344C2C),
    onTertiaryContainer = Color(0xFFCCE7BF),

    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFEAE1D9),

    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFEAE1D9),
    surfaceVariant = Color(0xFF252525),
    onSurfaceVariant = Color(0xFFD3C4B4),

    outline = Color(0xFF9D8E7F),
    outlineVariant = Color(0xFF4F4539),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

// ── Light Theme ─────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = SpotOnOrange,
    onPrimary = Color.White,
    primaryContainer = SpotOnOrangeLight,
    onPrimaryContainer = Color(0xFF2A1700),

    secondary = Color(0xFF745B3E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF291804),

    tertiary = Color(0xFF586249),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE8C8),
    onTertiaryContainer = Color(0xFF161E0B),

    background = Color(0xFFFFF8F3),
    onBackground = Color(0xFF1F1B16),

    surface = Color(0xFFFFF8F3),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFF2E2D0),
    onSurfaceVariant = Color(0xFF504539),

    outline = Color(0xFF827568),
    outlineVariant = Color(0xFFD6C6B5),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

// ── Glass Effect Colors ─────────────────────────────────────────────

object GlassColors {
    val cardBackground @Composable get() =
        if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.12f)
        else Color.White.copy(alpha = 0.85f)

    val cardBorder @Composable get() =
        if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.1f)
        else Color.Black.copy(alpha = 0.08f)

    val overlayBackground @Composable get() =
        if (isSystemInDarkTheme()) Color.Black.copy(alpha = 0.5f)
        else Color.Black.copy(alpha = 0.3f)
}

// ── Theme Composable ────────────────────────────────────────────────

@Composable
fun SpotOnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpotOnTypography,
        content = content
    )
}
