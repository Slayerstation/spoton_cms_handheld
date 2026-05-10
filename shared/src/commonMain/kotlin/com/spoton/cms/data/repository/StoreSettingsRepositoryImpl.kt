package com.spoton.cms.data.repository

import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.domain.model.StoreSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StoreSettingsRepositoryImpl(
    private val api: SpotOnApi
) : StoreSettingsRepository {

    private val _settings = MutableStateFlow(StoreSettings())
    
    companion object {
        private const val OPTION_NAME = "spoton_universal_settings"
    }

    override suspend fun getSettings(): StoreSettings {
        return try {
            val response = api.getOption(OPTION_NAME)
            val settings = response.value?.let { jsonElement ->
                val rawValue = jsonElement.toString()
                
                // 1. If it's already a JSON object (starts with {)
                if (rawValue.startsWith("{")) {
                    Json.decodeFromString<StoreSettings>(rawValue)
                } else {
                    // 2. If it's a double-encoded string (common in WP Options)
                    val unquoted = rawValue
                        .removePrefix("\"")
                        .removeSuffix("\"")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                    
                    if (unquoted.startsWith("{")) {
                        Json.decodeFromString<StoreSettings>(unquoted)
                    } else {
                        StoreSettings()
                    }
                }
            } ?: StoreSettings()
            
            _settings.value = settings
            settings
        } catch (e: Exception) {
            // Fallback to defaults (which are now pre-populated with .env values)
            val defaults = StoreSettings()
            _settings.value = defaults
            defaults
        }
    }

    override suspend fun saveSettings(settings: StoreSettings) {
        try {
            val jsonString = Json.encodeToString(settings)
            api.updateOption(OPTION_NAME, jsonString)
            _settings.value = settings
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getSystemInfo(): Result<com.spoton.cms.domain.model.BackendSystemInfo> {
        return try {
            Result.success(api.getSystemInfo())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSettingsFlow() = _settings.asStateFlow()
}
