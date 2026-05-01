package com.spoton.cms.data.repository

import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.domain.model.StyleConfig

class StyleRepository(private val api: SpotOnApi) {

    suspend fun getStyleConfig(): Result<StyleConfig> {
        return try {
            Result.success(api.getStyleConfig())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStyleConfig(config: StyleConfig): Result<Unit> {
        return try {
            api.updateStyleConfig(config)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
