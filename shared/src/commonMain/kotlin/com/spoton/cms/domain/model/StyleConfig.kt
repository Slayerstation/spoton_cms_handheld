package com.spoton.cms.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the global style configuration for the webshop frontend.
 * These values map directly to the CSS custom properties defined in
 * the frontend's globals.css (:root block).
 */
@Serializable
data class StyleConfig(
    val primary: String = "#FF8800",
    val primaryForeground: String = "#FFFFFF",
    val background: String = "#f0f0f0",
    val foreground: String = "#0a0a0a",
    val muted: String = "#f4f4f5",
    val mutedForeground: String = "#52525b",

    // Dark mode overrides
    val darkBackground: String = "#0f0f0f",
    val darkForeground: String = "#ffffff",
    val darkMuted: String = "#27272a",
    val darkMutedForeground: String = "#d4d4d8"
)
