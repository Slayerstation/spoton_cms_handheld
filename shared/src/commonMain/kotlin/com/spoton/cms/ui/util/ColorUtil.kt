package com.spoton.cms.ui.util

import androidx.compose.ui.graphics.Color

/**
 * Parses a hex color string (e.g. "#FF8800" or "#AAFF8800") into a Compose Color.
 * Works on all platforms without relying on android.graphics.Color.
 */
fun parseHexColor(hex: String): Color {
    if (hex.isBlank() || !hex.startsWith("#")) return Color.Gray
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorLong = when (cleanHex.length) {
            6 -> "FF$cleanHex".toLong(16)
            8 -> cleanHex.toLong(16)
            else -> return Color.Gray
        }
        Color(
            red = ((colorLong shr 16) and 0xFF).toInt(),
            green = ((colorLong shr 8) and 0xFF).toInt(),
            blue = (colorLong and 0xFF).toInt(),
            alpha = ((colorLong shr 24) and 0xFF).toInt()
        )
    } catch (e: Exception) {
        Color.Gray
    }
}
