package com.spoton.cms.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    @SerialName("user_email") val email: String,
    @SerialName("user_nicename") val nicename: String,
    @SerialName("user_display_name") val displayName: String,
    val token: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthToken(
    val token: String,
    @SerialName("user_email") val userEmail: String? = null,
    @SerialName("user_nicename") val userNicename: String? = null,
    @SerialName("user_display_name") val userDisplayName: String? = null
)
