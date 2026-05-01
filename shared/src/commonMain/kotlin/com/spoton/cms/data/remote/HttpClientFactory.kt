package com.spoton.cms.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Creates a configured Ktor HttpClient for the SpotOn CMS.
 * Automatically injects the JWT Bearer token into every request
 * and handles JSON serialization.
 */
fun createHttpClient(tokenProvider: () -> String?): HttpClient {
    return HttpClient {
        // JSON serialization
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
                encodeDefaults = true
            })
        }

        // JWT Bearer auth — automatically injected on every request
        install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenProvider()
                    if (token != null) {
                        BearerTokens(token, "")
                    } else null
                }
            }
        }

        // Logging (debug builds)
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.HEADERS
        }

        // Timeouts
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }

        // Default headers
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}
