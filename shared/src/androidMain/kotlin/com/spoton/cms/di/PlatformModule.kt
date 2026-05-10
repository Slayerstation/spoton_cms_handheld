package com.spoton.cms.di

import com.spoton.cms.data.local.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
    singleOf(::DatabaseDriverFactory)
    // ImagePicker is registered in MainActivity because it needs ComponentActivity
}
