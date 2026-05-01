package com.spoton.cms

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.runtime.remember
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.spoton.cms.di.sharedModule
import com.spoton.cms.navigation.RootComponent
import com.spoton.cms.ui.App
import org.koin.core.context.startKoin
import java.awt.Dimension

fun main() {
    // Initialize Koin
    startKoin {
        modules(sharedModule, com.spoton.cms.di.platformModule)
    }

    application {
        val lifecycle = remember { LifecycleRegistry() }
        val rootComponent = remember {
            RootComponent(
                componentContext = DefaultComponentContext(lifecycle = lifecycle)
            )
        }

        val windowState = rememberWindowState(width = 450.dp, height = 800.dp)

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "SpotOn CMS",
            resizable = true
        ) {
            window.minimumSize = Dimension(400, 600)
            App(rootComponent)
        }
    }
}
