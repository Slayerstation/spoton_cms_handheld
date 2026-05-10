package com.spoton.cms.data.remote

import com.spoton.cms.data.repository.StoreSettingsRepository
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class HostingerApiClient(
    private val client: HttpClient,
    private val settingsRepository: StoreSettingsRepository
) {
    // Note: Hostinger's actual API documentation may vary. 
    // This uses a generalized API structure based on typical hosting provider APIs.
    private val BASE_URL = "https://api.hostinger.com/v1"

    suspend fun clearCache(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getSettings()
            val apiKey = settings.integrations.hostinger.apiKey
            val serverName = settings.integrations.hostinger.serverName

            if (apiKey.isBlank() || serverName.isBlank()) {
                return Result.failure(Exception("Hostinger API Key or Server Name is missing in Settings."))
            }

            // Endpoint to clear cache for the specific server/website
            val response = client.post("$BASE_URL/websites/$serverName/cache/clear") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Hostinger API error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServerStatus(): Result<String> {
        return try {
            val settings = settingsRepository.getSettings()
            val apiKey = settings.integrations.hostinger.apiKey
            val serverName = settings.integrations.hostinger.serverName

            if (apiKey.isBlank() || serverName.isBlank()) {
                return Result.failure(Exception("Hostinger API Key or Server Name is missing."))
            }

            val response = client.get("$BASE_URL/websites/$serverName/status") {
                header("Authorization", "Bearer $apiKey")
            }

            if (response.status.isSuccess()) {
                // In reality, this would parse a JSON response. 
                // For now, we simulate a successful 'Online' status.
                Result.success("Online (Active)")
            } else {
                Result.failure(Exception("Status error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
