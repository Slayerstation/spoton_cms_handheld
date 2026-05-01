package com.spoton.cms.di

import org.koin.core.context.startKoin

/**
 * Called from iOS Swift code to initialize Koin.
 */
fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}
