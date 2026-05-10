package com.spoton.cms.data.repository

import com.spoton.cms.domain.model.BackendSystemInfo
import com.spoton.cms.domain.model.StoreSettings
import kotlinx.coroutines.flow.Flow

interface StoreSettingsRepository {
    suspend fun getSettings(): StoreSettings
    suspend fun saveSettings(settings: StoreSettings)
    suspend fun getSystemInfo(): Result<BackendSystemInfo>
    fun getSettingsFlow(): Flow<StoreSettings>
}
