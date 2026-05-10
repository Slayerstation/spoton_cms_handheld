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
        return try {
            settings.serverUrl = serverUrl.trimEnd('/')
            val token = api.login(username, password)
            
            settings.jwtToken = token.token
            // JWT plugin doesn't have refresh token by default, so we check for null
            token.userDisplayName?.let { settings.username = it }
            
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
