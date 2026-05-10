package com.spoton.cms.di

import com.russhwolf.settings.Settings
import com.spoton.cms.data.local.DatabaseDriverFactory
import com.spoton.cms.data.local.SettingsManager
import com.spoton.cms.data.remote.SpotOnApi
import com.spoton.cms.data.remote.createHttpClient
import com.spoton.cms.data.repository.ArticleRepository
import com.spoton.cms.data.repository.AuthRepository
import com.spoton.cms.data.repository.OrderRepository
import com.spoton.cms.data.repository.ProductRepository
import com.spoton.cms.data.repository.StoreSettingsRepository
import com.spoton.cms.data.repository.StoreSettingsRepositoryImpl
import com.spoton.cms.data.repository.StyleRepository
import com.spoton.cms.db.SpotOnDatabase
import com.spoton.cms.data.remote.HostingerApiClient
import com.spoton.cms.data.repository.BookkeepingRepository
import com.spoton.cms.data.repository.ChatRepository
import com.spoton.cms.data.repository.ContentRepository
import com.spoton.cms.data.repository.MediaRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

expect val platformModule: Module

/**
 * Shared Koin module providing all app-wide dependencies.
 */
val sharedModule = module {
    // Settings & Serialization
    single { Settings() }
    single { kotlinx.serialization.json.Json { ignoreUnknownKeys = true } }
    singleOf(::SettingsManager)

    // Database
    single {
        val factory: DatabaseDriverFactory = get()
        val driver = factory.createDriver()
        SpotOnDatabase(driver)
    }
    single { get<SpotOnDatabase>().spotOnDatabaseQueries }

    // Networking
    single {
        val settingsManager: SettingsManager = get()
        createHttpClient(tokenProvider = { settingsManager.jwtToken })
    }
    single {
        val settingsManager: SettingsManager = get()
        SpotOnApi(
            httpClient = get(),
            baseUrlProvider = { settingsManager.serverUrl }
        )
    }

    // Repositories
    singleOf(::AuthRepository)
    singleOf(::ProductRepository)
    singleOf(::OrderRepository)
    singleOf(::StyleRepository)
    singleOf(::ArticleRepository)
    single<StoreSettingsRepository> { StoreSettingsRepositoryImpl(get()) }
    singleOf(::ChatRepository)
    singleOf(::HostingerApiClient)
    singleOf(::ContentRepository)
    singleOf(::MediaRepository)
    singleOf(::BookkeepingRepository)
}
