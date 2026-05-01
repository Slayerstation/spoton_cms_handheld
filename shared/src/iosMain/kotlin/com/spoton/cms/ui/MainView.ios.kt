package com.spoton.cms.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.spoton.cms.navigation.RootComponent

fun MainViewController(rootComponent: RootComponent) = ComposeUIViewController {
    App(rootComponent)
}
