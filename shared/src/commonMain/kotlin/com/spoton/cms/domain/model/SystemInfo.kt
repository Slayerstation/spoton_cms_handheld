package com.spoton.cms.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackendSystemInfo(
    @SerialName("php_version") val phpVersion: String,
    @SerialName("mysql_version") val mysqlVersion: String,
    @SerialName("wp_version") val wpVersion: String,
    @SerialName("active_theme") val activeTheme: String,
    @SerialName("active_theme_version") val activeThemeVersion: String,
    @SerialName("site_url") val siteUrl: String,
    @SerialName("debug_mode") val debugMode: Boolean,
    val hosting: HostingInfo
)

@Serializable
data class HostingInfo(
    val status: String,
    val provider: String,
    val usage: HostingUsage? = null,
    val ssl: SslStatus? = null,
    val error: String? = null
)

@Serializable
data class HostingUsage(
    val disk: UsageMetric? = null,
    val inodes: UsageMetric? = null,
    val cpu: UsageMetric? = null,
    @SerialName("ram_usage") val ram: UsageMetric? = null
)

@Serializable
data class UsageMetric(
    val current: String,
    val total: String,
    val percent: Double? = null
)

@Serializable
data class SslStatus(
    val active: Boolean,
    val domain: String,
    @SerialName("days_left") val daysLeft: Int? = null,
    @SerialName("issuer_name") val issuer: String? = null
)
