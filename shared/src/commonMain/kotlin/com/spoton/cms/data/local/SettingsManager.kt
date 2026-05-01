package com.spoton.cms.data.local

import com.russhwolf.settings.Settings

/**
 * Manages persistent local settings using multiplatform-settings.
 * Stores JWT token, server URL, and user preferences.
 */
class SettingsManager(private val settings: Settings) {

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    // ── JWT ─────────────────────────────────────────────────────────

    var jwtToken: String?
        get() = settings.getStringOrNull(KEY_JWT_TOKEN)
        set(value) {
            if (value != null) settings.putString(KEY_JWT_TOKEN, value)
            else settings.remove(KEY_JWT_TOKEN)
        }

    var refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)
        set(value) {
            if (value != null) settings.putString(KEY_REFRESH_TOKEN, value)
            else settings.remove(KEY_REFRESH_TOKEN)
        }

    // ── Server URL ──────────────────────────────────────────────────

    var serverUrl: String
        get() = settings.getString(KEY_SERVER_URL, "")
        set(value) = settings.putString(KEY_SERVER_URL, value)

    // ── User ────────────────────────────────────────────────────────

    var username: String
        get() = settings.getString(KEY_USERNAME, "")
        set(value) = settings.putString(KEY_USERNAME, value)

    // ── FCM ─────────────────────────────────────────────────────────

    var fcmToken: String?
        get() = settings.getStringOrNull(KEY_FCM_TOKEN)
        set(value) {
            if (value != null) settings.putString(KEY_FCM_TOKEN, value)
            else settings.remove(KEY_FCM_TOKEN)
        }

    // ── Preferences ─────────────────────────────────────────────────

    var darkMode: Boolean
        get() = settings.getBoolean(KEY_DARK_MODE, true)
        set(value) = settings.putBoolean(KEY_DARK_MODE, value)

    // ── Auth state ──────────────────────────────────────────────────

    val isLoggedIn: Boolean get() = !jwtToken.isNullOrBlank()

    fun clearAuth() {
        jwtToken = null
        refreshToken = null
        username = ""
    }
}
