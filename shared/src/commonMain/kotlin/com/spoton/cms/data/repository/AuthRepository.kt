package com.spoton.cms.data.repository

import com.spoton.cms.data.local.SettingsManager
import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.domain.model.AuthToken

class AuthRepository(
    private val api: SpotOnApi,
    private val settings: SettingsManager
) {
    val isLoggedIn: Boolean get() = settings.isLoggedIn
    val currentServerUrl: String get() = settings.serverUrl

    suspend fun login(serverUrl: String, username: String, password: String): Result<AuthToken> {
        // MOCK LOGIN BYPASS for demo purposes
        if (username == "admin" && password == "password") {
            settings.serverUrl = serverUrl.trimEnd('/')
            settings.jwtToken = "mock_jwt_token"
            settings.username = "Admin Demo"
            return Result.success(AuthToken("mock_jwt_token", "mock_refresh_token"))
        }

        return try {
            settings.serverUrl = serverUrl.trimEnd('/')
            val token = api.login(username, password)
            settings.jwtToken = token.token
            settings.refreshToken = token.refreshToken
            settings.username = username
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        settings.clearAuth()
    }

    fun getToken(): String? = settings.jwtToken
}
