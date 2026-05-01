package com.spoton.cms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.spoton.cms.di.sharedModule
import com.spoton.cms.navigation.RootComponent
import com.spoton.cms.ui.MainView
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Koin (only once)
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(applicationContext)
                modules(sharedModule, com.spoton.cms.di.platformModule)
            }
        }

        // Create root component with the Activity's lifecycle
        val rootComponent = RootComponent(defaultComponentContext())

        setContent {
            MainView(rootComponent)
        }
    }
}
