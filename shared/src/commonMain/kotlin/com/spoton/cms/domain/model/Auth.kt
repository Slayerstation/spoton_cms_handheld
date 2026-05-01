package com.spoton.cms.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String,
    val token: String,
    val refreshToken: String? = null
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthToken(
    val token: String,
    val refreshToken: String? = null
)
